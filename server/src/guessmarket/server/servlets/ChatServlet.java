package guessmarket.server.servlets;

import com.google.gson.Gson;
import guessmarket.dto.ChatMessageDto;
import guessmarket.dto.VersionedDto;
import guessmarket.engine.chat.ChatManager;
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

// Returns the chat messages that the logged-in user does not have yet. The client sends the version it
// holds, which is the number of messages it received, and the server answers 204 when there is nothing new.
@WebServlet(
        name = "ChatServlet",
        urlPatterns = {"/chat"})
public class ChatServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (SessionUtils.getUsernameOrSendUnauthorized(request, response) == null) {
            return;
        }

        int clientVersion = ServletUtils.parseVersion(request.getParameter(Constants.VERSION));
        ChatManager chatManager = ServletUtils.getChatManager(getServletContext());

        VersionedDto<List<ChatMessageDto>> result;
        // The version and the messages are read together, so that no message is added in between.
        synchronized (chatManager) {
            int currentVersion = chatManager.getVersion();
            if (clientVersion == currentVersion) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return;
            }
            result = new VersionedDto<>(chatManager.getNewMessages(clientVersion), currentVersion);
        }

        response.setContentType("application/json;charset=UTF-8");
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(result);

        try (PrintWriter out = response.getWriter()) {
            out.print(jsonResponse);
        }
    }
}
