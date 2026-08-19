package guessmarket.engine.exception;

public class NoEventsException extends InvalidFileException {
    private static final String EVENT_ELEMENT = "GM-event";
    private static final String MSG_FORMAT =
            "The file contains no events. At least one <%s> element is required.";

    @Override
    public String getMessage() {
        return String.format(MSG_FORMAT, EVENT_ELEMENT);
    }
}
