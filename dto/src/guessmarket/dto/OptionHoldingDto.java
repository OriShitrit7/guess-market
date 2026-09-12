package guessmarket.dto;

// Represents one user's current holding in an event option.
public final class OptionHoldingDto {
    private final String optionName;
    private final int quantity;
    private final double amountPaid;
    // What the holding is worth right now, or null while the option has no price.
    private final Double currentValue;

    public OptionHoldingDto(String optionName, int quantity, double amountPaid, Double currentValue) {
        this.optionName = optionName;
        this.quantity = quantity;
        this.amountPaid = amountPaid;
        this.currentValue = currentValue;
    }

    public String getOptionName() {
        return optionName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public Double getCurrentValue() {
        return currentValue;
    }
}
