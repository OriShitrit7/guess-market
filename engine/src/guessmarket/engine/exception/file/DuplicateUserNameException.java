package guessmarket.engine.exception.file;

import guessmarket.engine.exception.InvalidFileException;

public class DuplicateUserNameException extends InvalidFileException {
    private static final String MSG_FORMAT =
            "The user name '%s' appears more than once in the file. User names must be unique.";

    private final String userName;

    public DuplicateUserNameException(String userName) {
        this.userName = userName;
    }

    @Override
    public String getMessage() {
        return String.format(MSG_FORMAT, userName);
    }
}
