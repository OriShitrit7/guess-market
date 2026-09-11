package guessmarket.engine.api;

import guessmarket.dto.*;
import guessmarket.engine.exception.file.InvalidFileException;
import guessmarket.engine.exception.request.EventRequestErrorCode;
import guessmarket.engine.exception.request.InvalidEventRequestException;
import guessmarket.engine.exception.request.InvalidUserRequestException;
import guessmarket.engine.exception.request.UserRequestErrorCode;
import guessmarket.engine.model.*;
import guessmarket.engine.exception.request.NonPositiveQuantityException;
import guessmarket.engine.orderbook.Order;
import guessmarket.engine.orderbook.OrderAction;
import guessmarket.engine.orderbook.OrderMatch;
import guessmarket.engine.orderbook.OrderExecution;
import guessmarket.engine.xml.LoadedSystemData;
import guessmarket.engine.xml.SystemFileLoader;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Manages loaded market events and exposes their operations through data transfer objects.
public class MarketManagerImpl implements MarketManager {

    private Map<Integer, MarketEvent> events = new LinkedHashMap<>();
    private UsersRegistry users = new UsersRegistry();
    private SettlementService settlement = new SettlementService(users);
    private final SystemFileLoader loader = new SystemFileLoader();
    private final DtoMapper dtoMapper = new DtoMapper();

    // Loads all candidate events before replacing the current state, so a failed load leaves it unchanged.
    @Override
    public void loadSystemFile(String path) throws InvalidFileException {
        LoadedSystemData loaded = loader.load(path);
        List<MarketEvent> loadedEvents = loaded.getEvents();

        Map<Integer, MarketEvent> loadedEventMap = buildEventMap(loadedEvents);
        UsersRegistry loadedUsers = loaded.getUsersRegistry();
        SettlementService loadedSettlement = new SettlementService(loadedUsers);

        events = loadedEventMap;
        users = loadedUsers;
        settlement = loadedSettlement;
    }

    // Creates a summary for every event in its original file order.
    @Override
    public List<EventSummaryDto> getEventSummaries() {
        return events.values().stream()
                .map(dtoMapper::createEventSummary)
                .toList();
    }

    // Creates a complete snapshot of the requested event, including closed events.
    @Override
    public EventTradingStateDto getEventTradingState(int eventId) {
        return dtoMapper.createEventTradingState(getEventById(eventId));
    }

