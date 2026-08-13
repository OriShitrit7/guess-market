package guessmarket.engine.model;

public class CommissionConfig {
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
}
