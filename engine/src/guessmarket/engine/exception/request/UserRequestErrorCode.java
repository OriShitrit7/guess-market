package guessmarket.engine.exception.request;

public enum UserRequestErrorCode {
    USER_NOT_FOUND("The selected user does not exist."),
    USER_BLOCKED("The selected user is blocked and cannot perform this action."),
    INSUFFICIENT_BALANCE("The user does not have enough money to perform this action."),
    INSUFFICIENT_SHARES("The user does not have enough available shares to submit this sell order."),
    USER_NAME_TAKEN("This user name is already taken. Please choose a different name."),
    USER_NAME_NOT_ENGLISH("The user name must be in English: letters, digits, spaces and punctuation only."),
    INVALID_LOAD_AMOUNT("The amount to load must be positive and have at most two decimal places.");

    private final String message;

    UserRequestErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
