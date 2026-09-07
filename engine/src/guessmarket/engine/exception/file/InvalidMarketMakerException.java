package guessmarket.engine.exception.file;

import guessmarket.engine.exception.InvalidFileException;

public class InvalidMarketMakerException extends InvalidFileException {
    private static final String UNKNOWN_EVENT_FORMAT =
            "User '%s' is defined as the market maker of event %d, which does not exist in the file.";
    private static final String ALREADY_ASSIGNED_FORMAT =
            "Event '%s' already has market maker '%s', so user '%s' cannot be its market maker too.";
    private static final String MISSING_FORMAT =
            "Event '%s' has no market maker. Every event must have exactly one.";

    private enum Reason { UNKNOWN_EVENT, ALREADY_ASSIGNED, MISSING }

    private final Reason reason;
    private final String userName;
    private final int eventId;
    private final String eventName;
    private final String existingMarketMakerName;

    public InvalidMarketMakerException(String userName, int eventId) {
        this.reason = Reason.UNKNOWN_EVENT;
        this.userName = userName;
        this.eventId = eventId;
        this.eventName = null;
        this.existingMarketMakerName = null;
    }

    public InvalidMarketMakerException(String eventName, String existingMarketMakerName, String userName) {
        this.reason = Reason.ALREADY_ASSIGNED;
        this.eventName = eventName;
        this.existingMarketMakerName = existingMarketMakerName;
        this.userName = userName;
        this.eventId = 0;
    }

    public InvalidMarketMakerException(String eventName) {
        this.reason = Reason.MISSING;
        this.eventName = eventName;
        this.userName = null;
        this.eventId = 0;
        this.existingMarketMakerName = null;
    }

    @Override
    public String getMessage() {
        return switch (reason) {
            case UNKNOWN_EVENT -> String.format(UNKNOWN_EVENT_FORMAT, userName, eventId);
            case ALREADY_ASSIGNED ->
                    String.format(ALREADY_ASSIGNED_FORMAT, eventName, existingMarketMakerName, userName);
            case MISSING -> String.format(MISSING_FORMAT, eventName);
        };
    }
}
