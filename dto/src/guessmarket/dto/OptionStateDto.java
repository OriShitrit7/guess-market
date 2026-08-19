package guessmarket.dto;

// Carries the current value and purchased share total of one market option.
public final class OptionStateDto {
    private final String optionName;
    // Current market value between zero and one.
    private final double currentValue;
    private final int totalSharesBought;

    public OptionStateDto(String optionName, double currentValue, int totalSharesBought) {
        this.optionName = optionName;
        this.currentValue = currentValue;
        this.totalSharesBought = totalSharesBought;
    }

    public String getOptionName() {
        return optionName;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public int getTotalSharesBought() {
        return totalSharesBought;
    }
}
