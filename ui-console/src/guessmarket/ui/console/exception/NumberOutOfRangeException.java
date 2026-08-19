package guessmarket.ui.console.exception;

public class NumberOutOfRangeException extends InvalidInputException {
    private static final String MSG_FORMAT =
            "Invalid input: %d is out of range. Please enter a whole number between %d and %d.";

    private final int value;
    private final int min;
    private final int max;

    public NumberOutOfRangeException(int value, int min, int max) {
        this.value = value;
        this.min = min;
        this.max = max;
    }

    @Override
    public String getMessage() {
        return String.format(MSG_FORMAT, value, min, max);
    }
}
