package guessmarket.server.servlets;

import guessmarket.engine.api.MarketManager;
import guessmarket.engine.exception.file.InvalidFileException;
import guessmarket.server.constants.Constants;
import guessmarket.server.utils.ServletUtils;
import guessmarket.server.utils.SessionUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

@WebServlet(
        name = "FileUploadServlet",
        urlPatterns = {"/upload-file"})
@MultipartConfig
public class FileUploadServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text/plain;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String userName = SessionUtils.getUsernameOrSendUnauthorized(request, response);
        if (userName == null) {
            return;
        }

        Part filePart = request.getPart(Constants.FILE_PART_NAME);
        if (filePart == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("No file uploaded!");
            return;
        }

        MarketManager marketManager = ServletUtils.getMarketManager(getServletContext());
        try (InputStream content = filePart.getInputStream()) {
            synchronized (marketManager) {
                marketManager.loadSystemFile(content, userName);
            }
            out.println("File successfully uploaded!");
        }
        catch (InvalidFileException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println(e.getMessage());
        }
    }
}
