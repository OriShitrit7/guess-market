package guessmarket.engine.api;

import java.util.List;

public interface MarketManager {

    List<EventSummary> getEventSummaries();
    EventTradingState getEventTradingState(int eventId);
    void buyShares(int eventId, int optionIndex);

}
