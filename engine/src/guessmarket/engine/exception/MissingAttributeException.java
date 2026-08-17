package guessmarket.engine.exception;

public class MissingAttributeException extends InvalidFileException {
    private static final String MSG_FORMAT =
            "The <%s> element must define a non-empty '%s' attribute.";

    private final String elementName;
    private final String attributeName;

    public MissingAttributeException(String elementName, String attributeName) {
        this.elementName = elementName;
        this.attributeName = attributeName;
    }

    @Override
    public String getMessage() {
        return String.format(MSG_FORMAT, elementName, attributeName);
    }
}
