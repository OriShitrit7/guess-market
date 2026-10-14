package guessmarket.engine.api;

import guessmarket.dto.*;
import guessmarket.engine.chat.ChatMessage;
import guessmarket.engine.model.*;
import guessmarket.engine.orderbook.OptionOrderBook;
import guessmarket.engine.orderbook.Order;
import guessmarket.engine.orderbook.OrderExecution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

// Converts internal model objects into detached DTO snapshots that are safe to hand to the engine's callers.
public class DtoMapper {

    // Creates the general event information shared by event lists and detail views.
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

    // Builds the state snapshot that matches the event's trading method.
    EventStateDto createEventState(MarketEvent event, Collection<User> users) {
        if (event instanceof LmsrMarketEvent lmsrEvent) {
            return createLmsrState(lmsrEvent, users);
        }
        return createOrderBookState((OrderBookMarketEvent) event, users);
    }

    // Builds an LMSR event snapshot. Its history is built separately, by createEventHistory.
    LmsrStateDto createLmsrState(LmsrMarketEvent event, Collection<User> users) {
        return new LmsrStateDto(createEventSummary(event), createOptionStates(event), createParticipants(event, users));
    }

    // Builds the books and participant data needed by the Order Book views. Its history is built separately.
    OrderBookStateDto createOrderBookState(OrderBookMarketEvent event, Collection<User> users) {
        return new OrderBookStateDto(createEventSummary(event), createOptionStates(event), createParticipants(event, users),
                createOptionBooks(event));
    }

    // Builds only the part of the event's history that comes after the counts the caller already holds:
    // the trades from tradesFrom, and for each option the price points from its entry in pricesFrom.
    EventHistoryDto createEventHistory(MarketEvent event, int tradesFrom, List<Integer> pricesFrom) {
        List<List<Double>> prices = event instanceof LmsrMarketEvent lmsrEvent
                ? createSharePriceHistory(lmsrEvent)
                : createOrderBookPriceHistory((OrderBookMarketEvent) event);
        List<List<Double>> newPrices = new ArrayList<>();

        for (int i = 0; i < prices.size(); i++) {
            newPrices.add(itemsFrom(prices.get(i), i < pricesFrom.size() ? pricesFrom.get(i) : 0));
        }
        List<TradeDto> newTrades = itemsFrom(getLmsrTrades(event), tradesFrom).stream()
                .map(this::createTradeDto)
                .toList();

        return new EventHistoryDto(newTrades, newPrices);
    }

    // An index outside the list returns all of it, as with account transactions.
    private <T> List<T> itemsFrom(List<T> items, int from) {
        int first = from < 0 || from > items.size() ? 0 : from;

        return items.subList(first, items.size());
    }

    // Creates the account summary displayed for one user.
    public UserSummaryDto createUserSummary(User user, boolean marketMaker) {
        return new UserSummaryDto(user.getName(), user.getBalance(), user.isBlocked(), marketMaker);
    }

    // Collects one user's trades, holdings, commission and final result in an event.
    UserInvolvementDto createUserInvolvement(MarketEvent event, User user) {
        List<Trade> userTrades = getLmsrTrades(event).stream()
                .filter(trade -> trade.getBuyerName().equals(user.getName()))
                .toList();

        Position position = user.getPositions().get(event.getEventId());
        return new UserInvolvementDto(createTradeDtos(userTrades), createHoldingDtos(event, position),
                position == null ? 0 : position.getTotalCommissionPaid(),
                position == null ? 0 : position.calcProfitLoss());
    }

    // Describes whether the user owns or participates in an event for the user events table.
    EventParticipationDto createEventParticipation(MarketEvent event, User user) {
        return new EventParticipationDto(createEventSummary(event), event.isMarketMaker(user.getName()),
                user.getPositions().containsKey(event.getEventId()));
    }

    // Builds the account view of one user. Only the transactions from the given index onward are included;
    // an index outside the list returns all of them.
    AccountDetailsDto createAccountDetails(User user, List<EventParticipationDto> events, int fromTransaction) {
        List<AccountTransaction> transactions = user.getTransactions();
        int firstNewTransaction = fromTransaction < 0 || fromTransaction > transactions.size() ? 0 : fromTransaction;

        List<AccountTransactionDto> newTransactions = transactions.subList(firstNewTransaction, transactions.size()).stream()
                .map(this::createAccountTransaction)
                .toList();
        boolean marketMaker = events.stream().anyMatch(EventParticipationDto::isMarketMaker);
        return new AccountDetailsDto(createUserSummary(user, marketMaker), newTransactions, events);
    }

    private AccountTransactionDto createAccountTransaction(AccountTransaction transaction) {
        return new AccountTransactionDto(toDto(transaction.getType()), transaction.getAmount(),
                transaction.getBalanceAfter(), transaction.getOccurredAt().toEpochMilli());
    }

    // Summarizes the matched, minted and waiting portions of an executed order.
    OrderResultDto createOrderResult(OrderExecution execution, double balanceAfter) {
        return new OrderResultDto(OrderActionDto.valueOf(execution.getAction().name()),
                execution.getMatchedQuantity(), execution.getMintedQuantity(),
                execution.getRemainingQuantity(), execution.getMatchedAmount(),
                execution.getMintedAmount(), execution.getCommissionPaid(), balanceAfter);
    }

    // Only LMSR events keep a trade history, so an Order Book event has no trades to list.
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

    // Anyone with a position, including one created only for a waiting order, counts as a participant.
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

    // Lists the holding in every option of the event, valued at the option's current price when one exists.
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
    private List<List<Double>> createSharePriceHistory(LmsrMarketEvent event) {
        List<MarketOption> options = event.getOptions();
        List<List<Double>> history = new ArrayList<>();
        int[] quantities = new int[options.size()];

        for (int i = 0; i < options.size(); i++) {
            history.add(new ArrayList<>());
        }

        addSharePriceSnapshot(history, event, quantities);

        for (Trade trade : event.getTradeHistory()) {
            int optionIndex = options.indexOf(trade.getOption());

            if (optionIndex >= 0) {
                quantities[optionIndex] += trade.getQuantity();
                addSharePriceSnapshot(history, event, quantities);
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

    // Converts a chat message into the form sent to the clients.
    public ChatMessageDto createChatMessage(ChatMessage message) {
        return new ChatMessageDto(
                message.getUsername(),
                message.getText(),
                message.getTimestamp());
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

    private AccountTransactionTypeDto toDto(AccountTransactionType type) {
        return switch (type) {
            case LOAD_FUNDS -> AccountTransactionTypeDto.LOAD_FUNDS;
            case EVENT_OPENING -> AccountTransactionTypeDto.EVENT_OPENING;
            case SHARES_PURCHASE -> AccountTransactionTypeDto.SHARES_PURCHASE;
            case SHARES_SALE -> AccountTransactionTypeDto.SHARES_SALE;
            case COMMISSION_RECEIVED -> AccountTransactionTypeDto.COMMISSION_RECEIVED;
            case WINNING_PAYOUT -> AccountTransactionTypeDto.WINNING_PAYOUT;
            case EVENT_CLOSING -> AccountTransactionTypeDto.EVENT_CLOSING;
        };
    }
}
