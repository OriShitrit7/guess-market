package guessmarket.engine.trading;

public abstract class TradingMethod {
    public abstract double calcInitialSubsidy(int optionCount);
    public abstract double[] calcOptionsValues(int[] quantities);
    public abstract double calcSharesCost(int[] quantitiesBefore, int optionIndex, int sharesToBuy);
    public abstract double calcWinningPayout(int winningShares);
}
