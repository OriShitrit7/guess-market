package guessmarket.engine.api;

import guessmarket.dto.AccountDetailsDto;
import guessmarket.dto.EventHistoryDto;
import guessmarket.dto.EventParticipationDto;
import guessmarket.dto.EventStateDto;
import guessmarket.dto.EventSummaryDto;
import guessmarket.dto.NewEventDto;
import guessmarket.dto.OrderActionDto;
import guessmarket.dto.OrderResultDto;
import guessmarket.dto.PurchaseResultDto;
import guessmarket.dto.TradeDto;
import guessmarket.dto.UserInvolvementDto;
import guessmarket.dto.UserSummaryDto;
import guessmarket.engine.exception.file.InvalidFileException;

import java.io.InputStream;
import java.util.List;

// Defines the operations available to clients of the market engine.
public interface MarketManager {

    // Adds a new user with an empty balance. The name must be in English and not already taken.
    void registerUser(String username);

    // Loads and validates events from an XML file and adds them to the system, with the uploader as
    // their market maker. Nothing is added when the file is invalid.
    void loadSystemFile(String fileName, InputStream content, String uploaderName) throws InvalidFileException;

    // Returns a summary of every event in the system.
    List<EventSummaryDto> getEventSummaries();

    // Returns the full state of one event, in the form that matches its trading method.
    EventStateDto getEventState(int eventId);

    // Returns the balance, blocked state and market maker status of every user.
    List<UserSummaryDto> getUserSummaries();

    // Returns every event in which the named user is the market maker or a participant.
    List<EventParticipationDto> getUserEvents(String userName);

    // Returns the named user's summary and events, with the account transactions recorded from the
    // given index onward.
    AccountDetailsDto getAccountDetails(String userName, int fromTransaction);

    // Returns how many times the list of events has changed: an event was added, or anything in one changed.
    int getEventsChangeCount();

    // Returns how many times the list of users has changed: a user was added, or a balance, block or
    // market maker role changed.
    int getUsersChangeCount();

    // Returns how many times anything in the requested event has changed.
    int getEventChangeCount(int eventId);

    // Returns how many account transactions the named user has. Callers use it as the version of the list.
    int getTransactionCount(String userName);

    // Returns what was added to the event's trade and price history after the counts the caller already holds:
    // tradesFrom trades, and for each option the number of price points in pricesFrom.
    EventHistoryDto getEventHistory(int eventId, int tradesFrom, List<Integer> pricesFrom);

    // Returns the named user's trades, holdings, paid commission and final result in the requested event.
    UserInvolvementDto getUserInvolvement(String userName, int eventId);

    // Prices an LMSR purchase without performing it, so the caller can preview its cost and commission.
    TradeDto quoteLmsrPurchase(String userName, int eventId, int optionIndex, int quantity);

    // Adds money to the named user's balance. The amount must be positive, with at most two decimal places.
    void loadFunds(String userName, double amount);

    // Purchases a positive quantity of shares in an LMSR event for the named user, using the zero-based
    // option index.
    PurchaseResultDto buyLmsrShares(String userName, int eventId, int optionIndex, int quantity);

    // Submits an Order Book request and reports the quantities matched, minted and left waiting.
    OrderResultDto submitOrder(String userName, int eventId, int optionIndex, OrderActionDto action,
                               int quantity, double pricePerShare);

    // Charges the named market maker and opens an event that has not started yet.
    EventStateDto openEvent(String userName, int eventId);

    // Closes an active event using the zero-based index of its winning option.
    EventStateDto closeEvent(String userName, int eventId, int winningOptionIndex);

    // Creates an event owned by the named user, who becomes its market maker.
    EventSummaryDto createEvent(String creatorName, NewEventDto request);
}
