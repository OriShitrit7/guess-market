package guessmarket.server.servlets;

import com.google.gson.Gson;
import guessmarket.dto.UserSummaryDto;
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

// Returns the name, balance and market maker status of every user as JSON.
@WebServlet(
        name = "UsersServlet",
        urlPatterns = {"/users"})
public class UsersServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        if (SessionUtils.getUsernameOrSendUnauthorized(request, response) == null) {
            return;
        }

        int clientVersion = ServletUtils.parseVersion(request.getParameter(Constants.VERSION));
        MarketManager marketManager = ServletUtils.getMarketManager(getServletContext());

        VersionedDto<List<UserSummaryDto>> userSummaries;
        synchronized (marketManager) {
            int currentVersion = marketManager.getUsersChangeCount();
            if (currentVersion == clientVersion) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return;
            }
            userSummaries = new VersionedDto<>(marketManager.getUserSummaries(), currentVersion);
        }
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(userSummaries);

        try (PrintWriter out = response.getWriter()) {
            out.print(jsonResponse);
            out.flush();
        }
    }
}
