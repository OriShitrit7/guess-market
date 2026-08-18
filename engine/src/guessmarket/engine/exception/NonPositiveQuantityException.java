package guessmarket.engine.exception;

public class NonPositiveQuantityException extends InvalidRequestException {
    private static final String MSG_FORMAT = "The quantity must be greater than 0, but was %d.";

    private final int quantity;

    public NonPositiveQuantityException(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String getMessage() {
        return String.format(MSG_FORMAT, quantity);
    }
}
