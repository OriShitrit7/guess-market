package guessmarket.engine.orderbook;

// The completed submission result, expressed from the submitting user's perspective.
public final class OrderExecution {
    private final OrderAction action;
    private final MatchPlan plan;
    private final int remainingQuantity;
    private final double commissionPaid;

    public OrderExecution(OrderAction action, MatchPlan plan, int remainingQuantity, double commissionPaid) {
        this.action = action;
        this.plan = plan;
        this.remainingQuantity = remainingQuantity;
        this.commissionPaid = commissionPaid;
    }

    public OrderAction getAction() {
        return action;
    }

    public int getMatchedQuantity() {
        return plan.getMatchedQuantity();
    }

    public int getMintedQuantity() {
        return plan.getMintedQuantity();
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    public double getMatchedAmount() {
        return plan.getMatchedAmount();
    }

    public double getMintedAmount() {
        return plan.getMintedAmount();
    }

    public double getCommissionPaid() {
        return commissionPaid;
    }
}
