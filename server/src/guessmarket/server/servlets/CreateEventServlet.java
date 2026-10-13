package guessmarket.server.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import guessmarket.dto.NewEventDto;
import guessmarket.engine.api.MarketManager;
import guessmarket.engine.exception.request.InvalidRequestException;
import guessmarket.server.utils.ServletUtils;
import guessmarket.server.utils.SessionUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(
        name = "CreateEventServlet",
        urlPatterns = {"/create-event"})
public class CreateEventServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain;charset=UTF-8");
        String userName = SessionUtils.getUsernameOrSendUnauthorized(request, response);
        if (userName == null) {
            return;
        }

        MarketManager marketManager = ServletUtils.getMarketManager(getServletContext());
        Gson gson = new Gson();
        try {
            NewEventDto newEvent = gson.fromJson(request.getReader(), NewEventDto.class);
            synchronized (marketManager) {
                marketManager.createEvent(userName, newEvent);
            }
        }
        catch (JsonSyntaxException | NullPointerException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Please provide valid event details.");
        }
        catch (InvalidRequestException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println(e.getMessage());
        }
    }
}
