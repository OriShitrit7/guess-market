package guessmarket.engine.exception.file;

import guessmarket.engine.exception.InvalidFileException;

public class InvalidInitialBalanceException extends InvalidFileException {
    private static final String MSG_FORMAT =
            "User '%s' has an initial balance of %.2f. The initial balance must be greater than 0.";

    private final String userName;
    private final double balance;

    public InvalidInitialBalanceException(String userName, double balance) {
        this.userName = userName;
        this.balance = balance;
    }

    @Override
    public String getMessage() {
        return String.format(MSG_FORMAT, userName, balance);
    }
}
