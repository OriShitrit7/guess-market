package guessmarket.engine.orderbook;

// One trade between a buyer and a seller at a single price, created when two opposite orders meet.
public class OrderMatch {
    private final String buyerName;
    private final String sellerName;
    private final int optionIndex;
    private final int quantity;
    private final double pricePerShare;

    public OrderMatch(String buyerName, String sellerName, int optionIndex,
                      int quantity, double pricePerShare) {
        this.buyerName = buyerName;
        this.sellerName = sellerName;
        this.optionIndex = optionIndex;
        this.quantity = quantity;
        this.pricePerShare = pricePerShare;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public String getSellerName() {
        return sellerName;
    }

    public int getOptionIndex() {
        return optionIndex;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPricePerShare() {
        return pricePerShare;
    }

    public double getTotalPaid() {
        return quantity * pricePerShare;
    }
}