    @Override
    public OrderBookStateDto getOrderBookState(int eventId) {
        return dtoMapper.createOrderBookState(getOrderBookEvent(eventId), users.getAllUsers());
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

    @Override
    public UserInvolvementDto getUserInvolvement(String userName, int eventId) {
        return dtoMapper.createUserInvolvement(getEventById(eventId), getUserByName(userName));
    }

    @Override
    public EventTradingStateDto openEvent(String userName, int eventId) {
        User marketMaker = getActiveUserByName(userName);
        MarketEvent event = getEventById(eventId);

        validateMarketMaker(event, userName);
        validateStatus(event, EventStatus.NOT_STARTED);
        validateSufficientBalance(marketMaker, event.calcOpeningCost());

        settlement.settleOpening(event);
        event.open();

        return dtoMapper.createEventTradingState(event);
    }

    @Override
    public EventTradingStateDto closeEvent(String userName, int eventId, int winningOptionIndex) {
        getActiveUserByName(userName);
        MarketEvent event = getEventById(eventId);

        validateMarketMaker(event, userName);
        validateStatus(event, EventStatus.ACTIVE);
        validateOptionIndex(event, winningOptionIndex);

        settlement.settleClose(event, event.getOptions().get(winningOptionIndex));
        event.close(winningOptionIndex);

        return dtoMapper.createEventTradingState(event);
    }
    
    @Override
    public PurchaseResultDto buyShares(String userName, int eventId, int optionIndex, int quantity) {
        getActiveUserByName(userName);
        LmsrMarketEvent event = getLmsrEvent(eventId);

        validateStatus(event, EventStatus.ACTIVE);
        validateOptionIndex(event, optionIndex);
        validateQuantity(quantity);

        Trade trade = event.buyShares(userName, optionIndex, quantity);
        settlement.settlePurchase(event, trade);

        return new PurchaseResultDto(dtoMapper.createTradeDto(trade), dtoMapper.createEventTradingState(event));
    }

    @Override
    public OrderResultDto submitOrder(String userName, int eventId, int optionIndex, OrderActionDto actionDto,
                                      int quantity, double pricePerShare) {
        User user = getActiveUserByName(userName);
        OrderBookMarketEvent event = getOrderBookEvent(eventId);
        OrderAction action = toOrderAction(actionDto);

        validateStatus(event, EventStatus.ACTIVE);
        validateOptionIndex(event, optionIndex);
        validateQuantity(quantity);
        validateOrderPrice(event, pricePerShare);

        if (action == OrderAction.SELL) {
            validateAvailableShares(user, event, optionIndex, quantity);
        }

        Order order = event.createOrder(userName, action, quantity, pricePerShare);
        List<OrderMatch> matches = event.findMatches(order, optionIndex);
        double commissionPaid = settlement.settleOrderMatches(event, userName, matches);

        event.commitOrder(order, optionIndex, matches);
        settlement.registerParticipation(event, userName);

        return dtoMapper.createOrderResult(
                new OrderExecution(action, matches, order.getRemainingQuantity(), commissionPaid));
    }

    private OrderAction toOrderAction(OrderActionDto action) {
        if (action == null) {
            throw new InvalidEventRequestException(EventRequestErrorCode.INVALID_ORDER_ACTION);
        }
        return OrderAction.valueOf(action.name());
    }


    private LmsrMarketEvent getLmsrEvent(int eventId) {
        if (getEventById(eventId) instanceof LmsrMarketEvent lmsrEvent) {
            return lmsrEvent;
        }
        throw new InvalidEventRequestException(EventRequestErrorCode.WRONG_TRADING_METHOD);
    }

    private OrderBookMarketEvent getOrderBookEvent(int eventId) {
        if (getEventById(eventId) instanceof OrderBookMarketEvent orderBookEvent) {
            return orderBookEvent;
        }
        throw new InvalidEventRequestException(EventRequestErrorCode.WRONG_TRADING_METHOD);
    }

    private void validateStatus(MarketEvent event, EventStatus requiredStatus) {
        if (event.getStatus() != requiredStatus) {
            throw new InvalidEventRequestException(EventRequestErrorCode.INVALID_EVENT_STATE);
        }
    }

    private void validateMarketMaker(MarketEvent event, String userName) {
        if (!event.isMarketMaker(userName)) {
            throw new InvalidEventRequestException(EventRequestErrorCode.UNAUTHORIZED_EVENT_ACTION);
        }
    }

    private void validateOptionIndex(MarketEvent event, int optionIndex) {
        if (optionIndex < 0 || optionIndex >= event.getOptionCount()) {
            throw new InvalidEventRequestException(EventRequestErrorCode.OPTION_NOT_FOUND);
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new NonPositiveQuantityException(quantity);
        }
    }

    private void validateOrderPrice(OrderBookMarketEvent event, double pricePerShare) {
        if (!event.isValidOrderPrice(pricePerShare)) {
            throw new InvalidEventRequestException(EventRequestErrorCode.INVALID_ORDER_PRICE);
        }
    }

    private void validateSufficientBalance(User user, double amount) {
        if (!user.hasSufficientBalance(amount)) {
            throw new InvalidUserRequestException(UserRequestErrorCode.INSUFFICIENT_BALANCE);
        }
    }

    private void validateAvailableShares(User user, OrderBookMarketEvent event, int optionIndex, int quantity) {
        MarketOption option = event.getOptions().get(optionIndex);
        long available = user.getQuantityInEvent(event.getEventId(), option)
                - event.getPendingSellQuantity(user.getName(), optionIndex);

        if (available < quantity) {
            throw new InvalidUserRequestException(UserRequestErrorCode.INSUFFICIENT_SHARES);
        }
    }

    private MarketEvent getEventById(int eventId) {
        MarketEvent event = events.get(eventId);

        if (event == null) {
            throw new InvalidEventRequestException(EventRequestErrorCode.EVENT_NOT_FOUND);
        }
        return event;
    }

    private User getUserByName(String userName) {
        User user = users.getUserByName(userName);

        if (user == null) {
            throw new InvalidUserRequestException(UserRequestErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private User getActiveUserByName(String userName) {
        User user = getUserByName(userName);

        if (user.isBlocked()) {
            throw new InvalidUserRequestException(UserRequestErrorCode.USER_BLOCKED);
        }
        return user;
    }

    private Map<Integer, MarketEvent> buildEventMap(List<MarketEvent> loadedEvents) {
        Map<Integer, MarketEvent> eventMap = new LinkedHashMap<>();

        for (MarketEvent event : loadedEvents) {
            eventMap.put(event.getEventId(), event);
        }
        return eventMap;
    }
}
