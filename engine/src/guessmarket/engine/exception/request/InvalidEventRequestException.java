package guessmarket.engine.exception.request;

import guessmarket.engine.exception.InvalidRequestException;

public class InvalidEventRequestException extends InvalidRequestException {
    private final EventRequestErrorCode code;

    public InvalidEventRequestException(EventRequestErrorCode code) {
        super(code.getMessage());
        this.code = code;
    }

    public EventRequestErrorCode getCode() {
        return code;
    }
}
