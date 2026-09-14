package guessmarket.engine.api;

import guessmarket.dto.*;
import guessmarket.engine.exception.file.InvalidFileException;
import guessmarket.engine.exception.request.EventRequestErrorCode;
import guessmarket.engine.exception.request.InvalidEventRequestException;
import guessmarket.engine.exception.request.InvalidUserRequestException;
import guessmarket.engine.exception.request.UserRequestErrorCode;
import guessmarket.engine.model.*;
import guessmarket.engine.exception.request.NonPositiveQuantityException;
import guessmarket.engine.orderbook.*;
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
        return dtoMapper.createEventTradingState(getEventById(eventId), users.getAllUsers());
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
    public List<BalancePointDto> getUserBalanceHistory(String userName) {
        return dtoMapper.createBalanceHistory(getUserByName(userName));
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

        return dtoMapper.createEventTradingState(event, users.getAllUsers());
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

        return dtoMapper.createEventTradingState(event, users.getAllUsers());
    }
    
    @Override
    public TradeDto quotePurchase(String userName, int eventId, int optionIndex, int quantity) {
        LmsrMarketEvent event = getLmsrEvent(eventId);

        validateStatus(event, EventStatus.ACTIVE);
        validateOptionIndex(event, optionIndex);
        validateQuantity(quantity);

        return dtoMapper.createTradeDto(event.quotePurchase(userName, optionIndex, quantity));
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

        return new PurchaseResultDto(dtoMapper.createTradeDto(trade), dtoMapper.createEventTradingState(event, users.getAllUsers()));
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

        MatchPlan plan = event.planMatches(order, optionIndex);
        double commissionPaid = settlement.settleOrder(event, userName, plan);

        event.commitOrder(order, optionIndex, plan);
        settlement.registerParticipation(event, userName);

        return dtoMapper.createOrderResult(
                new OrderExecution(action, plan, order.getRemainingQuantity(), commissionPaid));
    }

    // The creator becomes the market maker, and pays nothing until the event is opened.
    @Override
    public EventSummaryDto createEvent(String creatorName, NewEventDto request) {
        User creator = getActiveUserByName(creatorName);

        validateNewEvent(request);

        MarketEvent event = buildEvent(nextEventId(), request);
        event.setMarketMakerName(creator.getName());
        events.put(event.getEventId(), event);

        return dtoMapper.createEventSummary(event);
    }

    private int nextEventId() {
        return events.keySet().stream().max(Integer::compareTo).orElse(0) + 1;
    }

    private MarketEvent buildEvent(int eventId, NewEventDto request) {
        List<MarketOption> options = request.getOptionNames().stream()
                .map(String::trim)
                .map(MarketOption::new)
                .toList();
        CommissionConfig commission = new CommissionConfig(request.getCommissionPercent(),
                toCommissionPolicy(request.getCommissionPolicy()));
        String eventName = request.getEventName().trim();
        String description = request.getDescription().trim();

        if (toTradingMethodType(request.getMethodType()) == TradingMethodType.LMSR) {
            return new LmsrMarketEvent(eventId, eventName, description, options, commission, request.getB());
        }
        return new OrderBookMarketEvent(eventId, eventName, description, options, commission,
                request.getD(), request.isAllowMint(), request.getInitialInvestment());
    }

    // The XSD guards a loaded event; a created one is guarded here.
    private void validateNewEvent(NewEventDto request) {
        validateText(request.getEventName(), EventRequestErrorCode.MISSING_EVENT_NAME);
        validateText(request.getDescription(), EventRequestErrorCode.MISSING_EVENT_DESCRIPTION);
        validateOptionNames(request.getOptionNames());
        validateCommissionPercent(request.getCommissionPercent());
        validateMethodValues(request);
    }

    private void validateText(String text, EventRequestErrorCode errorCode) {
        if (text == null || text.isBlank()) {
            throw new InvalidEventRequestException(errorCode);
        }
    }

    private void validateOptionNames(List<String> optionNames) {
        if (optionNames.size() != MarketEvent.REQUIRED_OPTION_COUNT) {
            throw new InvalidEventRequestException(EventRequestErrorCode.WRONG_OPTION_COUNT);
        }
        optionNames.forEach(name -> validateText(name, EventRequestErrorCode.MISSING_OPTION_NAME));

        if (optionNames.getFirst().trim().equalsIgnoreCase(optionNames.getLast().trim())) {
            throw new InvalidEventRequestException(EventRequestErrorCode.DUPLICATE_OPTION_NAMES);
        }
    }

    private void validateCommissionPercent(int commissionPercent) {
        if (commissionPercent < CommissionConfig.MIN_COMMISSION_PERCENT
                || commissionPercent > CommissionConfig.MAX_COMMISSION_PERCENT) {
            throw new InvalidEventRequestException(EventRequestErrorCode.COMMISSION_OUT_OF_RANGE);
        }
    }

    private void validateMethodValues(NewEventDto request) {
        if (toTradingMethodType(request.getMethodType()) == TradingMethodType.LMSR) {
            if (request.getB() <= 0) {
                throw new InvalidEventRequestException(EventRequestErrorCode.INVALID_LMSR_B);
            }
            return;
        }
        if (request.getD() <= 0) {
            throw new InvalidEventRequestException(EventRequestErrorCode.INVALID_BASE_VALUE);
        }
        if (request.getInitialInvestment() < 0) {
            throw new InvalidEventRequestException(EventRequestErrorCode.INVALID_INITIAL_INVESTMENT);
        }
    }

    private CommissionPolicy toCommissionPolicy(CommissionPolicyDto policy) {
        if (policy == null) {
            throw new InvalidEventRequestException(EventRequestErrorCode.COMMISSION_OUT_OF_RANGE);
        }
        return CommissionPolicy.valueOf(policy.name());
    }

    private TradingMethodType toTradingMethodType(TradingMethodTypeDto methodType) {
        if (methodType == null) {
            throw new InvalidEventRequestException(EventRequestErrorCode.MISSING_TRADING_METHOD);
        }
        return TradingMethodType.valueOf(methodType.name());
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
