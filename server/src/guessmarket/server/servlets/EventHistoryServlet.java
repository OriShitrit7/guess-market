package guessmarket.server.servlets;

import com.google.gson.Gson;
import guessmarket.dto.EventHistoryDto;
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
import java.util.Arrays;
import java.util.List;

// Returns only what was added to an event's trade and price history as JSON. The tradesFrom parameter and
// one pricesFrom parameter per option are the counts the client already holds. When nothing was added
// since, the answer is 204 with no body.
@WebServlet(
        name = "EventHistoryServlet",
        urlPatterns = {"/event-history"})
public class EventHistoryServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        if (SessionUtils.getUsernameOrSendUnauthorized(request, response) == null) {
            return;
        }

        int tradesFrom = ServletUtils.parseVersion(request.getParameter(Constants.TRADES_FROM));
        List<Integer> pricesFrom = parseCounts(request.getParameterValues(Constants.PRICES_FROM));
        MarketManager marketManager = ServletUtils.getMarketManager(getServletContext());
        EventHistoryDto history;
        try {
            int eventId = Integer.parseInt(request.getParameter(Constants.EVENT_ID));
            synchronized (marketManager) {
                history = marketManager.getEventHistory(eventId, tradesFrom, pricesFrom);
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

        if (history.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(history);

        try (PrintWriter out = response.getWriter()) {
            out.print(jsonResponse);
            out.flush();
        }
    }

    private List<Integer> parseCounts(String[] counts) {
        if (counts == null) {
            return List.of();
        }
        return Arrays.stream(counts)
                .map(ServletUtils::parseVersion)
                .toList();
    }
}
