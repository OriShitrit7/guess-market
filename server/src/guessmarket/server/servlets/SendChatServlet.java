package guessmarket.server.servlets;

import guessmarket.engine.api.ChatManager;
import guessmarket.engine.exception.request.InvalidChatRequestException;
import guessmarket.server.constants.Constants;
import guessmarket.server.utils.ServletUtils;
import guessmarket.server.utils.SessionUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

// Adds a message to the shared chat on behalf of the logged-in user.
@WebServlet(
        name = "SendChatServlet",
        urlPatterns = {"/send-chat"})
public class SendChatServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain;charset=UTF-8");
        String username = SessionUtils.getUsernameOrSendUnauthorized(request, response);
        if (username == null) {
            return;
        }

        String text = request.getParameter(Constants.MESSAGE);
        ChatManager chatManager = ServletUtils.getChatManager(getServletContext());
        try {
            chatManager.addMessage(username, text);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
        catch (InvalidChatRequestException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println(e.getMessage());
        }
    }
}
