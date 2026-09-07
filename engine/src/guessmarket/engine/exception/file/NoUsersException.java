package guessmarket.engine.exception.file;

import guessmarket.engine.exception.InvalidFileException;

public class NoUsersException extends InvalidFileException {
    private static final String USER_ELEMENT = "GM-user";
    private static final String MSG_FORMAT =
            "The file contains no users. At least one <%s> element is required.";

    @Override
    public String getMessage() {
        return String.format(MSG_FORMAT, USER_ELEMENT);
    }
}
