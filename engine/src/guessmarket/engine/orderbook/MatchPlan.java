package guessmarket.engine.orderbook;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// Everything a submitted order would do, worked out in full before any money or share moves.
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

    // Every user standing on either side of this plan, which is who settlement may charge.
    public Set<String> getParticipantNames() {
        return Stream.concat(
                matches.stream().flatMap(match -> Stream.of(match.getBuyerName(), match.getSellerName())),
                mints.stream().flatMap(mint -> Stream.of(mint.getRestingBuyerName(), mint.getIncomingBuyerName())))
                .collect(Collectors.toCollection(LinkedHashSet::new));
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
