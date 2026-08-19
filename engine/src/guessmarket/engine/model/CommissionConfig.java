package guessmarket.engine.model;

// Defines how and when commission is collected for a market event.
public class CommissionConfig {
    public static final int MIN_COMMISSION_PERCENT = 0;
    public static final int MAX_COMMISSION_PERCENT = 90;

    private static final double PERCENT_DIVISOR = 100.0;

    private final int commissionPercent;
    private final CommissionPolicy commissionPolicy;

    public CommissionConfig(int commissionPercent, CommissionPolicy commissionPolicy) {
        this.commissionPercent = commissionPercent;
        this.commissionPolicy = commissionPolicy;
    }

    public int getCommissionPercent() {
        return commissionPercent;
    }

    public CommissionPolicy getCommissionPolicy() {
        return commissionPolicy;
    }

    // Calculates the commission charged during a purchase when the policy requires it.
    public double calcPurchaseCommission(double sharesCost) {
        return commissionPolicy == CommissionPolicy.ON_PURCHASE ? calcCommission(sharesCost) : 0;
    }

    // Calculates the commission charged when an event closes when the policy requires it.
    public double calcCloseCommission(double winningInvestment) {
        return commissionPolicy == CommissionPolicy.ON_CLOSE ? calcCommission(winningInvestment) : 0;
    }

    private double calcCommission(double amount) {
        return amount * commissionPercent / PERCENT_DIVISOR;
    }
}
