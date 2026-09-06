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
}
