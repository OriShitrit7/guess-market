package guessmarket.engine.exception;

public class InvalidLiquidityException extends InvalidFileException {
    private static final String MSG_FORMAT =
            "Event '%s': the <b> value %d is invalid. It must be greater than 0.";

    private final String eventName;
    private final int liquidityParameter;

    public InvalidLiquidityException(String eventName, int liquidityParameter) {
        this.eventName = eventName;
        this.liquidityParameter = liquidityParameter;
    }

    @Override
    public String getMessage() {
        return String.format(MSG_FORMAT, eventName, liquidityParameter);
    }
}
