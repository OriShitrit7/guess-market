package guessmarket.dto;

// Carries one waiting order for the order book display and the user's pending orders table.
public final class OrderEntryDto {
    private final String userName;
    private final int quantity;
    private final double pricePerShare;

    public OrderEntryDto(String userName, int quantity, double pricePerShare) {
        this.userName = userName;
        this.quantity = quantity;
        this.pricePerShare = pricePerShare;
    }

    public String getUserName() {
        return userName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPricePerShare() {
        return pricePerShare;
    }
}
