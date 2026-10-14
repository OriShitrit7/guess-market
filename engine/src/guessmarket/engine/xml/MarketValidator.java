package guessmarket.engine.xml;

import guessmarket.engine.exception.file.FileErrorCode;
import guessmarket.engine.exception.file.InvalidFileException;
import guessmarket.engine.model.CommissionConfig;
import guessmarket.engine.model.MarketEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

// Validates the business rules that the events of a file must satisfy before they are loaded.
public class MarketValidator {

    // Checks that event names are unique within the file, and that every event has a valid commission
    // and the required number of options.
    public void validate(List<MarketEvent> events) throws InvalidFileException {
        validateUniqueEventNames(events); // Check the rule shared by all events

        for (MarketEvent event : events) { // Validate each event
            validateCommission(event);
            validateOptionCount(event);
        }
    }

    // Names are compared without regard to case.
    private void validateUniqueEventNames(List<MarketEvent> events) throws InvalidFileException {
        Set<String> seenNames = new HashSet<>();

        for (MarketEvent event : events) {
            if (!seenNames.add(event.getEventName().toLowerCase(Locale.ROOT))) {
                throw new InvalidFileException(FileErrorCode.DUPLICATE_EVENT_NAME, event.getEventName());
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
}
