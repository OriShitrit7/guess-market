package guessmarket.engine.exception;

import guessmarket.engine.xml.EventXmlMapper;

public class NoEventsException extends InvalidFileException {
    private static final String MSG_FORMAT =
            "The file contains no events. At least one <%s> element is required.";

    @Override
    public String getMessage() {
        return String.format(MSG_FORMAT, EventXmlMapper.EVENT_ELEMENT);
    }
}
