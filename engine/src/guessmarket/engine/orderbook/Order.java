package guessmarket.engine.orderbook;

public class Order {
    private final String userName;
    private final OrderAction action;
    private int remainingQuantity;
    private final double pricePerShare;
    private final long sequence;

    public Order(String userName, OrderAction action, int quantity, double pricePerShare, long sequence) {
        this.userName = userName;
        this.action = action;
        this.remainingQuantity = quantity;
        this.pricePerShare = pricePerShare;
        this.sequence = sequence;
    }

    public String getUserName() {
        return userName;
    }

    public OrderAction getAction() {
        return action;
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    public double getPricePerShare() {
        return pricePerShare;
    }

    public long getSequence() {
        return sequence;
    }

    void reduceRemainingQuantity(int quantity) {
        remainingQuantity -= quantity;
    }

    public boolean isFilled() {
        return remainingQuantity == 0;
    }
}
