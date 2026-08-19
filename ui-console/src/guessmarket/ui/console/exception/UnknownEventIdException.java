package guessmarket.ui.console.exception;

public class UnknownEventIdException extends InvalidInputException {
    private static final String MSG_FORMAT =
            "Invalid input: no event with ID %d appears in the list above. Please try again.";

    private final int eventId;

    public UnknownEventIdException(int eventId) {
        this.eventId = eventId;
    }

    @Override
    public String getMessage() {
        return String.format(MSG_FORMAT, eventId);
    }
}
