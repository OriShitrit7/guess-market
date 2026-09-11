package guessmarket.engine.api;

import guessmarket.dto.*;
import guessmarket.engine.model.*;
import guessmarket.engine.orderbook.OptionOrderBook;
import guessmarket.engine.orderbook.Order;
import guessmarket.engine.orderbook.OrderExecution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DtoMapper {

    EventSummaryDto createEventSummary(MarketEvent event) {
        List<String> optionNames = event.getOptions().stream()
                .map(MarketOption::getName)
                .toList();

        return new EventSummaryDto(event.getEventId(), event.getEventName(), event.getDescription(),
                event.getCommissionPercent(), toDto(event.getCommissionPolicy()), optionNames,
                toDto(event.getStatus()), toDto(event.getMethodType()), event.getAccount().getBalance());
    }

    EventTradingStateDto createEventTradingState(MarketEvent event) {
        MarketOption winner = event.getWinningOption();

        return new EventTradingStateDto(createEventSummary(event), createOptionStates(event),
                event.getAccount().getTotalCommissionCollected(),
                createTradeDtos(getLmsrTrades(event)),
                winner == null ? null : winner.getName());
    }

    OrderBookStateDto createOrderBookState(OrderBookMarketEvent event, Collection<User> users) {
        MarketOption winner = event.getWinningOption();

        return new OrderBookStateDto(createEventSummary(event), createOptionBooks(event),
                createParticipants(event, users), event.getAccount().getTotalCommissionCollected(),
                winner == null ? null : winner.getName());
    }

    public UserSummaryDto createUserSummary(User user) {
        return new UserSummaryDto(user.getName(), user.getBalance(), user.isBlocked());
    }

    UserInvolvementDto createUserInvolvement(MarketEvent event, User user) {
        List<Trade> userTrades = getLmsrTrades(event).stream()
                .filter(trade -> trade.getBuyerName().equals(user.getName()))
                .toList();

        Position position = user.getPositions().get(event.getEventId());
        boolean finalProfitLossAvailable = event instanceof OrderBookMarketEvent
                && event.getStatus() == EventStatus.CLOSED;
        return new UserInvolvementDto(createTradeDtos(userTrades), createHoldingDtos(event, position),
                position == null ? 0 : position.getTotalCommissionPaid(), finalProfitLossAvailable,
                position == null ? 0 : position.calcProfitLoss());
    }

    EventParticipationDto createEventParticipation(MarketEvent event, User user) {
        return new EventParticipationDto(createEventSummary(event), event.isMarketMaker(user.getName()),
                user.getPositions().containsKey(event.getEventId()));
    }

    OrderResultDto createOrderResult(OrderExecution execution) {
        return new OrderResultDto(OrderActionDto.valueOf(execution.getAction().name()), execution.getMatchedQuantity(),
                execution.getRemainingQuantity(), execution.getTradeAmount(), execution.getCommissionPaid());
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
    private List<EventParticipantDto> createParticipants(MarketEvent event, Collection<User> users) {
        return users.stream()
                .filter(user -> user.getPositions().containsKey(event.getEventId()))
                .map(user -> new EventParticipantDto(user.getName(),
                        createHoldingDtos(event, user.getPositions().get(event.getEventId()))))
                .toList();
    }

    private List<OptionHoldingDto> createHoldingDtos(MarketEvent event, Position position) {
        return event.getOptions().stream().map(option -> {
            OptionHolding holding = position == null ? null : position.getHoldings().get(option);
            int quantity = holding == null ? 0 : holding.getQuantity();
            double amountPaid = holding == null ? 0 : holding.getAmountPaid();
            return new OptionHoldingDto(option.getName(), quantity, amountPaid);
        }).toList();
    }

    private List<OptionStateDto> createOptionStates(MarketEvent event) {
        if (!(event instanceof LmsrMarketEvent lmsrEvent)) {
            return List.of();
        }

        List<OptionStateDto> states = new ArrayList<>();

        List<MarketOption> options = event.getOptions();
        double[] values = lmsrEvent.getOptionsValues();

        for (int i = 0; i < options.size(); i++) {
            states.add(new OptionStateDto(options.get(i).getName(), values[i],
                    options.get(i).getTotalSharesBought()));
        }
        return states;
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
        return new TradeDto(trade.getOption().getName(), trade.getQuantity(),
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
