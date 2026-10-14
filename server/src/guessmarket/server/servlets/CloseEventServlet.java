package guessmarket.server.servlets;

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

// Closes an active event with the winning option chosen by its market maker, and pays the winning holders.
@WebServlet(
        name = "CloseEventServlet",
        urlPatterns = {"/close-event"})
public class CloseEventServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain;charset=UTF-8");
        String userName = SessionUtils.getUsernameOrSendUnauthorized(request, response);
        if (userName == null) {
            return;
        }

        MarketManager marketManager = ServletUtils.getMarketManager(getServletContext());
        try {
            int eventId = Integer.parseInt(request.getParameter(Constants.EVENT_ID));
            int winningOptionIndex = Integer.parseInt(request.getParameter(Constants.WINNING_OPTION_INDEX));

            synchronized (marketManager) {
                marketManager.closeEvent(userName, eventId, winningOptionIndex);
            }
        }
        catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Please provide a valid event ID and winning option.");
        }
        catch (InvalidRequestException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println(e.getMessage());
        }
    }
}
