package guessmarket.engine.exception;

import guessmarket.engine.model.CommissionConfig;

public class CommissionOutOfRangeException extends InvalidFileException {
    private static final String MSG_FORMAT =
            "Event '%s': the commission is %d%%. The allowed range is %d%% to %d%%, inclusive.";

    private final String eventName;
    private final int commissionPercent;

    public CommissionOutOfRangeException(String eventName, int commissionPercent) {
        this.eventName = eventName;
        this.commissionPercent = commissionPercent;
    }

    @Override
    public String getMessage() {
        return String.format(MSG_FORMAT, eventName, commissionPercent,
                CommissionConfig.MIN_COMMISSION_PERCENT, CommissionConfig.MAX_COMMISSION_PERCENT);
    }
}
