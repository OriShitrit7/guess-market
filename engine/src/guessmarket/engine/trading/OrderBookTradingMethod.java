package guessmarket.engine.trading;

import guessmarket.engine.model.MarketOption;

import java.util.List;

public class OrderBookTradingMethod extends TradingMethod {
    private final int d;
    private final boolean mintAllowed;
    private final double initialInvestment;
    private final List<OptionOrderBook> books;
    private long nextSequence;

    public OrderBookTradingMethod(int d, boolean mintAllowed, double initialInvestment,
                                  List<MarketOption> options) {
        this.d = d;
        this.mintAllowed = mintAllowed;
        this.initialInvestment = initialInvestment;
        this.books = options.stream()
                .map(OptionOrderBook::new)
                .toList();
    }

    public int getD() {
        return d;
    }

    public boolean isMintAllowed() {
        return mintAllowed;
    }

    public double getInitialInvestment() {
        return initialInvestment;
    }

    public List<OptionOrderBook> getOptionBooks() {
        return books;
    }

    @Override
    public double calcOpeningCost(int optionCount) {
        return initialInvestment;
    }

    @Override
    public int calcInitialSharesPerOption() {
        return (int) (initialInvestment / d);
    }

    @Override
    public double calcWinningPayout(int winningShares) {
        return winningShares * d;
    }

    @Override
    public TradingMethodType getType() {
        return TradingMethodType.ORDER_BOOK;
    }

    public List<OrderMatch> submitOrder(String userName, int optionIndex, OrderAction action, int quantity, double pricePerShare) {
        OptionOrderBook book = books.get(optionIndex);
        Order order = new Order(userName, action, quantity, pricePerShare, nextSequence++);
        return book.submitOrder(order, optionIndex);
    }

    public boolean isValidOrderPrice(double pricePerShare) {
        return Double.isFinite(pricePerShare) && pricePerShare > 0 && pricePerShare <= d - 0.01;
    }

    public int getPendingSellQuantity(String userName, int optionIndex) {
        return books.get(optionIndex).getPendingSellQuantity(userName);
    }
}
