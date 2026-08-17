package guessmarket.engine.exception;

public class DuplicateEventIdException extends InvalidFileException {
    private static final String MSG_FORMAT =
            "Event '%s': ID %d is already assigned to another event. Event IDs must be unique.";

    private final String eventName;
    private final int eventId;

    public DuplicateEventIdException(String eventName, int eventId) {
        this.eventName = eventName;
        this.eventId = eventId;
    }

    @Override
    public String getMessage() {
        return String.format(MSG_FORMAT, eventName, eventId);
    }
}
