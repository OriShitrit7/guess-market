package guessmarket.engine.api;

import guessmarket.dto.CommissionPolicyDto;
import guessmarket.dto.EventStatusDto;
import guessmarket.dto.EventSummaryDto;
import guessmarket.dto.EventTradingStateDto;
import guessmarket.dto.OptionStateDto;
import guessmarket.dto.PurchaseResultDto;
import guessmarket.dto.TradeDto;
import guessmarket.engine.exception.EventClosedException;
import guessmarket.engine.exception.EventNotFoundException;
import guessmarket.engine.exception.InvalidFileException;
import guessmarket.engine.exception.NonPositiveQuantityException;
import guessmarket.engine.exception.OptionNotFoundException;
import guessmarket.engine.model.CommissionPolicy;
import guessmarket.engine.model.EventStatus;
import guessmarket.engine.model.MarketEvent;
import guessmarket.engine.model.MarketOption;
import guessmarket.engine.model.Trade;
import guessmarket.engine.xml.SystemFileLoader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Manages loaded market events and exposes their operations through data transfer objects.
public class MarketManagerImpl implements MarketManager {

    private Map<Integer, MarketEvent> events = new LinkedHashMap<>();
    private final SystemFileLoader loader = new SystemFileLoader();

    // Loads all candidate events before replacing the current state, so a failed load leaves it unchanged.
    @Override
    public void loadSystemFile(String path) throws InvalidFileException {
        List<MarketEvent> loadedEvents = loader.load(path);

        processInitialSubsidies(loadedEvents);
        events = buildEventMap(loadedEvents);
    }

    // Creates a summary for every event in its original file order.
    @Override
    public List<EventSummaryDto> getEventSummaries() {
        return events.values().stream()
                .map(this::createEventSummary)
                .toList();
    }

    // Creates summaries only for events that can still accept purchases or be closed.
    @Override
    public List<EventSummaryDto> getActiveEventSummaries() {
        return events.values().stream()
                .filter(event -> event.getStatus() == EventStatus.ACTIVE)
                .map(this::createEventSummary)
                .toList();
    }

    // Creates a complete snapshot of the requested event, including closed events.
    @Override
    public EventTradingStateDto getEventTradingState(int eventId) {
        return createEventTradingState(getEventById(eventId));
    }

    // Validates and performs a purchase, then returns both the trade and the updated event state.
    @Override
    public PurchaseResultDto buyShares(int eventId, int optionIndex, int quantity) {
        MarketEvent event = getActiveEventById(eventId);

        validateOptionIndex(event, optionIndex);
        validateQuantity(quantity);

        Trade trade = event.buyShares(optionIndex, quantity);

        return new PurchaseResultDto(createTradeDto(trade), createEventTradingState(event));
    }

    // Validates and closes an active event, then returns its final state.
    @Override
    public EventTradingStateDto closeEvent(int eventId, int winningOptionIndex) {
        MarketEvent event = getActiveEventById(eventId);

        validateOptionIndex(event, winningOptionIndex);
        event.close(winningOptionIndex);

        return createEventTradingState(event);
    }

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

    private EventSummaryDto createEventSummary(MarketEvent event) {
        List<String> optionNames = event.getOptions().stream()
                .map(MarketOption::getName)
                .toList();

        return new EventSummaryDto(event.getEventId(), event.getEventName(), event.getDescription(),
                event.getCommissionPercent(), toDto(event.getCommissionPolicy()),
                optionNames, toDto(event.getStatus()));
    }

    private EventTradingStateDto createEventTradingState(MarketEvent event) {
        MarketOption winner = event.getWinningOption();

        return new EventTradingStateDto(createEventSummary(event), createOptionStates(event),
                event.getAccount().getBalance(),
                event.getAccount().getTotalCommissionCollected(),
                createTradeDtos(event),
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

    // Converts trade history to DTOs in reverse order so the newest purchases appear first.
    private List<TradeDto> createTradeDtos(MarketEvent event) {
        List<Trade> trades = event.getAccount().getTradeHistory();
        List<TradeDto> tradeDtos = new ArrayList<>();

        for (int i = trades.size() - 1; i >= 0; i--) {
            tradeDtos.add(createTradeDto(trades.get(i)));
        }
        return tradeDtos;
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
