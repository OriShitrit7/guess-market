package guessmarket.server.servlets;

import com.google.gson.Gson;
import guessmarket.dto.AccountDetailsDto;
import guessmarket.dto.VersionedDto;
import guessmarket.engine.api.MarketManager;
import guessmarket.server.constants.Constants;
import guessmarket.server.utils.ServletUtils;
import guessmarket.server.utils.SessionUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

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

        MarketManager marketManager = ServletUtils.getMarketManager(getServletContext());
        VersionedDto<AccountDetailsDto> accountDetails;
        synchronized (marketManager) {
            accountDetails = new VersionedDto<>(marketManager.getAccountDetails(userName, transactionsVersion),
                    marketManager.getTransactionCount(userName));
        }
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(accountDetails);

        try (PrintWriter out = response.getWriter()) {
            out.print(jsonResponse);
            out.flush();
        }
    }}
