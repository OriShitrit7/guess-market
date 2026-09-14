package guessmarket.dto;

import java.util.List;

// Carries the general event details displayed in event lists and summary sections in the UI.
public final class EventSummaryDto {
    private final int eventId;
    private final String eventName;
    private final String description;
    private final String marketMakerName;
    private final EventStatusDto status;
    private final TradingMethodTypeDto methodType;

    // The commission rate as a whole-number percentage.
    private final int commissionPercent;
    private final CommissionPolicyDto commissionPolicy;

    private final List<String> optionNames;
    private final double accountBalance;

    // What the market maker is charged when the event is opened.
    private final double openingCost;
    // What one winning share pays when the event closes: 1.00 in LMSR, d in Order Book.
    private final double winningSharePayout;
    private final double totalCommissionCollected;
    // The name of the option that won, or null while the event is not resolved.
    private final String winningOptionName;

    public EventSummaryDto(int eventId, String eventName, String description, String marketMakerName,
                           int commissionPercent,
                           CommissionPolicyDto commissionPolicy, List<String> optionNames,
                           EventStatusDto status, TradingMethodTypeDto methodType, double accountBalance,
                           double openingCost, double winningSharePayout, double totalCommissionCollected,
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
        this.openingCost = openingCost;
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

    public double getOpeningCost() {
        return openingCost;
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
