package guessmarket.server.servlets;

import com.google.gson.Gson;
import guessmarket.dto.OrderActionDto;
import guessmarket.dto.OrderResultDto;
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
import java.util.Locale;

// Submits a buy or sell order to an Order Book event for the logged-in user,
// and returns how the order was executed as JSON.
@WebServlet(
        name = "SubmitOrderServlet",
        urlPatterns = {"/submit-order"})
public class SubmitOrderServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        String userName = SessionUtils.getUsernameOrSendUnauthorized(request, response);
        if (userName == null) {
            return;
        }

        MarketManager marketManager = ServletUtils.getMarketManager(getServletContext());
        OrderResultDto result;
        try {
            int eventId = Integer.parseInt(request.getParameter(Constants.EVENT_ID));
            int optionIndex = Integer.parseInt(request.getParameter(Constants.OPTION_INDEX));
            int quantity = Integer.parseInt(request.getParameter(Constants.QUANTITY));
            double price = Double.parseDouble(request.getParameter(Constants.PRICE));
            OrderActionDto action = parseAction(request.getParameter(Constants.ACTION));

            synchronized (marketManager) {
                result = marketManager.submitOrder(userName, eventId, optionIndex, action, quantity, price);
            }
        }
        catch (NumberFormatException | NullPointerException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Please provide a valid event ID, option, quantity and price.");
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

    // An unknown action becomes null, so that the engine rejects it with its own message.
    private OrderActionDto parseAction(String action) {
        try {
            return OrderActionDto.valueOf(action.trim().toUpperCase(Locale.ROOT));
        }
        catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }
}
