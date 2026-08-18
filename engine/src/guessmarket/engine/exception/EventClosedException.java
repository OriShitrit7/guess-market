package guessmarket.engine.exception;

public class EventClosedException extends InvalidRequestException {
    private static final String MSG_FORMAT =
            "Event '%s' is closed. It can no longer be traded on or closed.";

    private final String eventName;

    public EventClosedException(String eventName) {
        this.eventName = eventName;
    }

    @Override
    public String getMessage() {
        return String.format(MSG_FORMAT, eventName);
    }
}
