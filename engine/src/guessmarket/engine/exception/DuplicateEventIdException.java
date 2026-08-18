package guessmarket.engine.exception;

public class DuplicateEventIdException extends InvalidFileException {
    private static final String MSG_FORMAT =
            "Event '%s' has ID %d, which is already used by event '%s'. Event IDs must be unique.";

    private final String eventName;
    private final String existingEventName;
    private final int eventId;

    public DuplicateEventIdException(String eventName, String existingEventName, int eventId) {
        this.eventName = eventName;
        this.existingEventName = existingEventName;
        this.eventId = eventId;
    }

    @Override
    public String getMessage() {
        return String.format(MSG_FORMAT, eventName, eventId, existingEventName);
    }
}
