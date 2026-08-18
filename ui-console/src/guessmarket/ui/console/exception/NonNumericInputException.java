package guessmarket.ui.console.exception;

public class NonNumericInputException extends InvalidInputException {
    private static final String MSG_FORMAT = "Invalid input: '%s' is not a whole number. Please try again.";

    private final String input;

    public NonNumericInputException(String input) {
        this.input = input;
    }

    @Override
    public String getMessage() {
        return String.format(MSG_FORMAT, input);
    }
}
