package guessmarket.engine.model;

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

    public double calcPurchaseCommission(double sharesCost) {
        return commissionPolicy == CommissionPolicy.ON_PURCHASE ? calcCommission(sharesCost) : 0;
    }

    public double calcCloseCommission(double winningInvestment) {
        return commissionPolicy == CommissionPolicy.ON_CLOSE ? calcCommission(winningInvestment) : 0;
    }
}
