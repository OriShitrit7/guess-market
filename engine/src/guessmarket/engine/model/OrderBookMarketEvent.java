package guessmarket.engine.model;

import guessmarket.engine.orderbook.Order;
import guessmarket.engine.orderbook.OrderAction;
import guessmarket.engine.orderbook.OptionOrderBook;
import guessmarket.engine.orderbook.OrderMatch;

import java.util.ArrayList;
import java.util.List;

public class OrderBookMarketEvent extends MarketEvent {
    // The highest legal order price is one cent below the base value.
    private static final double PRICE_MARGIN = 0.01;

    private final int d;
    private final boolean mintAllowed;
    private final double initialInvestment;
    private final List<OptionOrderBook> books;
    private final List<OrderMatch> tradeHistory = new ArrayList<>();
    private long nextSequence;

    public OrderBookMarketEvent(int eventId, String eventName, String description, List<MarketOption> options,
                                CommissionConfig commissionConfig, int d, boolean mintAllowed,
                                double initialInvestment) {
        super(eventId, eventName, description, options, commissionConfig);
        this.d = d;
        this.mintAllowed = mintAllowed;
        this.initialInvestment = initialInvestment;
        this.books = options.stream().map(OptionOrderBook::new).toList();
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

    public List<OrderMatch> getTradeHistory() {
        return List.copyOf(tradeHistory);
    }

    @Override
    public TradingMethodType getMethodType() {
        return TradingMethodType.ORDER_BOOK;
    }

    @Override
    public double calcOpeningCost() {
        return initialInvestment;
    }

    @Override
    public double calcWinningPayout(int winningShares) {
        return winningShares * (double) d;
    }

    @Override
    public int calcInitialSharesPerOption() {
        return (int) (initialInvestment / d);
    }

    @Override
    public double calcInitialAmountPerOption() {
        return calcInitialSharesPerOption() * (double) d / getOptionCount();
    }

    public boolean isValidOrderPrice(double pricePerShare) {
        return Double.isFinite(pricePerShare) && pricePerShare > 0 && pricePerShare <= d - PRICE_MARGIN;
    }

    public long getPendingSellQuantity(String userName, int optionIndex) {
        return books.get(optionIndex).getPendingSellQuantity(userName);
    }

    public Order createOrder(String userName, OrderAction action, int quantity, double pricePerShare) {
        return new Order(userName, action, quantity, pricePerShare, nextSequence++);
    }

    // Plans the matches without touching the book, so the caller can settle them first.
    public List<OrderMatch> findMatches(Order order, int optionIndex) {
        return books.get(optionIndex).findMatches(order, optionIndex);
    }

    // Applies the settled matches to the book and rests whatever is left of the order.
    public void commitOrder(Order order, int optionIndex, List<OrderMatch> matches) {
        OptionOrderBook book = books.get(optionIndex);

        book.completeMatches(order, matches);
        tradeHistory.addAll(matches);

        if (!order.isFilled()) {
            book.addOrder(order);
        }
    }

    @Override
    protected void afterClose() {
        books.forEach(OptionOrderBook::clearOrders);
    }
}
