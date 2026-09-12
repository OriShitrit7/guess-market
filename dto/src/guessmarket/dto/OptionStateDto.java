package guessmarket.dto;

// Carries the current value and purchased share total of one market option.
public final class OptionStateDto {
    private final String optionName;
    // Current market value between zero and one.
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
