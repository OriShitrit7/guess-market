package guessmarket.dto;

import java.util.List;

// Carries the general details of a market event from the engine to its clients.
public final class EventSummaryDto {
    private final int eventId;
    private final String eventName;
    private final String description;
    private final String marketMakerName;
    private final EventStatusDto status;
    private final TradingMethodTypeDto methodType;
    private final int commissionPercent;
    private final CommissionPolicyDto commissionPolicy;
    private final List<String> optionNames;
    private final double accountBalance;
    // What one winning share pays when the event closes: 1.00 in LMSR, d in Order Book.
    private final double winningSharePayout;
    private final double totalCommissionCollected;
    // The name of the option that won, or null while the event is not resolved.
    private final String winningOptionName;

    public EventSummaryDto(int eventId, String eventName, String description, String marketMakerName,
                           int commissionPercent,
                           CommissionPolicyDto commissionPolicy, List<String> optionNames,
                           EventStatusDto status, TradingMethodTypeDto methodType, double accountBalance,
                           double winningSharePayout, double totalCommissionCollected,
                           String winningOptionName) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.description = description;
        this.marketMakerName = marketMakerName;
        this.commissionPercent = commissionPercent;
        this.commissionPolicy = commissionPolicy;
        this.optionNames = List.copyOf(optionNames);
        this.status = status;
        this.methodType = methodType;
        this.accountBalance = accountBalance;
        this.winningSharePayout = winningSharePayout;
        this.totalCommissionCollected = totalCommissionCollected;
        this.winningOptionName = winningOptionName;
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

    public String getMarketMakerName() {
        return marketMakerName;
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

    public double getWinningSharePayout() {
        return winningSharePayout;
    }

    public double getTotalCommissionCollected() {
        return totalCommissionCollected;
    }

    public String getWinningOptionName() {
        return winningOptionName;
    }
}
