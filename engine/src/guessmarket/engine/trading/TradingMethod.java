package guessmarket.engine.trading;

// Defines the pricing, option value and payout calculations required by a trading method.
public abstract class TradingMethod {
    // Calculates the amount the market maker pays to open a market with the given number of options.
    public abstract double calcOpeningCost(int optionCount);

    public abstract int calcInitialSharesPerOption();

    // Calculates the amount paid for the winning shares when an event closes.
    public abstract double calcWinningPayout(int winningShares);

    // Identifies which trading method this implementation provides.
    public abstract TradingMethodType getType();
}
