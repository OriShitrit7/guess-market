package guessmarket.engine.model;

import guessmarket.engine.orderbook.*;

import java.util.ArrayList;
import java.util.List;

public class OrderBookMarketEvent extends MarketEvent {
    // The highest legal order price is one cent below the base value.
    private static final double PRICE_MARGIN = 0.01;
    private static final double PRICE_SCALE = 100;

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
    public MatchPlan planMatches(Order order, int optionIndex) {
        List<OrderMatch> matches = books.get(optionIndex).findMatches(order, optionIndex);

        int matchedQuantity = matches.stream()
                .mapToInt(OrderMatch::getQuantity)
                .sum();

        int quantityLeft = order.getRemainingQuantity() - matchedQuantity;

        if (!mintAllowed || order.getAction() != OrderAction.BUY || quantityLeft == 0) {
            return new MatchPlan(matches, List.of());
        }
        return new MatchPlan(matches, findMintMatches(order, optionIndex, quantityLeft));
    }

    private int getOppositeOptionIndex(int optionIndex) {
        return optionIndex == 0 ? 1 : 0;
    }

    // Pairs the buying order with the demand for the opposite option, best price first.
    private List<MintMatch> findMintMatches(Order incomingOrder, int optionIndex, int quantityLeft) {
        List<MintMatch> mints = new ArrayList<>();
        int oppositeOptionIndex = getOppositeOptionIndex(optionIndex);
        int remainingQuantity = quantityLeft;

        for (Order restingOrder : books.get(oppositeOptionIndex).getBuyOrders()) {
            double restingPrice = restingOrder.getPricePerShare();

            if (remainingQuantity == 0 || !checkMintThreshold(restingPrice, incomingOrder.getPricePerShare())) {
                break;
            }
            int mintedQuantity = Math.min(remainingQuantity, restingOrder.getRemainingQuantity());

            mints.add(new MintMatch(restingOrder.getUserName(), incomingOrder.getUserName(),
                    oppositeOptionIndex, optionIndex, mintedQuantity,
                    restingPrice, calcComplementPrice(restingPrice)));
            remainingQuantity -= mintedQuantity;
        }
        return List.copyOf(mints);
    }

    private boolean checkMintThreshold(double restingPrice, double incomingPrice) {
        return restingPrice + incomingPrice >= d;
    }

    // The waiting order pays its full price, so the arriving one completes it to the base value.
    private double calcComplementPrice(double restingPrice) {
        return Math.round((d - restingPrice) * PRICE_SCALE) / PRICE_SCALE;
    }

    // Applies the settled plan to the books and rests whatever is left of the order.
    public void commitOrder(Order order, int optionIndex, MatchPlan plan) {
        OptionOrderBook book = books.get(optionIndex);

        book.completeMatches(order, plan.getMatches());
        tradeHistory.addAll(plan.getMatches());
        commitMints(order, optionIndex, plan.getMints());

        if (!order.isFilled()) {
            book.addOrder(order);
        }
    }

    // Minting consumes orders from both books, and gives both options their new shares.
    private void commitMints(Order order, int optionIndex, List<MintMatch> mints) {
        if (mints.isEmpty()) {
            return;
        }
        books.get(getOppositeOptionIndex(optionIndex)).completeMints(order, mints);
        mints.forEach(mint -> books.get(optionIndex).recordTradePrice(mint.getIncomingPricePerShare()));

        mints.forEach(this::mintShares);
    }

    private void mintShares(MintMatch mint) {
        getOptions().get(mint.getRestingOptionIndex()).addShares(mint.getQuantity());
        getOptions().get(mint.getIncomingOptionIndex()).addShares(mint.getQuantity());
    }

    @Override
    protected void afterClose() {
        books.forEach(OptionOrderBook::clearOrders);
    }
}
