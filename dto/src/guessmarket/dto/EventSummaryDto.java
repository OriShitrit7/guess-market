package guessmarket.dto;

import java.util.List;

// Carries the general details of a market event from the engine to its clients.
public final class EventSummaryDto {
    private final int eventId;
    private final String eventName;
    private final String description;
    private final EventStatusDto status;
    private final TradingMethodTypeDto methodType;
    private final int commissionPercent;
    private final CommissionPolicyDto commissionPolicy;
    private final List<String> optionNames;
    private final double accountBalance;

    public EventSummaryDto(int eventId, String eventName, String description, int commissionPercent,
                           CommissionPolicyDto commissionPolicy, List<String> optionNames,
                           EventStatusDto status, TradingMethodTypeDto methodType, double accountBalance) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.description = description;
        this.commissionPercent = commissionPercent;
        this.commissionPolicy = commissionPolicy;
        this.optionNames = List.copyOf(optionNames);
        this.status = status;
        this.methodType = methodType;
        this.accountBalance = accountBalance;
    }

    public int getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public String getDescription() {
        return description;
    }

    public int getCommissionPercent() {
        return commissionPercent;
    }

    public CommissionPolicyDto getCommissionPolicy() {
        return commissionPolicy;
    }

    public List<String> getOptionNames() {
        return optionNames;
    }

    public EventStatusDto getStatus() {
        return status;
    }

    public TradingMethodTypeDto getMethodType() {
        return methodType;
    }

    public double getAccountBalance() {
        return accountBalance;
    }
}
