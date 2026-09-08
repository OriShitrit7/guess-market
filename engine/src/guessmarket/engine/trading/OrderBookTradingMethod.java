package guessmarket.engine.trading;

public class OrderBookTradingMethod extends TradingMethod {
    private final int d;
    private final boolean mint;
    private double initialSubsidy;   // נראה לי שלא צריך את זה, זה פשוט היתרה של הMM

    public OrderBookTradingMethod(int d, boolean mint, double initialSubsidy) {
        this.d = d;
        this.mint = mint;
        this.initialSubsidy = initialSubsidy;
    }

    public int getD() {
        return d;
    }

    public boolean isMint() {
        return mint;
    }

    @Override
    public double calcInitialSubsidy(int optionCount) {
        return initialSubsidy;
    }

    @Override
    public double[] calcOptionsValues(int[] quantities) {
        return new double[0];
    }

    @Override
    public double calcSharesCost(int[] quantitiesBefore, int optionIndex, int sharesToBuy) {
        return 0;
    }

    @Override
    public double calcWinningPayout(int winningShares) {
        return 0;
    }

    @Override
    public TradingMethodType getType() {
        return TradingMethodType.ORDER_BOOK;
    }
}
