package guessmarket.server.servlets;

import com.google.gson.Gson;
import guessmarket.dto.AccountDetailsDto;
import guessmarket.dto.VersionedDto;
import guessmarket.engine.api.MarketManager;
import guessmarket.server.constants.Constants;
import guessmarket.server.utils.ServletUtils;
import guessmarket.server.utils.SessionUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

// Returns the logged-in user's account details as JSON. The transactionsVersion parameter is the number of
// transactions the client already has, so only newer ones are sent. The user's events depend on the events
// list, so the eventsVersion parameter is checked too, and when neither has changed the answer is 204.
// The inner version of the response is the transaction count and the outer one the events version.
@WebServlet(
        name = "AccountDetailsServlet",
        urlPatterns = {"/account-details"})
public class AccountDetailsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        String userName = SessionUtils.getUsernameOrSendUnauthorized(request, response);
        if (userName == null) {
            return;
        }

        int transactionsVersion;
        try {
            transactionsVersion = Integer.parseInt(request.getParameter(Constants.TRANSACTIONS_VERSION));
        }
        catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Please provide a valid transactions version.");
            return;
        }

        int eventsVersion = ServletUtils.parseVersion(request.getParameter(Constants.EVENTS_VERSION));
        MarketManager marketManager = ServletUtils.getMarketManager(getServletContext());
        VersionedDto<VersionedDto<AccountDetailsDto>> accountDetails;
        synchronized (marketManager) {
            int transactionCount = marketManager.getTransactionCount(userName);
            int eventsChangeCount = marketManager.getEventsChangeCount();
            if (transactionCount == transactionsVersion && eventsChangeCount == eventsVersion) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return;
            }
            VersionedDto<AccountDetailsDto> details = new VersionedDto<>(
                    marketManager.getAccountDetails(userName, transactionsVersion), transactionCount);
            accountDetails = new VersionedDto<>(details, eventsChangeCount);
        }
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(accountDetails);

        try (PrintWriter out = response.getWriter()) {
            out.print(jsonResponse);
            out.flush();
        }
    }
}
