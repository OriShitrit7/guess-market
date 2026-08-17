package guessmarket.engine.exception;

public class UnknownCommissionTypeException extends InvalidFileException {
    private static final String MSG_FORMAT =
            "Event '%s': the 'type' attribute of <comision> has an unsupported value: '%s'. "
                    + "Expected 'on-purchase' or 'on-close'.";

    private final String eventName;
    private final String value;

    public UnknownCommissionTypeException(String eventName, String value) {
        this.eventName = eventName;
        this.value = value;
    }

    @Override
    public String getMessage() {
        return String.format(MSG_FORMAT, eventName, value);
    }
}
