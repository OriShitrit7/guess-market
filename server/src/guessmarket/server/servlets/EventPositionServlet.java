package guessmarket.server.servlets;

import com.google.gson.Gson;
import guessmarket.dto.UserInvolvementDto;
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

@WebServlet(
        name = "EventPositionServlet",
        urlPatterns = {"/event-position"})
public class EventPositionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        String userName = SessionUtils.getUsernameOrSendUnauthorized(request, response);
        if (userName == null) {
            return;
        }

        MarketManager marketManager = ServletUtils.getMarketManager(getServletContext());
        UserInvolvementDto position;
        try {
            int eventId = Integer.parseInt(request.getParameter(Constants.EVENT_ID));
            synchronized (marketManager) {
                position = marketManager.getUserInvolvement(userName, eventId);
            }
        }
        catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Please provide a valid event ID.");
            return;
        }
        catch (InvalidRequestException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println(e.getMessage());
            return;
        }

        Gson gson = new Gson();
        String jsonResponse = gson.toJson(position);

        try (PrintWriter out = response.getWriter()) {
            out.print(jsonResponse);
            out.flush();
        }
    }
}
