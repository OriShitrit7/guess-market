package guessmarket.engine.api;

import guessmarket.engine.exception.InvalidFileException;

import java.util.List;

public interface MarketManager {

    void loadSystemFile(String path) throws InvalidFileException;
    List<EventSummary> getEventSummaries();
    List<EventSummary> getActiveEventSummaries();
    EventTradingState getEventTradingState(int eventId);
    TradeInfo buyShares(int eventId, int optionIndex, int quantity);
    void closeEvent(int eventId, int winningOptionIndex);
}
