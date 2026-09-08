package guessmarket.engine.exception.request;

import guessmarket.engine.exception.InvalidRequestException;

public class UserNotFoundException extends InvalidRequestException {
    private static final String MSG_FORMAT = "No user named '%s' exists in the system.";

    private final String userName;

    public UserNotFoundException(String userName) {
        this.userName = userName;
    }

    @Override
    public String getMessage() {
        return String.format(MSG_FORMAT, userName);
    }
}
