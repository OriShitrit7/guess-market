package guessmarket.engine.api;

import guessmarket.dto.BalancePointDto;
import guessmarket.dto.EventParticipationDto;
import guessmarket.dto.EventSummaryDto;
import guessmarket.dto.EventTradingStateDto;
import guessmarket.dto.NewEventDto;
import guessmarket.dto.OrderActionDto;
import guessmarket.dto.OrderBookStateDto;
import guessmarket.dto.OrderResultDto;
import guessmarket.dto.PurchaseResultDto;
import guessmarket.dto.TradeDto;
import guessmarket.dto.UserInvolvementDto;
import guessmarket.dto.UserSummaryDto;
import guessmarket.engine.exception.file.InvalidFileException;

import java.util.List;

// Defines the operations available to clients of the market engine.
public interface MarketManager {

    // Loads and validates events from an XML file. Existing events are replaced only after a successful load.
    void loadSystemFile(String path) throws InvalidFileException;

    // Returns a summary of every loaded event.
    List<EventSummaryDto> getEventSummaries();

    // Returns the current account, option and trade state of the requested event.
    EventTradingStateDto getEventTradingState(int eventId);

    // Returns the books, statistics and participants of the requested order book event.
    OrderBookStateDto getOrderBookState(int eventId);

    // Returns the current balance and blocked state of every user.
    List<UserSummaryDto> getUserSummaries();

    // Returns the named user's balance from its initial value through every balance change.
    List<BalancePointDto> getUserBalanceHistory(String userName);

    // Returns every event in which the named user is the market maker or a participant.
    List<EventParticipationDto> getUserEvents(String userName);

    // Returns the named user's own trades in the requested event, newest first.
    UserInvolvementDto getUserInvolvement(String userName, int eventId);

    // Prices a purchase without performing it, so the caller can preview its cost and commission.
    TradeDto quotePurchase(String userName, int eventId, int optionIndex, int quantity);

    // Purchases a positive quantity of shares for the named user, using the zero-based option index.
    PurchaseResultDto buyShares(String userName, int eventId, int optionIndex, int quantity);

    // Submits an Order Book request and reports the quantities matched, minted and left waiting.
    OrderResultDto submitOrder(String userName, int eventId, int optionIndex, OrderActionDto action,
                               int quantity, double pricePerShare);

    // Charges the named market maker and opens an event that has not started yet.
    EventTradingStateDto openEvent(String userName, int eventID);

    // Closes an active event using the zero-based index of its winning option.
    EventTradingStateDto closeEvent(String userName, int eventId, int winningOptionIndex);

    // Creates an event owned by the named user, who becomes its market maker.
    EventSummaryDto createEvent(String creatorName, NewEventDto request);
}
