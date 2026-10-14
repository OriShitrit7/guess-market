package guessmarket.engine.model;

import guessmarket.engine.orderbook.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

// Represents an Order Book event and manages its orders, matching, minting and price history.
public class OrderBookMarketEvent extends MarketEvent {
    // An order price must stay at least one cent below the base value.
    private static final double PRICE_MARGIN = 0.01;
    // Prices are rounded to two decimal places.
    private static final double PRICE_SCALE = 100;

    // Base value and payout of one winning share.
    private final int d;
    // Whether buy orders for opposite options may be paired to create new shares.
    private final boolean mintAllowed;

    // Funds provided by the market maker when opening the event.
    private final double initialInvestment;
    private final List<OptionOrderBook> books;

    // Preserves submission order when prices are equal.
    private long nextSequence;

    public OrderBookMarketEvent(int eventId, String eventName, String description, List<MarketOption> options,
                                CommissionConfig commissionConfig, int d, boolean mintAllowed,
                                double initialInvestment) {
        super(eventId, eventName, description, options, commissionConfig);
        this.d = d;
        this.mintAllowed = mintAllowed;
        this.initialInvestment = initialInvestment;
        this.books = IntStream.range(0, options.size())
                .mapToObj(index -> new OptionOrderBook(options.get(index), index))
                .toList();
    }

    public int getD() {
        return d;
    }

    public List<OptionOrderBook> getOptionBooks() {
        return books;
    }

    // Removes the user's waiting buy orders from every option book.
    @Override
    public boolean cancelBuyOrdersOf(String userName) {
        boolean removed = false;

        for (OptionOrderBook book : books) {
            removed |= book.removeBuyOrdersOf(userName); // Every book is checked, even after a removal
        }
        return removed;
    }

    @Override
    public TradingMethodType getMethodType() {
        return TradingMethodType.ORDER_BOOK;
    }

    // The market maker funds the event with the initial investment.
    @Override
    public double calcOpeningCost() {
        return initialInvestment;
    }

    // Calculates the payout using the event's base value.
    @Override
    public double calcWinningPayout(int winningShares) {
        return winningShares * (double) d;
    }

    // Converts the initial investment into an equal share quantity for each option.
    @Override
    public int calcInitialSharesPerOption() {
        return (int) (initialInvestment / d);
    }

    // Calculates the cost assigned to each initial option holding.
    @Override
    public double calcInitialAmountPerOption() {
        return calcInitialSharesPerOption() * (double) d / getOptionCount();
    }

    // Checks that an order price is finite and within the event's allowed range.
    public boolean isValidOrderPrice(double pricePerShare) {
        return Double.isFinite(pricePerShare) && pricePerShare > 0 && pricePerShare <= d - PRICE_MARGIN;
    }

    // Returns shares reserved by one user's waiting sell orders for the selected option.
    public int getPendingSellQuantity(String userName, int optionIndex) {
        return books.get(optionIndex).getPendingSellQuantity(userName);
    }

    // Creates an order with the next sequence number used for price-time priority.
    public Order createOrder(String userName, OrderAction action, int quantity, double pricePerShare) {
        return new Order(userName, action, quantity, pricePerShare, nextSequence++);
    }

    // Plans the matches without touching the book, so the caller can settle them first.
    public MatchPlan planMatches(Order order, int optionIndex) {
        List<OrderMatch> matches = books.get(optionIndex).findMatches(order); // Plan regular matches

        int matchedQuantity = matches.stream()
                .mapToInt(OrderMatch::getQuantity)
                .sum(); // Count shares already matched

        int quantityLeft = order.getRemainingQuantity() - matchedQuantity; // Find the unmatched quantity

        if (!mintAllowed || order.getAction() != OrderAction.BUY || quantityLeft == 0) {
            return new MatchPlan(matches, List.of()); // Skip minting when it cannot apply
        }
        return new MatchPlan(matches, findMintMatches(order, optionIndex, quantityLeft)); // Check for minting
    }

    private int getOppositeOptionIndex(int optionIndex) {
        return optionIndex == 0 ? 1 : 0;
    }

    // Pairs the buying order with the demand for the opposite option, best price first.
    private List<MintMatch> findMintMatches(Order incomingOrder, int optionIndex, int quantityLeft) {
        List<MintMatch> mints = new ArrayList<>();
        int oppositeOptionIndex = getOppositeOptionIndex(optionIndex); // Search the opposite option
        int remainingQuantity = quantityLeft;

        for (Order restingOrder : books.get(oppositeOptionIndex).getMatchableBuyOrders(incomingOrder.getUserName())) {
            double restingPrice = restingOrder.getPricePerShare();

            if (remainingQuantity == 0 || !checkMintThreshold(restingPrice, incomingOrder.getPricePerShare())) {
                break; // Later orders cannot meet the price threshold
            }
            int mintedQuantity = Math.min(remainingQuantity, restingOrder.getRemainingQuantity()); // Limit both orders

            mints.add(new MintMatch(restingOrder.getUserName(), incomingOrder.getUserName(),
                    oppositeOptionIndex, optionIndex, mintedQuantity,
                    restingPrice, calcComplementPrice(restingPrice)));
            remainingQuantity -= mintedQuantity;
        }
        return List.copyOf(mints);
    }

    // Two opposite buy orders can create a pair of shares only when together they cover the base value.
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

        book.completeMatches(order, plan.getMatches()); // Consume regular matches
        commitMints(order, optionIndex, plan.getMints()); // Apply cross-option mints

        if (!order.isFilled()) {
            book.addOrder(order); // Keep the unfilled quantity waiting
        }
    }

    // Minting consumes orders from both books, and gives both options their new shares.
    private void commitMints(Order order, int optionIndex, List<MintMatch> mints) {
        if (mints.isEmpty()) {
            return;
        }
        books.get(getOppositeOptionIndex(optionIndex)).completeMints(order, mints); // Consume opposite orders
        mints.forEach(mint -> books.get(optionIndex).recordTradePrice(mint.getIncomingPricePerShare())); // Record prices

        mints.forEach(this::mintShares); // Issue shares for both options
    }

    private void mintShares(MintMatch mint) {
        getOptions().get(mint.getRestingOptionIndex()).addShares(mint.getQuantity());
        getOptions().get(mint.getIncomingOptionIndex()).addShares(mint.getQuantity());
    }

    @Override
    protected void afterClose() {
        books.forEach(OptionOrderBook::clearOrders); // Cancel every waiting order
    }
}
