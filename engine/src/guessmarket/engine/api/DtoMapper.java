package guessmarket.engine.api;

import guessmarket.dto.*;
import guessmarket.engine.model.*;
import guessmarket.engine.orderbook.OptionOrderBook;
import guessmarket.engine.orderbook.Order;
import guessmarket.engine.orderbook.OrderExecution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

public class DtoMapper {

    EventSummaryDto createEventSummary(MarketEvent event) {
        List<String> optionNames = event.getOptions().stream()
                .map(MarketOption::getName)
                .toList();
        MarketOption winner = event.getWinningOption();

        return new EventSummaryDto(event.getEventId(), event.getEventName(), event.getDescription(),
                event.getMarketMakerName(), event.getCommissionPercent(), toDto(event.getCommissionPolicy()), optionNames,
                toDto(event.getStatus()), toDto(event.getMethodType()), event.getAccount().getBalance(),
                event.calcOpeningCost(), event.calcWinningPayout(1),
                event.getAccount().getTotalCommissionCollected(),
                winner == null ? null : winner.getName());
    }

    EventTradingStateDto createEventTradingState(MarketEvent event, Collection<User> users) {
        return new EventTradingStateDto(createEventSummary(event), createOptionStates(event),
                createSharePriceHistory(event), createTradeDtos(getLmsrTrades(event)),
                createParticipants(event, users));
    }

    OrderBookStateDto createOrderBookState(OrderBookMarketEvent event, Collection<User> users) {
        return new OrderBookStateDto(createEventSummary(event), createOptionBooks(event),
                createOrderBookPriceHistory(event), createOptionStates(event),
                createParticipants(event, users));
    }

    public UserSummaryDto createUserSummary(User user) {
        return new UserSummaryDto(user.getName(), user.getBalance(), user.isBlocked());
    }

    List<BalancePointDto> createBalanceHistory(User user) {
        List<Double> balances = user.getBalanceHistory();
        List<BalancePointDto> history = new ArrayList<>();

        for (int i = 0; i < balances.size(); i++) {
            history.add(new BalancePointDto(i, balances.get(i)));
        }
        return List.copyOf(history);
    }

    UserInvolvementDto createUserInvolvement(MarketEvent event, User user) {
        List<Trade> userTrades = getLmsrTrades(event).stream()
                .filter(trade -> trade.getBuyerName().equals(user.getName()))
                .toList();

        Position position = user.getPositions().get(event.getEventId());
        boolean finalProfitLossAvailable = event.getStatus() == EventStatus.CLOSED;
        return new UserInvolvementDto(createTradeDtos(userTrades), createHoldingDtos(event, position),
                position == null ? 0 : position.getTotalCommissionPaid(), finalProfitLossAvailable,
                position == null ? 0 : position.calcProfitLoss());
    }

    EventParticipationDto createEventParticipation(MarketEvent event, User user) {
        return new EventParticipationDto(createEventSummary(event), event.isMarketMaker(user.getName()),
                user.getPositions().containsKey(event.getEventId()));
    }

    OrderResultDto createOrderResult(OrderExecution execution) {
        return new OrderResultDto(OrderActionDto.valueOf(execution.getAction().name()),
                execution.getMatchedQuantity(), execution.getMintedQuantity(),
                execution.getRemainingQuantity(), execution.getMatchedAmount(),
                execution.getMintedAmount(), execution.getCommissionPaid());
    }

    // The existing UI DTO represents LMSR purchases only; OB details will receive their own DTO later.
    private List<Trade> getLmsrTrades(MarketEvent event) {
        return event instanceof LmsrMarketEvent lmsrEvent ? lmsrEvent.getTradeHistory() : List.of();
    }

    private List<OptionBookDto> createOptionBooks(OrderBookMarketEvent event) {
        return event.getOptionBooks().stream()
                .map(this::createOptionBook)
                .toList();
    }

    private List<List<Double>> createOrderBookPriceHistory(OrderBookMarketEvent event) {
        return event.getOptionBooks().stream()
                .map(OptionOrderBook::getPriceHistory)
                .toList();
    }

    private OptionBookDto createOptionBook(OptionOrderBook book) {
        return new OptionBookDto(book.getOption().getName(), createOrderEntries(book.getBuyOrders()),
                createOrderEntries(book.getSellOrders()), book.getLastTradePrice(), book.getBid(),
                book.getAsk(), book.getMid(), book.getSpread());
    }

    private List<OrderEntryDto> createOrderEntries(List<Order> orders) {
        return orders.stream()
                .map(order -> new OrderEntryDto(order.getUserName(), order.getRemainingQuantity(),
                        order.getPricePerShare()))
                .toList();
    }

    // Anyone holding shares or waiting with an order counts as a participant.
    private List<ParticipantHoldingsDto> createParticipants(MarketEvent event, Collection<User> users) {
        return users.stream()
                .filter(user -> user.getPositions().containsKey(event.getEventId()))
                .map(user -> new ParticipantHoldingsDto(user.getName(),
                        createHoldingDtos(event, user.getPositions().get(event.getEventId())),
                        countOpenOrders(event, user.getName())))
                .toList();
    }

