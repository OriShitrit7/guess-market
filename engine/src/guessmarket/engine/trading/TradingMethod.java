package guessmarket.engine.trading;

// Defines the pricing, option value and payout calculations required by a trading method.
public abstract class TradingMethod {
    // Calculates the subsidy needed to create a market with the given number of options.
    public abstract double calcInitialSubsidy(int optionCount);

    // Calculates the current value of each option from its purchased share quantity.
    public abstract double[] calcOptionsValues(int[] quantities);

    // Calculates the cost of adding shares to one option without changing the supplied quantities.
    public abstract double calcSharesCost(int[] quantitiesBefore, int optionIndex, int sharesToBuy);

    // Calculates the amount paid for the winning shares when an event closes.
    public abstract double calcWinningPayout(int winningShares);
}
