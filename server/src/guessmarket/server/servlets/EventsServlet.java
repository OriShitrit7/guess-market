package guessmarket.server.servlets;

import com.google.gson.Gson;
import guessmarket.dto.EventSummaryDto;
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
import java.util.List;

// Returns a summary of every event in the system as JSON. When the list has not changed since the version
// the client sent, the answer is 204 with no body.
@WebServlet(
        name = "EventsServlet",
        urlPatterns = {"/events"})
public class EventsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        if (SessionUtils.getUsernameOrSendUnauthorized(request, response) == null) {
            return;
        }

        int clientVersion = ServletUtils.parseVersion(request.getParameter(Constants.VERSION));
        MarketManager marketManager = ServletUtils.getMarketManager(getServletContext());
        VersionedDto<List<EventSummaryDto>> eventSummaries;
        synchronized (marketManager) {
            int currentVersion = marketManager.getEventsChangeCount();
            if (currentVersion == clientVersion) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return;
            }
            eventSummaries = new VersionedDto<>(marketManager.getEventSummaries(), currentVersion);
        }
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(eventSummaries);

        try (PrintWriter out = response.getWriter()) {
            out.print(jsonResponse);
            out.flush();
        }
    }
}
