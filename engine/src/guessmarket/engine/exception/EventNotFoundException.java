package guessmarket.engine.exception;

public class EventNotFoundException extends InvalidRequestException {
    private static final String MSG_FORMAT = "No event with ID %d exists in the system.";

    private final int eventId;

    public EventNotFoundException(int eventId) {
        this.eventId = eventId;
    }

    @Override
    public String getMessage() {
        return String.format(MSG_FORMAT, eventId);
    }
}
