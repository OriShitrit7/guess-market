package guessmarket.engine.trading;

// Implements the LMSR formulas used to price shares and calculate option values.
// Shifted exponents keep the calculations stable for large share quantities.
public class LmsrTradingMethod extends TradingMethod {

    // Each winning share pays one dollar when the event closes.
    private static final double WINNING_SHARE_VALUE = 1.0;

    // Positive LMSR liquidity parameter that controls price sensitivity.
    private final int b;

    public LmsrTradingMethod(int b) {
        if (b <= 0) {
            throw new IllegalArgumentException("b must be greater than 0, but was " + b);
        }
        this.b = b;
    }

    // Calculates the initial subsidy C(0) = b * ln(optionCount).
    @Override
    public double calcInitialSubsidy(int optionCount) {
        return calcCost(new int[optionCount]);
    }

    // Calculates normalized option values from exp(q_i / b).
    // Each value is between zero and one, and all values sum to one.
    @Override
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
    @Override
    public double calcSharesCost(int[] quantitiesBefore, int optionIndex, int sharesToBuy) {
        int[] quantitiesAfter = quantitiesBefore.clone();
        quantitiesAfter[optionIndex] += sharesToBuy;

        return calcCost(quantitiesAfter) - calcCost(quantitiesBefore);
    }

    // Calculates the payout using the fixed value of one dollar per winning share.
    @Override
    public double calcWinningPayout(int winningShares) {
        return winningShares * WINNING_SHARE_VALUE;
    }

    // Calculates exponentials relative to the largest q_i / b value.
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
