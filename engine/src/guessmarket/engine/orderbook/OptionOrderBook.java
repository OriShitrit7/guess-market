package guessmarket.engine.orderbook;

import guessmarket.engine.model.MarketOption;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Maintains both sides of one option's book, plans and completes matches, and records trade prices.
public class OptionOrderBook {
    private final MarketOption option;
    private final int optionIndex; // Zero-based position in the event, stored in every match
    private final List<Order> buyOrders = new ArrayList<>();
    private final List<Order> sellOrders = new ArrayList<>();
    private final List<Double> priceHistory = new ArrayList<>(); // Execution prices in chronological order
    private Double lastTradePrice; // Null until the first execution

    // Buyers are ordered by the highest price first and sellers by the lowest, and whenever two
    // prices are equal the order that arrived earlier comes first.
    private static final Comparator<Order> BUY_ORDER_COMPARATOR =
            Comparator.comparingDouble(Order::getPricePerShare).reversed()
                    .thenComparingLong(Order::getSequence);

    private static final Comparator<Order> SELL_ORDER_COMPARATOR =
            Comparator.comparingDouble(Order::getPricePerShare)
                    .thenComparingLong(Order::getSequence);

    public OptionOrderBook(MarketOption option, int optionIndex) {
        this.option = option;
        this.optionIndex = optionIndex;
    }

    public MarketOption getOption() {
        return option;
    }

    // Drops the waiting buy orders of a user whose account was blocked, and reports whether any were dropped.
    public boolean removeBuyOrdersOf(String userName) {
        return buyOrders.removeIf(order -> order.getUserName().equals(userName));
    }

    public List<Order> getBuyOrders() {
        return List.copyOf(buyOrders);
    }

    public List<Order> getSellOrders() {
        return List.copyOf(sellOrders);
    }

    public Double getLastTradePrice() {
        return lastTradePrice;
    }

    public List<Double> getPriceHistory() {
        return List.copyOf(priceHistory);
    }

    // The highest price anyone is currently willing to pay, or null when no one is buying.
    public Double getBid() {
        return buyOrders.isEmpty() ? null : buyOrders.getFirst().getPricePerShare();
    }

    // The lowest price anyone is currently willing to sell at, or null when no one is selling.
    public Double getAsk() {
        return sellOrders.isEmpty() ? null : sellOrders.getFirst().getPricePerShare();
    }

    // The midpoint between BID and ASK, available only while both sides have orders.
    public Double getMid() {
        Double bid = getBid();
        Double ask = getAsk();

        return bid == null || ask == null ? null : (bid + ask) / 2;
    }

    // The gap between ASK and BID, available only while both sides have orders.
    public Double getSpread() {
        Double bid = getBid();
        Double ask = getAsk();

        return bid == null || ask == null ? null : ask - bid;
    }

    // Adds an unfilled order to the correct side and restores price-time priority.
    public void addOrder(Order order) {
        if (order.getAction() == OrderAction.BUY) { // Highest price gets priority
            buyOrders.add(order);
            buyOrders.sort(BUY_ORDER_COMPARATOR);
        } else { // Lowest price gets priority
            sellOrders.add(order);
            sellOrders.sort(SELL_ORDER_COMPARATOR);
        }
    }

    // Returns other users' waiting buy orders in their current priority order.
    public List<Order> getMatchableBuyOrders(String userName) {
        return matchableOrders(buyOrders, userName);
    }

    // Preserves book order while excluding the incoming user's own orders.
    private List<Order> matchableOrders(List<Order> orders, String userName) {
        return orders.stream().filter(order -> !order.getUserName().equals(userName)).toList();
    }

    private List<Order> oppositeOrders(Order incomingOrder) {
        return incomingOrder.getAction() == OrderAction.BUY ? sellOrders : buyOrders;
    }

