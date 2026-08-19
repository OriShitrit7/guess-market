package guessmarket.dto;

// Carries the share and commission details of a completed purchase.
public final class TradeDto {
    private final String optionName;
    private final int quantity;
    // Cost of the purchased shares in dollars, excluding commission.
    private final double sharesCost;
    // Commission charged for the purchase in dollars.
    private final double commissionCost;

    public TradeDto(String optionName, int quantity, double sharesCost, double commissionCost) {
        this.optionName = optionName;
        this.quantity = quantity;
        this.sharesCost = sharesCost;
        this.commissionCost = commissionCost;
    }

    public String getOptionName() {
        return optionName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getSharesCost() {
        return sharesCost;
    }

    public double getCommissionCost() {
        return commissionCost;
    }
}
