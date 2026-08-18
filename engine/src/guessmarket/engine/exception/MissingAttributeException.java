package guessmarket.engine.exception;

public class MissingAttributeException extends InvalidFileException {
    private static final String MSG_FORMAT =
            "Event #%d in the file: the <%s> element must define a non-empty '%s' attribute.";

    private final int eventPosition;
    private final String elementName;
    private final String attributeName;

    public MissingAttributeException(int eventPosition, String elementName, String attributeName) {
        this.eventPosition = eventPosition;
        this.elementName = elementName;
        this.attributeName = attributeName;
    }

    @Override
    public String getMessage() {
        return String.format(MSG_FORMAT, eventPosition, elementName, attributeName);
    }
}
