package guessmarket.engine.api;

import guessmarket.dto.EventParticipationDto;
import guessmarket.dto.EventSummaryDto;
import guessmarket.dto.EventTradingStateDto;
import guessmarket.dto.PurchaseResultDto;
import guessmarket.dto.UserSummaryDto;
import guessmarket.engine.exception.request.EventClosedException;
import guessmarket.engine.exception.request.EventNotFoundException;
import guessmarket.engine.exception.InvalidFileException;
import guessmarket.engine.exception.request.NonPositiveQuantityException;
import guessmarket.engine.exception.request.OptionNotFoundException;
import guessmarket.engine.exception.request.UserNotFoundException;
import guessmarket.engine.model.EventStatus;
import guessmarket.engine.model.MarketEvent;
import guessmarket.engine.model.Trade;
import guessmarket.engine.model.User;
import guessmarket.engine.model.UsersRegistry;
import guessmarket.engine.xml.LoadedSystemData;
import guessmarket.engine.xml.SystemFileLoader;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Manages loaded market events and exposes their operations through data transfer objects.
public class MarketManagerImpl implements MarketManager {

    private Map<Integer, MarketEvent> events = new LinkedHashMap<>();
    private UsersRegistry users = new UsersRegistry();
    private final SystemFileLoader loader = new SystemFileLoader();
    private final DtoMapper dtoMapper = new DtoMapper();

    // Loads all candidate events before replacing the current state, so a failed load leaves it unchanged.
    @Override
    public void loadSystemFile(String path) throws InvalidFileException {
        LoadedSystemData loaded = loader.load(path);
        List<MarketEvent> loadedEvents = loaded.getEvents();

        processInitialSubsidies(loadedEvents);
        events = buildEventMap(loadedEvents);
        users = loaded.getUsersRegistry();
    }

    // Creates a summary for every event in its original file order.
    @Override
    public List<EventSummaryDto> getEventSummaries() {
        return events.values().stream()
                .map(dtoMapper::createEventSummary)
                .toList();
    }

    // Creates summaries only for events that can still accept purchases or be closed.
    @Override
    public List<EventSummaryDto> getActiveEventSummaries() {
        return events.values().stream()
                .filter(event -> event.getStatus() == EventStatus.ACTIVE)
                .map(dtoMapper::createEventSummary)
                .toList();
    }

    // Creates a complete snapshot of the requested event, including closed events.
    @Override
    public EventTradingStateDto getEventTradingState(int eventId) {
        return dtoMapper.createEventTradingState(getEventById(eventId));
    }

    @Override
    public List<UserSummaryDto> getUserSummaries() {
        return users.getAllUsers().stream()
                .map(dtoMapper::createUserSummary)
                .toList();
    }

    @Override
    public List<EventParticipationDto> getUserEvents(String userName) {
        User user = getUserByName(userName);

        return events.values().stream()
                .map(event -> dtoMapper.createEventParticipation(event, user))
                .filter(participation -> participation.isMarketMaker() || participation.isParticipant())
                .toList();
    }

    // Validates and performs a purchase, then returns both the trade and the updated event state.
    @Override
    public PurchaseResultDto buyShares(int eventId, int optionIndex, int quantity) {
        MarketEvent event = getActiveEventById(eventId);

        validateOptionIndex(event, optionIndex);
        validateQuantity(quantity);

        Trade trade = event.buyShares(optionIndex, quantity);

        return new PurchaseResultDto(dtoMapper.createTradeDto(trade),
                dtoMapper.createEventTradingState(event));
    }

    // Validates and closes an active event, then returns its final state.
    @Override
    public EventTradingStateDto closeEvent(int eventId, int winningOptionIndex) {
        MarketEvent event = getActiveEventById(eventId);

        validateOptionIndex(event, winningOptionIndex);
        event.close(winningOptionIndex);

        return dtoMapper.createEventTradingState(event);
    }

    private MarketEvent getEventById(int eventId) {
        MarketEvent event = events.get(eventId);

        if (event == null) {
            throw new EventNotFoundException(eventId);
        }
        return event;
    }

    private User getUserByName(String userName) {
        User user = users.getUserByName(userName);

        if (user == null) {
            throw new UserNotFoundException(userName);
        }
        return user;
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
