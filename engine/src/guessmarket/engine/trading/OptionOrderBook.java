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
            Order restingOrder = oppositeOrders.getFirst();

            if (!pricesMatch(incomingOrder, restingOrder)) {
                break;
            }

            int matchedQuantity = Math.min(incomingOrder.getRemainingQuantity(),
                    restingOrder.getRemainingQuantity());
            double executionPrice = restingOrder.getPricePerShare();

            matches.add(createMatch(incomingOrder, restingOrder, optionIndex,
                    matchedQuantity, executionPrice));
            lastTradePrice = executionPrice;

            incomingOrder.reduceRemainingQuantity(matchedQuantity);
            restingOrder.reduceRemainingQuantity(matchedQuantity);

            if (restingOrder.isFilled()) {
                oppositeOrders.removeFirst();
            }
        }

        if (!incomingOrder.isFilled()) {
            addOrder(incomingOrder);
        }

        return matches;
    }

    private boolean pricesMatch(Order incomingOrder, Order restingOrder) {
        if (incomingOrder.getAction() == OrderAction.BUY) {
            return incomingOrder.getPricePerShare() >= restingOrder.getPricePerShare();
        }
        return incomingOrder.getPricePerShare() <= restingOrder.getPricePerShare();
    }

    private OrderMatch createMatch(Order incomingOrder, Order restingOrder, int optionIndex,
                                   int quantity, double executionPrice) {
        String buyerName;
        String sellerName;

        if (incomingOrder.getAction() == OrderAction.BUY) {
            buyerName = incomingOrder.getUserName();
            sellerName = restingOrder.getUserName();
        } else {
            buyerName = restingOrder.getUserName();
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
