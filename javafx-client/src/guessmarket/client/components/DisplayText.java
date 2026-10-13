package guessmarket.client.components;

import guessmarket.dto.AccountTransactionTypeDto;
import guessmarket.dto.CommissionPolicyDto;
import guessmarket.dto.EventStatusDto;
import guessmarket.dto.TradingMethodTypeDto;

// Formats domain values into consistent text used throughout the UI.
public abstract class DisplayText {
    private static final String AMOUNT_FORMAT = "$%.2f";
    private static final String NEGATIVE_AMOUNT_FORMAT = "-$%.2f";
    private static final String POSITIVE_AMOUNT_FORMAT = "+$%.2f";
    private static final String NO_AMOUNT = "-";

    // Every amount of money in the UI goes through here, so the currency sign lives in one place.
    public static String amount(double value) {
        return value < 0 ? String.format(NEGATIVE_AMOUNT_FORMAT, -value)
                : String.format(AMOUNT_FORMAT, value);
    }

    // An amount that may be missing, such as a price in an empty order book.
    public static String optionalAmount(Double value) {
        return value == null ? NO_AMOUNT : amount(value);
    }

    // A profit or loss, which is only meaningful together with its sign.
    public static String signedAmount(double value) {
        return value < 0 ? amount(value) : String.format(POSITIVE_AMOUNT_FORMAT, value);
    }

    // Converts an event status to its user-facing label.
    public static String of(EventStatusDto status) {
        return switch (status) {
            case NOT_STARTED -> "Not Started";
            case ACTIVE -> "Active";
            case CLOSED -> "Closed";
        };
    }

    // Converts a trading method to its user-facing label.
    public static String of(TradingMethodTypeDto methodType) {
        return switch (methodType) {
            case LMSR -> "LMSR";
            case ORDER_BOOK -> "Order Book";
        };
    }

    // Converts a commission policy to its user-facing label.
    public static String of(CommissionPolicyDto policy) {
        return switch (policy) {
            case ON_CLOSE -> "On Close";
            case ON_PURCHASE -> "On Purchase";
        };
    }

    public static String of(AccountTransactionTypeDto type) {
        return switch (type) {
            case LOAD_FUNDS -> "Funds Loaded";
            case EVENT_OPENING -> "Event Opening";
            case SHARES_PURCHASE -> "Shares Purchase";
            case SHARES_SALE -> "Shares Sale";
            case COMMISSION_RECEIVED -> "Commission Received";
            case WINNING_PAYOUT -> "Winning Payout";
            case EVENT_CLOSING -> "Event Closing";
        };
    }
}
