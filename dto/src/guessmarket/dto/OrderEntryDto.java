package guessmarket.dto;

// Represents one order still waiting in an option's book.
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
