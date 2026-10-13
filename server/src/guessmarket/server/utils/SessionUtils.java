package guessmarket.server.utils;

import guessmarket.server.constants.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class SessionUtils {
    private static final String LOGIN_REQUIRED_MESSAGE = "You must log in first.";

    public static String getUsername (HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object sessionAttribute = session != null ? session.getAttribute(Constants.USERNAME) : null;
        return sessionAttribute != null ? sessionAttribute.toString() : null;
    }

    public static String getUsernameOrSendUnauthorized(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = getUsername(request);
        if (username == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println(LOGIN_REQUIRED_MESSAGE);
        }
        return username;
    }

    public static void clearSession (HttpServletRequest request) {
        request.getSession().invalidate();
    }
}
