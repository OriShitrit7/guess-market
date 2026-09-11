package guessmarket.dto;

// Represents one user's current holding in an event option.
public final class OptionHoldingDto {
    private final String optionName;
    private final int quantity;
    private final double amountPaid;

    public OptionHoldingDto(String optionName, int quantity, double amountPaid) {
        this.optionName = optionName;
        this.quantity = quantity;
        this.amountPaid = amountPaid;
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
}
