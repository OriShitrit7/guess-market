package guessmarket.engine.api;

public class OptionTradingInfo {
    private final String optionName;
    private final double currentValue;
    private final int totalSharesBought;

    public OptionTradingInfo(String optionName, double currentValue, int totalSharesBought) {
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
