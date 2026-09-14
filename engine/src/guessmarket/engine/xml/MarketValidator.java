package guessmarket.engine.xml;

import guessmarket.engine.exception.file.FileErrorCode;
import guessmarket.engine.exception.file.InvalidFileException;
import guessmarket.engine.model.CommissionConfig;
import guessmarket.engine.model.MarketEvent;
import guessmarket.engine.model.User;
import guessmarket.engine.model.UsersRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Validates business rules that mapped events and users must satisfy before loading.
public class MarketValidator {

    // Checks unique IDs, commission, option counts, market makers and initial balances.
    public void validate(List<MarketEvent> events, UsersRegistry users) throws InvalidFileException {
        validateUniqueEventIds(events); // Check the rule shared by all events

        for (MarketEvent event : events) { // Validate each event
            validateCommission(event);
            validateOptionCount(event);
            validateMarketMaker(event);
        }

        for (User user : users.getAllUsers()) { // Validate each user
            validateInitialBalance(user);
        }
    }

    // Tracks the first event using each ID so duplicate errors can name both events.
    private void validateUniqueEventIds(List<MarketEvent> events) throws InvalidFileException {
        Map<Integer, String> namesById = new HashMap<>();

        for (MarketEvent event : events) {
            String existingName = namesById.putIfAbsent(event.getEventId(), event.getEventName());

            if (existingName != null) {
                throw new InvalidFileException(FileErrorCode.DUPLICATE_EVENT_ID,
                        event.getEventName(), event.getEventId(), existingName);
            }
        }
    }

    private void validateCommission(MarketEvent event) throws InvalidFileException {
        int commissionPercent = event.getCommissionPercent();

        if (!CommissionConfig.isValidPercent(commissionPercent)) {
            throw new InvalidFileException(FileErrorCode.COMMISSION_OUT_OF_RANGE, event.getEventName(), commissionPercent,
                    CommissionConfig.MIN_COMMISSION_PERCENT, CommissionConfig.MAX_COMMISSION_PERCENT);
        }
    }

    private void validateOptionCount(MarketEvent event) throws InvalidFileException {
        if (event.getOptionCount() != MarketEvent.REQUIRED_OPTION_COUNT) {
            throw new InvalidFileException(FileErrorCode.WRONG_OPTION_COUNT,
                    event.getEventName(), event.getOptionCount(), MarketEvent.REQUIRED_OPTION_COUNT);
        }
    }

    private void validateMarketMaker(MarketEvent event) throws InvalidFileException {
        if (event.getMarketMakerName() == null) {
            throw new InvalidFileException(FileErrorCode.MARKET_MAKER_MISSING, event.getEventName());
        }
    }

    private void validateInitialBalance(User user) throws InvalidFileException {
        if (user.getBalance() <= 0) {
            throw new InvalidFileException(FileErrorCode.INVALID_INITIAL_BALANCE, user.getName(), user.getBalance());
        }
    }

}
