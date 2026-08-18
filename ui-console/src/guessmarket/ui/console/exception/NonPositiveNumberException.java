package guessmarket.ui.console.exception;

public class NonPositiveNumberException extends InvalidInputException {
    private static final String MSG_FORMAT =
            "Invalid input: %d is not a positive number. Please enter a number greater than 0.";

    private final int value;

    public NonPositiveNumberException(int value) {
        this.value = value;
    }

    @Override
    public String getMessage() {
        return String.format(MSG_FORMAT, value);
    }
}
