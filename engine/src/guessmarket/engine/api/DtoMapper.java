package guessmarket.engine.api;

import guessmarket.dto.*;
import guessmarket.engine.model.*;
import guessmarket.engine.trading.TradingMethodType;

import java.util.ArrayList;
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
                createTradeDtos(event),
                winner == null ? null : winner.getName());
    }

    public UserSummaryDto createUserSummary(User user) {
        return new UserSummaryDto(user.getName(), user.getBalance(), user.isBlocked());
    }

    EventParticipationDto createEventParticipation(MarketEvent event, User user) {
        return new EventParticipationDto(createEventSummary(event), event.getMarketMaker() == user,
                user.getPositions().containsKey(event.getEventId()));
    }

    private List<OptionStateDto> createOptionStates(MarketEvent event) {
        List<OptionStateDto> states = new ArrayList<>();

        List<MarketOption> options = event.getOptions();
        double[] values = event.getOptionsValues();

        for (int i = 0; i < options.size(); i++) {
            states.add(new OptionStateDto(options.get(i).getName(), values[i],
                    options.get(i).getTotalSharesBought()));
        }
        return states;
    }


    // Converts trade history to DTOs in reverse order so the newest purchases appear first.
    private List<TradeDto> createTradeDtos(MarketEvent event) {
        List<Trade> trades = event.getAccount().getTradeHistory();
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
