package guessmarket.engine.orderbook;

// A trade where both sides buy opposite options, so nobody sells and the shares are created instead.
// Each side pays its own price, and the two prices together cover the event's base value d.
public class MintMatch {
    private final String restingBuyerName;
    private final String incomingBuyerName;
    private final int restingOptionIndex;
    private final int incomingOptionIndex;
    private final int quantity;
    private final double restingPricePerShare;
    private final double incomingPricePerShare;

    public MintMatch(String restingBuyerName, String incomingBuyerName, int restingOptionIndex,
                     int incomingOptionIndex, int quantity, double restingPricePerShare,
                     double incomingPricePerShare) {
        this.restingBuyerName = restingBuyerName;
        this.incomingBuyerName = incomingBuyerName;
        this.restingOptionIndex = restingOptionIndex;
        this.incomingOptionIndex = incomingOptionIndex;
        this.quantity = quantity;
        this.restingPricePerShare = restingPricePerShare;
        this.incomingPricePerShare = incomingPricePerShare;
    }

    public String getRestingBuyerName() {
        return restingBuyerName;
    }

    public String getIncomingBuyerName() {
        return incomingBuyerName;
    }

    public int getRestingOptionIndex() {
        return restingOptionIndex;
    }

    public int getIncomingOptionIndex() {
        return incomingOptionIndex;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getRestingPricePerShare() {
        return restingPricePerShare;
    }

    public double getIncomingPricePerShare() {
        return incomingPricePerShare;
    }

    public double getRestingTotalPaid() {
        return quantity * restingPricePerShare;
    }

    public double getIncomingTotalPaid() {
        return quantity * incomingPricePerShare;
    }
}
