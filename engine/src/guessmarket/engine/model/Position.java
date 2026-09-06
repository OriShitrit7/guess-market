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

    void recordPurchase(Trade trade) {
        holdings.computeIfAbsent(trade.getOption(), option -> new OptionHolding())
                .add(trade.getQuantity(), trade.getSharesCost());
        recordCommission(trade.getCommissionCost());
    }

    void recordCommission(double amount) {
        totalCommissionPaid += amount;
    }
}
