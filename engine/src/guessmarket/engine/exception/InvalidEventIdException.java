package guessmarket.engine.exception;

public class InvalidEventIdException extends InvalidFileException {
    private static final String MSG_FORMAT =
            "Event #%d in the file: <id> is missing or is not a positive integer.";

    private final int eventPosition;

    public InvalidEventIdException(int eventPosition) {
        this.eventPosition = eventPosition;
    }

    @Override
    public String getMessage() {
        return String.format(MSG_FORMAT, eventPosition);
    }
}
