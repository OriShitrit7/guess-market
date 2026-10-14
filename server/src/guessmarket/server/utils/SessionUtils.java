package guessmarket.server.utils;

import guessmarket.server.constants.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

// Identifies the logged-in user of a request through its HTTP session.
public class SessionUtils {
    private static final String LOGIN_REQUIRED_MESSAGE = "You must log in first.";

    // Returns the name of the logged-in user, or null when the request has no session.
    public static String getUsername (HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object sessionAttribute = session != null ? session.getAttribute(Constants.USERNAME) : null;
        return sessionAttribute != null ? sessionAttribute.toString() : null;
    }

    // Returns the name of the logged-in user. When there is none it answers 401 and returns null,
    // so the calling servlet can stop.
    public static String getUsernameOrSendUnauthorized(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = getUsername(request);
        if (username == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println(LOGIN_REQUIRED_MESSAGE);
        }
        return username;
    }
}
