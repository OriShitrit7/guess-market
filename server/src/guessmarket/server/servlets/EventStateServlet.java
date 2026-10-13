package guessmarket.server.servlets;

import com.google.gson.Gson;
import guessmarket.dto.EventStateDto;
import guessmarket.engine.api.MarketManager;
import guessmarket.engine.exception.request.InvalidEventRequestException;
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
        name = "EventStateServlet",
        urlPatterns = {"/event-state"})
public class EventStateServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        if (SessionUtils.getUsernameOrSendUnauthorized(request, response) == null) {
            return;
        }

        MarketManager marketManager = ServletUtils.getMarketManager(getServletContext());
        EventStateDto eventState;
        try {
            int eventId = Integer.parseInt(request.getParameter(Constants.EVENT_ID));
            synchronized (marketManager) {
                eventState = marketManager.getEventState(eventId);
            }
        }
        catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Please provide a valid event ID.");
            return;
        }
        catch (InvalidEventRequestException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println(e.getMessage());
            return;
        }

        Gson gson = new Gson();
        String jsonResponse = gson.toJson(eventState);

        try (PrintWriter out = response.getWriter()) {
            out.print(jsonResponse);
            out.flush();
        }
    }
}
