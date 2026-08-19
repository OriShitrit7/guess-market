package guessmarket.ui.console.exception;

public class EmptyInputException extends InvalidInputException {
    private static final String MESSAGE = "Invalid input: no value was entered. Please try again.";

    @Override
    public String getMessage() {
        return MESSAGE;
    }
}
