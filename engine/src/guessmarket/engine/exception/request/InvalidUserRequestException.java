package guessmarket.engine.exception.request;

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
