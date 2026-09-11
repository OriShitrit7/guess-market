package guessmarket.engine.model;

public class OptionHolding {
    private int quantity;
    private double amountPaid;

    public int getQuantity() {
        return quantity;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    void add(int quantity, double amountPaid) {
        this.quantity += quantity;
        this.amountPaid += amountPaid;
    }

    // Removes shares at their average cost, so the remaining amount still reflects what they cost.
    void remove(int quantity) {
        if (quantity > this.quantity) {
            throw new IllegalArgumentException("Cannot sell more shares than the user holds.");
        }
        amountPaid -= amountPaid * quantity / this.quantity;
        this.quantity -= quantity;
    }
}
