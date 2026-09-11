package guessmarket.engine.exception;

// Parent exception for invalid operations requested from the market engine.
public abstract class InvalidRequestException extends RuntimeException {
    protected InvalidRequestException() {
    }

    protected InvalidRequestException(String message) {
        super(message);
    }
}
