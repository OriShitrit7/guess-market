package guessmarket.engine.exception.request;

public enum EventRequestErrorCode {
    EVENT_NOT_FOUND("The selected event does not exist."),
    INVALID_EVENT_STATE("This action is not available in the event's current state."),
    UNAUTHORIZED_EVENT_ACTION("Only the event's market maker can perform this action."),
    OPTION_NOT_FOUND("The selected option does not exist for this event."),
    WRONG_TRADING_METHOD("This action is not available for this event's trading method."),
    INVALID_ORDER_ACTION("An order action must be selected."),
    INVALID_ORDER_PRICE("The order price must be greater than 0 and no greater than d - 0.01.");

    private final String message;

    EventRequestErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
