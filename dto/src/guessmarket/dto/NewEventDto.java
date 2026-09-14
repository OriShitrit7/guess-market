package guessmarket.dto;

import java.util.List;

// Carries the details of an event a user asked to create. The fields of the trading method
// that was not chosen are ignored.
public final class NewEventDto {
    private final String eventName;
    private final String description;
    private final List<String> optionNames;
    private final int commissionPercent;
    private final CommissionPolicyDto commissionPolicy;
    private final TradingMethodTypeDto methodType;
    private final int b;
    private final int d;
    private final int initialInvestment;
    private final boolean allowMint;

    public NewEventDto(String eventName, String description, List<String> optionNames,
                       int commissionPercent, CommissionPolicyDto commissionPolicy,
                       TradingMethodTypeDto methodType, int b, int d, int initialInvestment,
                       boolean allowMint) {
        this.eventName = eventName;
        this.description = description;
        this.optionNames = List.copyOf(optionNames);
        this.commissionPercent = commissionPercent;
        this.commissionPolicy = commissionPolicy;
        this.methodType = methodType;
        this.b = b;
        this.d = d;
        this.initialInvestment = initialInvestment;
        this.allowMint = allowMint;
    }

    public String getEventName() {
        return eventName;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getOptionNames() {
        return optionNames;
    }

    public int getCommissionPercent() {
        return commissionPercent;
    }

    public CommissionPolicyDto getCommissionPolicy() {
        return commissionPolicy;
    }

    public TradingMethodTypeDto getMethodType() {
        return methodType;
    }

    public int getB() {
        return b;
    }

    public int getD() {
        return d;
    }

    public int getInitialInvestment() {
        return initialInvestment;
    }

    public boolean isAllowMint() {
        return allowMint;
    }
}
