package guessmarket.server.utils;

import guessmarket.engine.api.MarketManager;
import guessmarket.engine.api.MarketManagerImpl;
import guessmarket.engine.chat.ChatManager;
import guessmarket.server.constants.Constants;
import jakarta.servlet.ServletContext;

// Gives the servlets access to the single market engine shared by the whole application.
public class ServletUtils {
    private static final int NO_VERSION = -1;

    private static final Object marketManagerLock = new Object();
    private static final Object chatManagerLock = new Object();

    // Returns the engine kept in the servlet context, creating it on the first call. The engine is not
    // synchronized itself, so every servlet locks on the returned object while calling it.
    public static MarketManager getMarketManager(ServletContext servletContext) {
        synchronized (marketManagerLock) {
            if (servletContext.getAttribute(Constants.MARKET_MANAGER_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(Constants.MARKET_MANAGER_ATTRIBUTE_NAME, new MarketManagerImpl());
            }
            return (MarketManager) servletContext.getAttribute(Constants.MARKET_MANAGER_ATTRIBUTE_NAME);
        }
    }

    public static ChatManager getChatManager(ServletContext servletContext) {
        synchronized (chatManagerLock) {
            if (servletContext.getAttribute(Constants.CHAT_MANAGER_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(Constants.CHAT_MANAGER_ATTRIBUTE_NAME, new ChatManager());
            }
            return (ChatManager) servletContext.getAttribute(Constants.CHAT_MANAGER_ATTRIBUTE_NAME);
        }
    }

    // A missing or invalid version means the client has nothing yet, so it receives the full list.
    public static int parseVersion(String version) {
        try {
            return Integer.parseInt(version);
        }
        catch (NumberFormatException e) {
            return NO_VERSION;
        }
    }
}
