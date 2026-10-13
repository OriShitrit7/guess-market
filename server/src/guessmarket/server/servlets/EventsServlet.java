package guessmarket.server.servlets;

import com.google.gson.Gson;
import guessmarket.dto.EventSummaryDto;
import guessmarket.engine.api.MarketManager;
import guessmarket.server.utils.ServletUtils;
import guessmarket.server.utils.SessionUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

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

        MarketManager marketManager = ServletUtils.getMarketManager(getServletContext());
        List<EventSummaryDto> eventSummaries;
        synchronized (marketManager) {
            eventSummaries = marketManager.getEventSummaries();
        }
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(eventSummaries);

        try (PrintWriter out = response.getWriter()) {
            out.print(jsonResponse);
            out.flush();
        }
    }
}
