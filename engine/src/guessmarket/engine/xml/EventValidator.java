package guessmarket.engine.xml;

import guessmarket.engine.exception.CommissionOutOfRangeException;
import guessmarket.engine.exception.DuplicateEventIdException;
import guessmarket.engine.exception.InvalidFileException;
import guessmarket.engine.exception.WrongOptionCountException;
import guessmarket.engine.model.CommissionConfig;
import guessmarket.engine.model.MarketEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Validates business rules that loaded market events must satisfy before they enter the system.
public class EventValidator {

    // Checks event ID uniqueness, commission limits and the required number of options.
    public void validate(List<MarketEvent> candidateEvents) throws InvalidFileException {
        validateUniqueEventIds(candidateEvents);

        for (MarketEvent event : candidateEvents) {
            validateCommission(event);
            validateOptionCount(event);
        }
    }

    private void validateUniqueEventIds(List<MarketEvent> events) throws DuplicateEventIdException {
        Map<Integer, String> namesById = new HashMap<>();

        for (MarketEvent event : events) {
            String existingName = namesById.putIfAbsent(event.getEventId(), event.getEventName());

            if (existingName != null) {
                throw new DuplicateEventIdException(event.getEventName(), existingName, event.getEventId());
            }
        }
    }

    private void validateCommission(MarketEvent event) throws CommissionOutOfRangeException {
        int commissionPercent = event.getCommissionPercent();

        if (commissionPercent < CommissionConfig.MIN_COMMISSION_PERCENT
                || commissionPercent > CommissionConfig.MAX_COMMISSION_PERCENT) {
            throw new CommissionOutOfRangeException(event.getEventName(), commissionPercent);
        }
    }

    private void validateOptionCount(MarketEvent event) throws WrongOptionCountException {
        if (event.getOptionCount() != MarketEvent.REQUIRED_OPTION_COUNT) {
            throw new WrongOptionCountException(event.getEventName(), event.getOptionCount());
        }
    }
}
