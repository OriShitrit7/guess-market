package guessmarket.engine.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Position {
    private final Map<MarketOption, OptionHolding> holdings = new LinkedHashMap<>();
    private double totalCommissionPaid;

    public Map<MarketOption, OptionHolding> getHoldings() {
        return Collections.unmodifiableMap(holdings);
    }

    public double getTotalCommissionPaid() {
        return totalCommissionPaid;
    }

    public int getPositionQuantityByOption(MarketOption option) {
        if (holdings.containsKey(option)) {
            return holdings.get(option).getQuantity();
        }
        return 0;
    }

    void recordPurchase(Trade trade) {
        addShares(trade.getOption(), trade.getQuantity(), trade.getSharesCost());
        recordCommission(trade.getCommissionCost());
    }

    void addShares(MarketOption option, int quantity, double amountPaid) {
        holdings.computeIfAbsent(option, ignored -> new OptionHolding()).add(quantity, amountPaid);
    }

    void removeShares(MarketOption option, int quantity) {
        OptionHolding holding = holdings.get(option);

        if (holding == null) {
            throw new IllegalArgumentException("The user does not hold shares of this option.");
        }
        holding.remove(quantity);
    }

    void recordCommission(double amount) {
        totalCommissionPaid += amount;
    }
}
