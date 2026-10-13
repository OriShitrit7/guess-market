package guessmarket.engine.api;

import guessmarket.dto.AccountDetailsDto;
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

    void registerUser(String username);

    // Loads and validates events from an XML file. Existing events are replaced only after a successful load.
    void loadSystemFile(InputStream content, String uploaderName) throws InvalidFileException;

    // Returns a summary of every loaded event.
    List<EventSummaryDto> getEventSummaries();

    EventStateDto getEventState(int eventId);

    // Returns the current balance and blocked state of every user.
    List<UserSummaryDto> getUserSummaries();

    // Returns every event in which the named user is the market maker or a participant.
    List<EventParticipationDto> getUserEvents(String userName);

    AccountDetailsDto getAccountDetails(String userName, int fromTransaction);

    int getTransactionCount(String userName);

    // Returns the named user's own trades in the requested event, newest first.
    UserInvolvementDto getUserInvolvement(String userName, int eventId);

    // Prices a purchase without performing it, so the caller can preview its cost and commission.
    TradeDto quoteLmsrPurchase(String userName, int eventId, int optionIndex, int quantity);

    void loadFunds(String userName, double amount);

    // Purchases a positive quantity of shares for the named user, using the zero-based option index.
    PurchaseResultDto buyLmsrShares(String userName, int eventId, int optionIndex, int quantity);

    // Submits an Order Book request and reports the quantities matched, minted and left waiting.
    OrderResultDto submitOrder(String userName, int eventId, int optionIndex, OrderActionDto action,
                               int quantity, double pricePerShare);

    // Charges the named market maker and opens an event that has not started yet.
    EventStateDto openEvent(String userName, int eventID);

    // Closes an active event using the zero-based index of its winning option.
    EventStateDto closeEvent(String userName, int eventId, int winningOptionIndex);

    // Creates an event owned by the named user, who becomes its market maker.
    EventSummaryDto createEvent(String creatorName, NewEventDto request);
}
