package guessmarket.engine.api;

import guessmarket.dto.EventParticipationDto;
import guessmarket.dto.EventSummaryDto;
import guessmarket.dto.EventTradingStateDto;
import guessmarket.dto.PurchaseResultDto;
import guessmarket.dto.UserSummaryDto;
import guessmarket.engine.exception.InvalidFileException;

import java.util.List;

// Defines the operations available to clients of the market engine.
public interface MarketManager {

    // Loads and validates events from an XML file. Existing events are replaced only after a successful load.
    void loadSystemFile(String path) throws InvalidFileException;

    // Returns a summary of every loaded event.
    List<EventSummaryDto> getEventSummaries();

    // Returns a summary of the loaded events that are still active.
    List<EventSummaryDto> getActiveEventSummaries();

    // Returns the current account, option and trade state of the requested event.
    EventTradingStateDto getEventTradingState(int eventId);

    List<UserSummaryDto> getUserSummaries();

    List<EventParticipationDto> getUserEvents(String userName);

    // Purchases a positive quantity of shares using the zero-based option index.
    PurchaseResultDto buyShares(int eventId, int optionIndex, int quantity);

    // Closes an active event using the zero-based index of its winning option.
    EventTradingStateDto closeEvent(int eventId, int winningOptionIndex);
}
