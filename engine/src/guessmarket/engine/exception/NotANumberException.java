package guessmarket.engine.exception;

public class NotANumberException extends InvalidFileException {
    private static final String MSG_FORMAT =
            "Event '%s': the value '%s' in <%s> is not a valid integer.";

    private final String eventName;
    private final String elementName;
    private final String value;

    public NotANumberException(String eventName, String elementName, String value) {
        this.eventName = eventName;
        this.elementName = elementName;
        this.value = value;
    }

    @Override
    public String getMessage() {
        return String.format(MSG_FORMAT, eventName, value, elementName);
    }
}
