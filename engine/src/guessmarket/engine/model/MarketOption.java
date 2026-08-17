package guessmarket.engine.model;

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
}
