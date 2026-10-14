package guessmarket.server.servlets;

import com.google.gson.Gson;
import guessmarket.dto.PurchaseResultDto;
import guessmarket.engine.api.MarketManager;
import guessmarket.engine.exception.request.InvalidRequestException;
import guessmarket.server.constants.Constants;
import guessmarket.server.utils.ServletUtils;
import guessmarket.server.utils.SessionUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

// Buys shares in an LMSR event for the logged-in user and returns the purchase result as JSON.
@WebServlet(
        name = "BuyLmsrSharesServlet",
        urlPatterns = {"/buy-lmsr-shares"})
public class BuyLmsrSharesServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        String userName = SessionUtils.getUsernameOrSendUnauthorized(request, response);
        if (userName == null) {
            return;
        }

        MarketManager marketManager = ServletUtils.getMarketManager(getServletContext());
        PurchaseResultDto result;
        try {
            int eventId = Integer.parseInt(request.getParameter(Constants.EVENT_ID));
            int optionIndex = Integer.parseInt(request.getParameter(Constants.OPTION_INDEX));
            int quantity = Integer.parseInt(request.getParameter(Constants.QUANTITY));

            synchronized (marketManager) {
                result = marketManager.buyLmsrShares(userName, eventId, optionIndex, quantity);
            }
        }
        catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Please provide a valid event ID, option and quantity.");
            return;
        }
        catch (InvalidRequestException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println(e.getMessage());
            return;
        }

        Gson gson = new Gson();
        String jsonResponse = gson.toJson(result);

        try (PrintWriter out = response.getWriter()) {
            out.print(jsonResponse);
            out.flush();
        }
    }
}
