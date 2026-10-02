package guessmarket.server.servlets;

import guessmarket.engine.api.MarketManager;
import guessmarket.engine.exception.request.InvalidUserRequestException;
import guessmarket.server.constants.Constants;
import guessmarket.server.utils.ServletUtils;
import guessmarket.server.utils.SessionUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(
        name = "LoginServlet",
        urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
            marketManager.registerUser(usernameFromParameter);
            request.getSession(true).setAttribute(Constants.USERNAME, usernameFromParameter);
            response.setStatus(HttpServletResponse.SC_OK);
        }
        catch (InvalidUserRequestException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println(e.getMessage());
        }
    }
}
