package guessmarket.engine.orderbook;

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
