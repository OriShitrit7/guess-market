package guessmarket.ui.console.exception;

// Signals that the console input stream ended before the user chose to exit.
// Unchecked because it cannot be recovered from by asking the user again.
public class InputClosedException extends RuntimeException {
    private static final String MESSAGE =
            "Input has ended unexpectedly. No more commands can be read, so the application is closing.";

    @Override
    public String getMessage() {
        return MESSAGE;
    }
}
