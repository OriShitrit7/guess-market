package guessmarket.engine.api;

import guessmarket.dto.EventSummaryDto;
import guessmarket.dto.EventTradingStateDto;
import guessmarket.dto.PurchaseResultDto;
import guessmarket.engine.exception.InvalidFileException;

import java.util.List;

public interface MarketManager {

    void loadSystemFile(String path) throws InvalidFileException;
    List<EventSummaryDto> getEventSummaries();
    List<EventSummaryDto> getActiveEventSummaries();
    EventTradingStateDto getEventTradingState(int eventId);
    PurchaseResultDto buyShares(int eventId, int optionIndex, int quantity);
    EventTradingStateDto closeEvent(int eventId, int winningOptionIndex);
}
