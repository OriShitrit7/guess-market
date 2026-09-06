package guessmarket.engine.exception.file;

import guessmarket.engine.exception.InvalidFileException;

public class MissingAttributeException extends InvalidFileException {
    private static final String MSG_FORMAT =
            "Event #%d in the file: the <%s> element must define a non-empty '%s' attribute.";

    private final int eventNumber;
    private final String elementName;
    private final String attributeName;

    public MissingAttributeException(int eventNumber, String elementName, String attributeName) {
        this.eventNumber = eventNumber;
        this.elementName = elementName;
        this.attributeName = attributeName;
    }

    @Override
    public String getMessage() {
        return String.format(MSG_FORMAT, eventNumber, elementName, attributeName);
    }
}
