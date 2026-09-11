package guessmarket.engine.orderbook;

import java.util.List;

// The completed submission result, expressed from the submitting user's perspective.
public final class OrderExecution {
    private final OrderAction action;
    private final List<OrderMatch> matches;
    private final int remainingQuantity;
    private final double commissionPaid;

    public OrderExecution(OrderAction action, List<OrderMatch> matches, int remainingQuantity, double commissionPaid) {
        this.action = action;
        this.matches = List.copyOf(matches);
        this.remainingQuantity = remainingQuantity;
        this.commissionPaid = commissionPaid;
    }

    public OrderAction getAction() {
        return action;
    }

    public List<OrderMatch> getMatches() {
        return matches;
    }

    public int getMatchedQuantity() {
        return matches.stream().mapToInt(OrderMatch::getQuantity).sum();
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    public double getTradeAmount() {
        return matches.stream().mapToDouble(OrderMatch::getTotalPaid).sum();
    }

    public double getCommissionPaid() {
        return commissionPaid;
    }
}