    private int countOpenOrders(MarketEvent event, String userName) {
        if (!(event instanceof OrderBookMarketEvent orderBookEvent)) {
            return 0;
        }
        return orderBookEvent.getOptionBooks().stream()
                .mapToInt(book -> countOrdersOf(book.getBuyOrders(), userName)
                        + countOrdersOf(book.getSellOrders(), userName))
                .sum();
    }

    private int countOrdersOf(List<Order> orders, String userName) {
        return (int) orders.stream()
                .filter(order -> order.getUserName().equals(userName))
                .count();
    }

    private List<OptionHoldingDto> createHoldingDtos(MarketEvent event, Position position) {
        Map<MarketOption, Double> values = optionValues(event);

        return event.getOptions().stream().map(option -> {
            OptionHolding holding = position == null ? null : position.getHoldings().get(option);
            int quantity = holding == null ? 0 : holding.getQuantity();
            double amountPaid = holding == null ? 0 : holding.getAmountPaid();
            Double price = values.get(option);

            return new OptionHoldingDto(option.getName(), quantity, amountPaid,
                    price == null ? null : price * quantity);
        }).toList();
    }

    // The price each option is currently worth: the LMSR formula, or the order book's MID.
    private Map<MarketOption, Double> optionValues(MarketEvent event) {
        Map<MarketOption, Double> values = new LinkedHashMap<>();
        List<MarketOption> options = event.getOptions();

        if (event instanceof LmsrMarketEvent lmsrEvent) {
            double[] lmsrValues = lmsrEvent.getOptionsValues();

            for (int i = 0; i < options.size(); i++) {
                values.put(options.get(i), lmsrValues[i]);
            }
            return values;
        }
        if (event instanceof OrderBookMarketEvent orderBookEvent) {
            orderBookEvent.getOptionBooks().forEach(book -> values.put(book.getOption(), book.getMid()));
        }
        return values;
    }

    private List<OptionStateDto> createOptionStates(MarketEvent event) {
        Map<MarketOption, Double> values = optionValues(event);

        return event.getOptions().stream()
                .map(option -> new OptionStateDto(option.getName(), values.get(option),
                        option.getTotalSharesBought()))
                .toList();
    }

    // Replays LMSR purchases in chronological order and records both option values after each one.
    private List<List<Double>> createSharePriceHistory(MarketEvent event) {
        if (!(event instanceof LmsrMarketEvent lmsrEvent)) {
            return List.of();
        }

        List<MarketOption> options = event.getOptions();
        List<List<Double>> history = options.stream()
                .<List<Double>>map(option -> new ArrayList<>())
                .toList();
        int[] quantities = new int[options.size()];

        addSharePriceSnapshot(history, lmsrEvent, quantities);

        for (Trade trade : lmsrEvent.getTradeHistory()) {
            int optionIndex = options.indexOf(trade.getOption());

            if (optionIndex >= 0) {
                quantities[optionIndex] += trade.getQuantity();
                addSharePriceSnapshot(history, lmsrEvent, quantities);
            }
        }
        return history.stream()
                .map(List::copyOf)
                .toList();
    }

    private void addSharePriceSnapshot(List<List<Double>> history, LmsrMarketEvent event,
                                       int[] quantities) {
        double[] prices = event.calcOptionsValues(quantities);

        for (int i = 0; i < history.size(); i++) {
            history.get(i).add(prices[i]);
        }
    }


    // Converts trade history to DTOs in reverse order so the newest purchases appear first.
    private List<TradeDto> createTradeDtos(List<Trade> trades) {
        List<TradeDto> tradeDtos = new ArrayList<>();

        for (int i = trades.size() - 1; i >= 0; i--) {
            tradeDtos.add(createTradeDto(trades.get(i)));
        }
        return tradeDtos;
    }

    TradeDto createTradeDto(Trade trade) {
        return new TradeDto(trade.getBuyerName(), trade.getOption().getName(), trade.getQuantity(),
                trade.getSharesCost(), trade.getCommissionCost());
    }

    private CommissionPolicyDto toDto(CommissionPolicy policy) {
        return switch (policy) {
            case ON_PURCHASE -> CommissionPolicyDto.ON_PURCHASE;
            case ON_CLOSE -> CommissionPolicyDto.ON_CLOSE;
        };
    }

    private TradingMethodTypeDto toDto(TradingMethodType type) {
        return switch (type) {
            case LMSR -> TradingMethodTypeDto.LMSR;
            case ORDER_BOOK -> TradingMethodTypeDto.ORDER_BOOK;
        };
    }

    private EventStatusDto toDto(EventStatus status) {
        return switch (status) {
            case NOT_STARTED -> EventStatusDto.NOT_STARTED;
            case ACTIVE -> EventStatusDto.ACTIVE;
            case CLOSED -> EventStatusDto.CLOSED;
        };
    }
}
