package guessmarket.engine.exception;

public class OptionNotFoundException extends InvalidRequestException {
    private static final String MSG_FORMAT =
            "Event '%s': option index %d is out of range. The event has %d options.";

    private final String eventName;
    private final int optionIndex;
    private final int optionCount;

    public OptionNotFoundException(String eventName, int optionIndex, int optionCount) {
        this.eventName = eventName;
        this.optionIndex = optionIndex;
        this.optionCount = optionCount;
    }

    @Override
    public String getMessage() {
        return String.format(MSG_FORMAT, eventName, optionIndex, optionCount);
    }
}
