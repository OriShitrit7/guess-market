package guessmarket.engine.exception.request;

public enum EventRequestErrorCode {
    EVENT_NOT_FOUND("The selected event does not exist."),
    INVALID_EVENT_STATE("This action is not available in the event's current state."),
    UNAUTHORIZED_EVENT_ACTION("Only the event's market maker can perform this action."),
    OPTION_NOT_FOUND("The selected option does not exist for this event."),
    WRONG_TRADING_METHOD("This action is not available for this event's trading method."),
    INVALID_ORDER_ACTION("An order action must be selected."),
    INVALID_ORDER_PRICE("The order price must be greater than 0 and no greater than d - 0.01."),
    MISSING_EVENT_NAME("An event name is required."),
    EVENT_NAME_TAKEN("An event with this name already exists. Event names must be unique (case insensitive)."),
    MISSING_EVENT_DESCRIPTION("An event description is required."),
    MISSING_OPTION_NAME("Both options must be named."),
    TEXT_NOT_ENGLISH("The event name, description and option names must be in English: letters, digits, spaces and punctuation only."),
    DUPLICATE_OPTION_NAMES("The two options must have different names."),
    WRONG_OPTION_COUNT("An event must have exactly two options."),
    COMMISSION_OUT_OF_RANGE("The commission percent is outside the allowed range."),
    MISSING_COMMISSION_POLICY("A commission policy must be selected."),
    INVALID_LMSR_B("The LMSR b value must be greater than 0."),
    INVALID_BASE_VALUE("The base value d must be greater than 0."),
    INVALID_INITIAL_INVESTMENT("The initial investment cannot be negative."),
    MISSING_TRADING_METHOD("A trading method must be selected.");

    private final String message;

    EventRequestErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
