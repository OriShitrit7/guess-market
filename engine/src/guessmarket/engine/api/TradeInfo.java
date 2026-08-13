package guessmarket.engine.api;

public class TradeInfo {
    private final String optionName;
    private final int quantity;
    private final double sharesCost;
    private final double commissionCost;

    public TradeInfo(String optionName, int quantity, double sharesCost, double commissionCost) {
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
