package guessmarket.engine.model;

// Defines how and when commission is collected for a market event.
public class CommissionConfig {
    public static final int MIN_COMMISSION_PERCENT = 0;
    public static final int MAX_COMMISSION_PERCENT = 90;

    private final int commissionPercentage;
    private final CommissionPolicy commissionPolicy;

    public CommissionConfig(int commissionPercent, CommissionPolicy commissionPolicy) {
        this.commissionPercentage = commissionPercent;
        this.commissionPolicy = commissionPolicy;
    }

    public int getPercentage() {
        return commissionPercentage;
    }

    public CommissionPolicy getCommissionPolicy() {
        return commissionPolicy;
    }

    private double calcCommission(double amount) {
        return amount * commissionPercentage / 100.0;
    }

    // Calculates the commission charged during a purchase when the policy requires it.
    public double calcPurchaseCommission(double sharesCost) {
        return commissionPolicy == CommissionPolicy.ON_PURCHASE ? calcCommission(sharesCost) : 0;
    }

    // Calculates the commission charged when an event closes when the policy requires it.
    public double calcCloseCommission(double winningInvestment) {
        return commissionPolicy == CommissionPolicy.ON_CLOSE ? calcCommission(winningInvestment) : 0;
    }
}
