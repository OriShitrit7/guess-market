package guessmarket.engine.exception;

public class MissingElementException extends InvalidFileException {
    private static final String MSG_FORMAT = "Event '%s' is missing the required <%s> element.";

    private final String eventName;
    private final String elementName;

    public MissingElementException(String eventName, String elementName) {
        this.eventName = eventName;
        this.elementName = elementName;
    }

    @Override
    public String getMessage() {
        return String.format(MSG_FORMAT, eventName, elementName);
    }
}
