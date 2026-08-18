package guessmarket.engine.api;

import guessmarket.dto.CommissionPolicyDto;
import guessmarket.dto.EventSummaryDto;
import guessmarket.dto.EventStatusDto;
import guessmarket.dto.EventTradingStateDto;
import guessmarket.dto.OptionStateDto;
import guessmarket.dto.PurchaseResultDto;
import guessmarket.dto.TradeDto;
import guessmarket.engine.exception.*;
import guessmarket.engine.model.*;
import guessmarket.engine.xml.SystemFileLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MarketManagerImpl implements MarketManager {

    private Map<Integer, MarketEvent> events = new LinkedHashMap<>();
    private final SystemFileLoader loader = new SystemFileLoader();

    private MarketEvent getEventById(int eventId) {
        MarketEvent event = events.get(eventId);

        if (event == null) {
            throw new EventNotFoundException(eventId);
        }
        return event;
    }

    private MarketEvent getActiveEventById(int eventId) {
        MarketEvent event = getEventById(eventId);

        if (event.getStatus() != EventStatus.ACTIVE) {
            throw new EventClosedException(event.getEventName());
        }
        return event;
    }

    private void validateOptionIndex(MarketEvent event, int optionIndex) {
        if (optionIndex < 0 || optionIndex >= event.getOptionCount()) {
            throw new OptionNotFoundException(event.getEventName(), optionIndex, event.getOptionCount());
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new NonPositiveQuantityException(quantity);
        }
    }

    @Override
    public void loadSystemFile(String path) throws InvalidFileException {
        List<MarketEvent> loadedEvents = loader.load(path);

        processInitialSubsidies(loadedEvents);
        events = buildEventMap(loadedEvents);
    }

    @Override
    public List<EventSummaryDto> getEventSummaries() {
        return events.values().stream()
                .map(this::createEventSummary)
                .toList();
    }
    // MarketManagerImpl
    @Override
    public List<EventSummaryDto> getActiveEventSummaries() {
        return events.values().stream()
                .filter(event -> event.getStatus() == EventStatus.ACTIVE)
                .map(this::createEventSummary)
                .toList();
    }

    @Override
    public EventTradingStateDto getEventTradingState(int eventId) {
        return createEventTradingState(getEventById(eventId));
    }

    @Override
    public PurchaseResultDto buyShares(int eventId, int optionIndex, int quantity) {
        MarketEvent event = getActiveEventById(eventId);

        validateOptionIndex(event, optionIndex);
        validateQuantity(quantity);

        Trade trade = event.buyShares(optionIndex, quantity);

        return new PurchaseResultDto(createTradeDto(trade), createEventTradingState(event));
    }

    @Override
    public EventTradingStateDto closeEvent(int eventId, int winningOptionIndex) {
        MarketEvent event = getActiveEventById(eventId);

        validateOptionIndex(event, winningOptionIndex);
        event.close(winningOptionIndex);

        return createEventTradingState(event);
    }

    private EventSummaryDto createEventSummary(MarketEvent event) {
        List<String> optionNames = new ArrayList<>();

        for (MarketOption option : event.getOptions()) {
            optionNames.add(option.getName());
        }

        return new EventSummaryDto(event.getEventId(), event.getEventName(), event.getDescription(),
                event.getCommissionPercentage(), toDto(event.getCommissionPolicy()),
                optionNames, toDto(event.getStatus()));
    }

    private EventTradingStateDto createEventTradingState(MarketEvent event) {
        MarketOption winner = event.getWinningOption();

        return new EventTradingStateDto(createEventSummary(event), createOptionStates(event),
                event.getAccount().getBalance(), event.getAccount().getTotalCommissionCollected(), createTradeInfos(event),
                winner == null ? null : winner.getName());
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

    private List<TradeDto> createTradeInfos(MarketEvent event) {
        List<TradeDto> infos = new ArrayList<>();

        for (Trade trade : event.getAccount().getTradeHistory()) {
            infos.add(createTradeDto(trade));
        }
        Collections.reverse(infos);
        return infos;
    }

    private TradeDto createTradeDto(Trade trade) {
        return new TradeDto(trade.getOption().getName(), trade.getQuantity(),
                trade.getSharesCost(), trade.getCommissionCost());
    }

    private CommissionPolicyDto toDto(CommissionPolicy policy) {
        return switch (policy) {
            case ON_PURCHASE -> CommissionPolicyDto.ON_PURCHASE;
            case ON_CLOSE -> CommissionPolicyDto.ON_CLOSE;
        };
    }

    private EventStatusDto toDto(EventStatus status) {
        return switch (status) {
            case ACTIVE -> EventStatusDto.ACTIVE;
            case CLOSED -> EventStatusDto.CLOSED;
        };
    }

    private void processInitialSubsidies(List<MarketEvent> loadedEvents) {
        loadedEvents.forEach(MarketEvent::processInitialSubsidy);
    }

    private Map<Integer, MarketEvent> buildEventMap(List<MarketEvent> loadedEvents) {
        Map<Integer, MarketEvent> eventMap = new LinkedHashMap<>();

        for (MarketEvent event : loadedEvents) {
            eventMap.put(event.getEventId(), event);
        }
        return eventMap;
    }
}
