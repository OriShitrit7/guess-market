package guessmarket.engine.orderbook;

import java.util.List;

public final class MatchPlan {
    private final List<OrderMatch> matches;
    private final List<MintMatch> mints;

    public MatchPlan(List<OrderMatch> matches, List<MintMatch> mints) {
        this.matches = List.copyOf(matches);
        this.mints = List.copyOf(mints);
    }

    public List<OrderMatch> getMatches() {
        return matches;
    }

    public List<MintMatch> getMints() {
        return mints;
    }

    public int getMatchedQuantity() {
        return matches.stream().mapToInt(OrderMatch::getQuantity).sum();
    }

    public int getMintedQuantity() {
        return mints.stream().mapToInt(MintMatch::getQuantity).sum();
    }

    public double getMatchedAmount() {
        return matches.stream().mapToDouble(OrderMatch::getTotalPaid).sum();
    }

    public double getMintedAmount() {
        return mints.stream().mapToDouble(MintMatch::getIncomingTotalPaid).sum();
    }
}
