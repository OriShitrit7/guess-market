package guessmarket.ui.console.exception;

public class EmptyInputException extends InvalidInputException {
    private static final String MSG_FORMAT = "Invalid input: no value was entered. Please try again.";

    @Override
    public String getMessage() {
        return MSG_FORMAT;
    }
}
