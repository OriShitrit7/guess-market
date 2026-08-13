package guessmarket.engine.trading;

public class LmsrTradingMethod extends TradingMethod {

    private final int b;

    public LmsrTradingMethod(int b) {
        if (b <= 0) {                // ++++++++ /////
            throw new IllegalArgumentException("b must be greater than 0, but was " + b);
        }
        this.b = b;
    }

    public double calcInitialSubsidy(int optionCount) {
        return calcCost(new int[optionCount]);
    }

    public double[] calcOptionsValues(int[] quantities) {
        double maximumExponent = findMaximumExponent(quantities);
        double[] values = calcExponents(quantities, maximumExponent);
        double sum = sumOf(values);

        for (int i = 0; i < values.length; i++) {
            values[i] /= sum;
        }
        return values;
    }

    private double calcCost(int[] quantities) {
        double maximumExponent = findMaximumExponent(quantities);
        double[] exponents = calcExponents(quantities, maximumExponent);

        return b * (maximumExponent + Math.log(sumOf(exponents)));
    }

    public double calcSharesCost(int[] quantitiesBefore, int optionIndex, int sharesToBuy) {
        int[] quantitiesAfter = quantitiesBefore.clone();
        quantitiesAfter[optionIndex] += sharesToBuy;

        return calcCost(quantitiesAfter) - calcCost(quantitiesBefore);
    }

    private double[] calcExponents(int[] quantities, double maximumExponent) {
        double[] exponents = new double[quantities.length];

        for (int i = 0; i < quantities.length; i++) {
            double exponent = quantities[i] / (double) b;
            exponents[i] = Math.exp(exponent - maximumExponent);
        }
        return exponents;
    }

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
