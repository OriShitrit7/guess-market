package guessmarket.engine.exception;

import guessmarket.engine.model.MarketEvent;

public class WrongOptionCountException extends InvalidFileException {
    private static final String MSG_FORMAT =
            "Event '%s': the number of options is %d. Each event must have exactly %d options.";

    private final String eventName;
    private final int optionCount;

    public WrongOptionCountException(String eventName, int optionCount) {
        this.eventName = eventName;
        this.optionCount = optionCount;
    }

    @Override
    public String getMessage() {
        return String.format(MSG_FORMAT, eventName, optionCount, MarketEvent.REQUIRED_OPTION_COUNT);
    }
}
