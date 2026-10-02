package guessmarket.server.utils;

import guessmarket.engine.api.MarketManager;
import guessmarket.engine.api.MarketManagerImpl;
import guessmarket.server.constants.Constants;
import jakarta.servlet.ServletContext;

public class ServletUtils {
    private static final Object marketManagerLock = new Object();

    public static MarketManager getMarketManager(ServletContext servletContext) {
        synchronized (marketManagerLock) {
            if (servletContext.getAttribute(Constants.MARKET_MANAGER_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(Constants.MARKET_MANAGER_ATTRIBUTE_NAME, new MarketManagerImpl());            }
            return (MarketManager) servletContext.getAttribute(Constants.MARKET_MANAGER_ATTRIBUTE_NAME);
        }
    }

}
