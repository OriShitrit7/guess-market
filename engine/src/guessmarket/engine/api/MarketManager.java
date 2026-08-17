package guessmarket.engine.api;

import guessmarket.engine.exception.InvalidFileException;

import java.util.List;

public interface MarketManager {

    void loadSystemFile(String path) throws InvalidFileException;
    List<EventSummary> getEventSummaries();
    EventTradingState getEventTradingState(int eventId);
    void buyShares(int eventId, int optionIndex);

}
