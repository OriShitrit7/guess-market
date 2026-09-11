package guessmarket.engine.orderbook;

import guessmarket.engine.model.MarketOption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OptionOrderBook {
    private final MarketOption option;
    private final List<Order> buyOrders = new ArrayList<>();
    private final List<Order> sellOrders = new ArrayList<>();
    private Double lastTradePrice;

    private static final Comparator<Order> BUY_ORDER_COMPARATOR =
            Comparator.comparingDouble(Order::getPricePerShare).reversed()
                    .thenComparingLong(Order::getSequence);

    private static final Comparator<Order> SELL_ORDER_COMPARATOR =
            Comparator.comparingDouble(Order::getPricePerShare)
                    .thenComparingLong(Order::getSequence);

    public OptionOrderBook(MarketOption option) {
        this.option = option;
    }

    public MarketOption getOption() {
        return option;
    }

    public List<Order> getBuyOrders() {
        return Collections.unmodifiableList(buyOrders);
    }

    public List<Order> getSellOrders() {
        return Collections.unmodifiableList(sellOrders);
    }

    public Double getLastTradePrice() {
        return lastTradePrice;
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

    public void addOrder(Order order) {
        if (order.getAction() == OrderAction.BUY) {
            buyOrders.add(order);
            buyOrders.sort(BUY_ORDER_COMPARATOR);
        } else {
            sellOrders.add(order);
            sellOrders.sort(SELL_ORDER_COMPARATOR);
        }
    }

    public void getEventData() {

    }

    // Plans matches without changing orders, the book, or LAST.
    public List<OrderMatch> findMatches(Order incomingOrder, int optionIndex) {
        List<OrderMatch> matches = new ArrayList<>();
        List<Order> oppositeOrders = incomingOrder.getAction() == OrderAction.BUY
                ? sellOrders
                : buyOrders;
        int remainingQuantity = incomingOrder.getRemainingQuantity();

        for (Order bestOppositeOrder : oppositeOrders) {
            if (remainingQuantity == 0 || !pricesMatch(incomingOrder, bestOppositeOrder)) {
                break;
            }

            int matchedQuantity = Math.min(remainingQuantity, bestOppositeOrder.getRemainingQuantity());
            double executionPrice = bestOppositeOrder.getPricePerShare();

            matches.add(createMatch(incomingOrder, bestOppositeOrder, optionIndex,
                    matchedQuantity, executionPrice));
            remainingQuantity -= matchedQuantity;
        }
        return List.copyOf(matches);
    }

    // Called by the event only after the planned matches have been settled successfully.
    public void completeMatches(Order incomingOrder, List<OrderMatch> matches) {
        List<Order> oppositeOrders = incomingOrder.getAction() == OrderAction.BUY ? sellOrders : buyOrders;
        for (OrderMatch match : matches) {
            Order bestOppositeOrder = oppositeOrders.getFirst();
            incomingOrder.reduceRemainingQuantity(match.getQuantity());
            bestOppositeOrder.reduceRemainingQuantity(match.getQuantity());
            lastTradePrice = match.getPricePerShare();

            if (bestOppositeOrder.isFilled()) {
                oppositeOrders.removeFirst();
            }
        }
    }

    public void clearOrders() {
        buyOrders.clear();
        sellOrders.clear();
    }

    private boolean pricesMatch(Order incomingOrder, Order bestOppositeOrder) {
        if (incomingOrder.getAction() == OrderAction.BUY) {
            return incomingOrder.getPricePerShare() >= bestOppositeOrder.getPricePerShare();
        }
        return incomingOrder.getPricePerShare() <= bestOppositeOrder.getPricePerShare();
    }

    private OrderMatch createMatch(Order incomingOrder, Order bestOppositeOrder, int optionIndex,
                                   int quantity, double executionPrice) {
        String buyerName;
        String sellerName;

        if (incomingOrder.getAction() == OrderAction.BUY) {
            buyerName = incomingOrder.getUserName();
            sellerName = bestOppositeOrder.getUserName();
        } else {
            buyerName = bestOppositeOrder.getUserName();
            sellerName = incomingOrder.getUserName();
        }

        return new OrderMatch(buyerName, sellerName, optionIndex, quantity, executionPrice);
    }

    public long getPendingSellQuantity(String userName) {
        return sellOrders.stream()
                .filter(order -> order.getUserName().equals(userName))
                .mapToLong(Order::getRemainingQuantity)
                .sum();
    }
}
