package guessmarket.engine.model;

// Represents one possible outcome of a market event and tracks its purchased shares.
public class MarketOption {
    private final String name;
    private int totalSharesBought;

    public MarketOption(String name) {
        this.name = name;
        this.totalSharesBought = 0;
    }

    public String getName() {
        return name;
    }

    public int getTotalSharesBought() {
        return totalSharesBought;
    }

    // Adds a validated positive quantity to the shares purchased for this option.
    void addShares(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0, but was " + quantity);
        }
        totalSharesBought += quantity;
    }
}
