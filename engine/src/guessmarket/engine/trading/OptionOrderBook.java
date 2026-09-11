package guessmarket.engine.trading;

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

    public void addOrder(Order order) {
        if (order.getAction() == OrderAction.BUY) {
            buyOrders.add(order);
            buyOrders.sort(BUY_ORDER_COMPARATOR);
        } else {
            sellOrders.add(order);
            sellOrders.sort(SELL_ORDER_COMPARATOR);
        }
    }

    public List<OrderMatch> submitOrder(Order incomingOrder, int optionIndex) {
        List<OrderMatch> matches = new ArrayList<>();
        List<Order> oppositeOrders = incomingOrder.getAction() == OrderAction.BUY
                ? sellOrders
                : buyOrders;

        while (!incomingOrder.isFilled() && !oppositeOrders.isEmpty()) {
            Order bestOppositeOrder = oppositeOrders.getFirst();

            if (!pricesMatch(incomingOrder, bestOppositeOrder)) {
                break;
            }

            int matchedQuantity = Math.min(incomingOrder.getRemainingQuantity(),
                    bestOppositeOrder.getRemainingQuantity());
            double executionPrice = bestOppositeOrder.getPricePerShare();

            matches.add(createMatch(incomingOrder, bestOppositeOrder, optionIndex,
                    matchedQuantity, executionPrice));
            lastTradePrice = executionPrice;

            incomingOrder.reduceRemainingQuantity(matchedQuantity);
            bestOppositeOrder.reduceRemainingQuantity(matchedQuantity);

            if (bestOppositeOrder.isFilled()) {
                oppositeOrders.removeFirst();
            }
        }

        if (!incomingOrder.isFilled()) {
            addOrder(incomingOrder);
        }

        return matches;
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

    public int getPendingSellQuantity(String userName) {
        return sellOrders.stream()
                .filter(order -> order.getUserName().equals(userName))
                .mapToInt(Order::getRemainingQuantity)
                .sum();
    }
}