    // Plans all possible matches without changing orders, the book or recorded prices.
    public List<OrderMatch> findMatches(Order incomingOrder) {
        List<OrderMatch> matches = new ArrayList<>();
        int remainingQuantity = incomingOrder.getRemainingQuantity(); // Simulate the unfilled quantity

        for (Order bestOppositeOrder : matchableOrders(oppositeOrders(incomingOrder), incomingOrder.getUserName())) { // Follow price-time priority
            if (remainingQuantity == 0 || !pricesMatch(incomingOrder, bestOppositeOrder)) {
                break; // Later orders have less competitive prices
            }

            int matchedQuantity = Math.min(remainingQuantity, bestOppositeOrder.getRemainingQuantity()); // Limit both orders
            double executionPrice = bestOppositeOrder.getPricePerShare(); // Resting order sets the price

            matches.add(createMatch(incomingOrder, bestOppositeOrder, matchedQuantity, executionPrice)); // Plan the trade
            remainingQuantity -= matchedQuantity; // Continue with the unfilled part
        }
        return List.copyOf(matches);
    }

    // Applies a settled match plan by consuming both orders and recording execution prices.
    public void completeMatches(Order incomingOrder, List<OrderMatch> matches) {
        List<Order> oppositeOrders = oppositeOrders(incomingOrder); // Select the matched side
        List<Order> matchable = matchableOrders(oppositeOrders, incomingOrder.getUserName()); // Recreate the planned view
        int index = 0;

        for (OrderMatch match : matches) {
            Order bestOppositeOrder = matchable.get(index++); // Follow the original match plan

            incomingOrder.reduceRemainingQuantity(match.getQuantity()); // Consume the incoming order
            bestOppositeOrder.reduceRemainingQuantity(match.getQuantity()); // Consume the waiting order
            lastTradePrice = match.getPricePerShare(); // Update the current market price
            priceHistory.add(lastTradePrice); // Record the execution

            if (bestOppositeOrder.isFilled()) {
                oppositeOrders.remove(bestOppositeOrder); // Remove a completed order
            }
        }
    }

    // Applies settled mints by consuming both buy orders and recording this option's prices.
    public void completeMints(Order incomingOrder, List<MintMatch> mints) {
        List<Order> matchable = getMatchableBuyOrders(incomingOrder.getUserName()); // Recreate the planned view
        int index = 0;

        for (MintMatch mint : mints) {
            Order restingOrder = matchable.get(index++); // Follow the original mint plan

            incomingOrder.reduceRemainingQuantity(mint.getQuantity()); // Consume the incoming buy
            restingOrder.reduceRemainingQuantity(mint.getQuantity()); // Consume the waiting buy
            lastTradePrice = mint.getRestingPricePerShare(); // Use this option's execution price
            priceHistory.add(lastTradePrice); // Record the mint execution

            if (restingOrder.isFilled()) {
                buyOrders.remove(restingOrder); // Remove a completed order
            }
        }
    }

    // Records the incoming side of a mint because only the opposite book consumes a waiting order.
    public void recordTradePrice(double price) {
        lastTradePrice = price;
        priceHistory.add(price);
    }

    // A closed event pays out from holdings alone, so nothing is left to match.
    public void clearOrders() {
        buyOrders.clear();
        sellOrders.clear();
    }

    // Checks whether the incoming limit accepts the best price on the opposite side.
    private boolean pricesMatch(Order incomingOrder, Order bestOppositeOrder) {
        if (incomingOrder.getAction() == OrderAction.BUY) {
            return incomingOrder.getPricePerShare() >= bestOppositeOrder.getPricePerShare();
        }
        return incomingOrder.getPricePerShare() <= bestOppositeOrder.getPricePerShare();
    }

    // The book stores both sides, so which of the two orders is the buyer depends on the incoming one.
    private OrderMatch createMatch(Order incomingOrder, Order bestOppositeOrder,
                                   int quantity, double executionPrice) {
        String buyerName;
        String sellerName;

        if (incomingOrder.getAction() == OrderAction.BUY) { // Incoming user is the buyer
            buyerName = incomingOrder.getUserName();
            sellerName = bestOppositeOrder.getUserName();
        } else { // Incoming user is the seller
            buyerName = bestOppositeOrder.getUserName();
            sellerName = incomingOrder.getUserName();
        }

        return new OrderMatch(buyerName, sellerName, optionIndex, quantity, executionPrice);
    }

    // Shares this user already promised to sell, which a new sell order may not offer again.
    public int getPendingSellQuantity(String userName) {
        return sellOrders.stream()
                .filter(order -> order.getUserName().equals(userName))
                .mapToInt(Order::getRemainingQuantity)
                .sum();
    }
}
