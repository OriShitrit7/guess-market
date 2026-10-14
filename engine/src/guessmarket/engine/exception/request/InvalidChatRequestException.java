package guessmarket.engine.exception.request;

public class InvalidChatRequestException extends InvalidRequestException {
    private final ChatRequestErrorCode code;

    public InvalidChatRequestException(ChatRequestErrorCode code) {
        super(code.getMessage());
        this.code = code;
    }

    public ChatRequestErrorCode getCode() {
        return code;
    }
}
