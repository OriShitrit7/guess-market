package guessmarket.server.servlets;

import com.google.gson.Gson;
import guessmarket.dto.EventStateDto;
import guessmarket.dto.VersionedDto;
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

// Returns the full state of the event given by the eventId parameter as JSON. When the event has not changed
// since the version the client sent, the answer is 204 with no body.
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

        int clientVersion = ServletUtils.parseVersion(request.getParameter(Constants.VERSION));
        MarketManager marketManager = ServletUtils.getMarketManager(getServletContext());
        VersionedDto<EventStateDto> eventState;
        try {
            int eventId = Integer.parseInt(request.getParameter(Constants.EVENT_ID));
            synchronized (marketManager) {
                int currentVersion = marketManager.getEventChangeCount(eventId);
                if (currentVersion == clientVersion) {
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    return;
                }
                eventState = new VersionedDto<>(marketManager.getEventState(eventId), currentVersion);
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
