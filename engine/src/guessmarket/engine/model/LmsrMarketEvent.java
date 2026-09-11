package guessmarket.engine.model;

import java.util.ArrayList;
import java.util.List;

public class LmsrMarketEvent extends MarketEvent {

    // Each winning share pays one dollar when the event closes.
    private static final double WINNING_SHARE_VALUE = 1.0;

    // Positive LMSR liquidity parameter that controls price sensitivity.
    private final int b;
    private final List<Trade> tradeHistory = new ArrayList<>();

    public LmsrMarketEvent(int eventId, String eventName, String description, List<MarketOption> options,
                           CommissionConfig commissionConfig, int b) {
        super(eventId, eventName, description, options, commissionConfig);
        this.b = b;
    }

    @Override
    public TradingMethodType getMethodType() {
        return TradingMethodType.LMSR;
    }

    // Calculates the initial subsidy C(0) = b * ln(optionCount).
    @Override
    public double calcOpeningCost() {
        return calcCost(new int[getOptionCount()]);
    }

    @Override
    public int calcInitialSharesPerOption() {
        return 0;
    }

    @Override
    public double calcInitialAmountPerOption() {
        return 0;
    }

    public List<Trade> getTradeHistory() {
        return List.copyOf(tradeHistory);
    }

    public double[] getOptionsValues() {
        return calcOptionsValues(getSharesCountPerOption());
    }

    // Prices the purchase and records it. The caller moves the money.
    public Trade buyShares(String buyerName, int optionIndex, int quantity) {
        MarketOption option = getOptions().get(optionIndex);
        double sharesCost = calcSharesCost(getSharesCountPerOption(), optionIndex, quantity);
        Trade trade = new Trade(buyerName, option, quantity, sharesCost, calcPurchaseCommission(sharesCost));

        option.addShares(quantity);
        tradeHistory.add(trade);
        return trade;
    }

    private int[] getSharesCountPerOption() {
        return getOptions().stream().mapToInt(MarketOption::getTotalSharesBought).toArray();
    }

    // Calculates the payout using the fixed value of one dollar per winning share.
    @Override
    public double calcWinningPayout(int winningShares) {
        return winningShares * WINNING_SHARE_VALUE;
    }

    // Calculates normalized option values from exp(q_i / b).
    // Each value is between zero and one, and all values sum to one.
    public double[] calcOptionsValues(int[] quantities) {
        double maximumExponent = findMaximumExponent(quantities);
        double[] values = calcExponents(quantities, maximumExponent);
        double sum = sumOf(values);

        for (int i = 0; i < values.length; i++) {
            values[i] /= sum;
        }
        return values;
    }

    // Calculates C(q) = b * ln(sum(exp(q_i / b))) in a numerically stable form.
    private double calcCost(int[] quantities) {
        double maximumExponent = findMaximumExponent(quantities);
        double[] exponents = calcExponents(quantities, maximumExponent);

        // Adding the maximum back after the logarithm preserves the original LMSR formula.
        return b * (maximumExponent + Math.log(sumOf(exponents)));
    }

    // Calculates a purchase price as the difference between the market cost before and after it.
    private double calcSharesCost(int[] quantitiesBefore, int optionIndex, int sharesToBuy) {
        int[] quantitiesAfter = quantitiesBefore.clone();
        quantitiesAfter[optionIndex] += sharesToBuy;

        return calcCost(quantitiesAfter) - calcCost(quantitiesBefore);
    }

    // Calculates exponential values relative to the largest q_i / b value.
    private double[] calcExponents(int[] quantities, double maximumExponent) {
        double[] exponents = new double[quantities.length];

        for (int i = 0; i < quantities.length; i++) {
            double exponent = quantities[i] / (double) b;
            // Subtracting the same maximum preserves the ratios while preventing overflow.
            exponents[i] = Math.exp(exponent - maximumExponent);
        }
        return exponents;
    }

    // Finds the reference exponent used to stabilize all exponential calculations.
    private double findMaximumExponent(int[] quantities) {
        double maximumExponent = Double.NEGATIVE_INFINITY;

        for (int quantity : quantities) {
            double exponent = quantity / (double) b;
            maximumExponent = Math.max(maximumExponent, exponent);
        }
        return maximumExponent;
    }

    private double sumOf(double[] values) {
        double total = 0;

        for (double value : values) {
            total += value;
        }
        return total;
    }
}
