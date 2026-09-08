package guessmarket.ui.javafx.components;

import guessmarket.dto.CommissionPolicyDto;
import guessmarket.dto.EventStatusDto;
import guessmarket.dto.TradingMethodTypeDto;

public abstract class DisplayText {

    public static String of(EventStatusDto status) {
        return switch (status) {
            case NOT_STARTED -> "Not Started";
            case ACTIVE -> "Active";
            case CLOSED -> "Closed";
        };
    }

    public static String of(TradingMethodTypeDto methodType) {
        return switch (methodType) {
            case LMSR -> "LMSR";
            case ORDER_BOOK -> "Order Book";
        };
    }

    public static String of(CommissionPolicyDto policy) {
        return switch (policy) {
            case ON_CLOSE -> "On Close";
            case ON_PURCHASE -> "On Purchase";
        };
    }
}
