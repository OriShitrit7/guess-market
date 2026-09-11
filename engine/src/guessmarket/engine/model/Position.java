package guessmarket.engine.model;

import guessmarket.engine.exception.request.InvalidUserRequestException;
import guessmarket.engine.exception.request.UserRequestErrorCode;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Position {
    private final Map<MarketOption, OptionHolding> holdings = new LinkedHashMap<>();
    private double totalCommissionPaid;
    private double totalPurchaseCost;
    private double totalSaleProceeds;
    private double totalPayoutReceived;

    public Map<MarketOption, OptionHolding> getHoldings() {
        return Collections.unmodifiableMap(holdings);
    }

    public double getTotalCommissionPaid() {
        return totalCommissionPaid;
    }

    public double getTotalPurchaseCost() {
        return totalPurchaseCost;
    }

    public double getTotalSaleProceeds() {
        return totalSaleProceeds;
    }

    public double getTotalPayoutReceived() {
        return totalPayoutReceived;
    }

    public double calcProfitLoss() {
        return totalSaleProceeds + totalPayoutReceived - totalPurchaseCost - totalCommissionPaid;
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
        totalPurchaseCost = totalPurchaseCost + amountPaid;
    }

    void removeShares(MarketOption option, int quantity) {
        OptionHolding holding = holdings.get(option);

        if (holding == null) {
            throw new InvalidUserRequestException(UserRequestErrorCode.INSUFFICIENT_SHARES);
        }
        holding.remove(quantity);
    }

    void recordCommission(double amount) {
        totalCommissionPaid = totalCommissionPaid + amount;
    }

    void recordSale(double amount) {
        totalSaleProceeds = totalSaleProceeds + amount;
    }

    void recordPayout(double amount) {
        totalPayoutReceived = totalPayoutReceived + amount;
    }
}
