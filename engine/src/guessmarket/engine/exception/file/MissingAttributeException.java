package guessmarket.engine.exception.file;

import guessmarket.engine.exception.InvalidFileException;

public class MissingAttributeException extends InvalidFileException {
    private static final String MSG_FORMAT =
            "%s #%d in the file: the <%s> element must define a non-empty '%s' attribute.";

    private final String entityName;
    private final int entityNumber;
    private final String elementName;
    private final String attributeName;

    public MissingAttributeException(String entityName, int entityNumber, String elementName,
                                     String attributeName) {
        this.entityName = entityName;
        this.entityNumber = entityNumber;
        this.elementName = elementName;
        this.attributeName = attributeName;
    }

    @Override
    public String getMessage() {
        return String.format(MSG_FORMAT, entityName, entityNumber, elementName, attributeName);
    }
}
