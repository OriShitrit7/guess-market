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

public class EventValidator {

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
        int commissionPercentage = event.getCommissionPercentage();
        if (commissionPercentage < CommissionConfig.MIN_COMMISSION_PERCENT
                || commissionPercentage > CommissionConfig.MAX_COMMISSION_PERCENT) {
            throw new CommissionOutOfRangeException(event.getEventName(), commissionPercentage);
        }
    }

    private void validateOptionCount(MarketEvent event) throws WrongOptionCountException {
        if (event.getOptionCount() != MarketEvent.REQUIRED_OPTION_COUNT) {
            throw new WrongOptionCountException(event.getEventName(), event.getOptionCount());
        }

    }
}
