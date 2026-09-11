package guessmarket.engine.exception.request;

import guessmarket.engine.exception.InvalidRequestException;

public class InvalidUserRequestException extends InvalidRequestException {
    private final UserRequestErrorCode code;

    public InvalidUserRequestException(UserRequestErrorCode code) {
        super(code.getMessage());
        this.code = code;
    }

    public UserRequestErrorCode getCode() {
        return code;
    }
}
