package guessmarket.server.servlets;

import guessmarket.engine.api.MarketManager;
import guessmarket.engine.exception.request.InvalidUserRequestException;
import guessmarket.engine.exception.request.UserRequestErrorCode;
import guessmarket.server.constants.Constants;
import guessmarket.server.utils.ServletUtils;
import guessmarket.server.utils.SessionUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

// Registers a new user by name and opens a session for them.
// A request that already has a session is accepted without registering again.
@WebServlet(
        name = "LoginServlet",
        urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain;charset=UTF-8");

        if (SessionUtils.getUsername(request) != null) {
            return;
        }

        String usernameFromParameter = request.getParameter(Constants.USERNAME);
        if (usernameFromParameter == null || usernameFromParameter.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Please enter a user name.");
            return;
        }

        usernameFromParameter = usernameFromParameter.trim();
        MarketManager marketManager = ServletUtils.getMarketManager(getServletContext());
        try {
            synchronized (marketManager) {
                marketManager.registerUser(usernameFromParameter);
            }
            request.getSession(true).setAttribute(Constants.USERNAME, usernameFromParameter);
            response.setStatus(HttpServletResponse.SC_OK);
        }
        catch (InvalidUserRequestException e) {
            response.setStatus(e.getCode() == UserRequestErrorCode.USER_NAME_TAKEN
                    ? HttpServletResponse.SC_CONFLICT : HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println(e.getMessage());
        }
    }
}
