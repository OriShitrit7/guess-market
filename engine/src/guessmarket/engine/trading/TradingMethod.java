package guessmarket.engine.trading;

public abstract class TradingMethod {
    public abstract double[] calcOptionsValues(int[] quantities);
}
