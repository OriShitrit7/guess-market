package guessmarket.engine.model;

// Represents a completed share purchase, including its share cost and commission.
public class Trade {
    private final String buyerName;
    private final MarketOption option;
    private final int quantity;
    private final double sharesCost;
    private final double commissionCost;

    public Trade(String buyerName, MarketOption option, int quantity, double sharesCost, double commissionCost) {
        this.buyerName = buyerName;
        this.option = option;
        this.quantity = quantity;
        this.sharesCost = sharesCost;
        this.commissionCost = commissionCost;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public MarketOption getOption() {
        return option;
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
