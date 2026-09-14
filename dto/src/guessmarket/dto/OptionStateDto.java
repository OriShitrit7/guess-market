package guessmarket.dto;

// Carries an option's current price and share total for event details and trading controls.
public final class OptionStateDto {
    private final String optionName;
    // The current price of one share, or null when an Order Book option has no market price.
    private final Double currentValue;
    private final int totalSharesBought;

    public OptionStateDto(String optionName, Double currentValue, int totalSharesBought) {
        this.optionName = optionName;
        this.currentValue = currentValue;
        this.totalSharesBought = totalSharesBought;
    }

    public String getOptionName() {
        return optionName;
    }

    public Double getCurrentValue() {
        return currentValue;
    }

    public int getTotalSharesBought() {
        return totalSharesBought;
    }
}
