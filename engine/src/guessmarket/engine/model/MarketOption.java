package guessmarket.engine.model;

// Represents one possible event outcome and tracks the total shares issued for it.
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

    // Adds a validated positive quantity to the option's issued share total.
    void addShares(int quantity) {
        totalSharesBought += quantity;
    }
}
