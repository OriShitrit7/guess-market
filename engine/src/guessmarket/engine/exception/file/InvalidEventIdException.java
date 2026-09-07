package guessmarket.engine.exception.file;

import guessmarket.engine.exception.InvalidFileException;

public class InvalidEventIdException extends InvalidFileException {
    private static final String MSG_FORMAT =
            "Event #%d in the file: <id> is missing or is not a positive integer.";

    private final int eventNumber;

    public InvalidEventIdException(int eventNumber) {
        this.eventNumber = eventNumber;
    }

    @Override
    public String getMessage() {
        return String.format(MSG_FORMAT, eventNumber);
    }
}
