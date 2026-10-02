package guessmarket.engine.api;

import guessmarket.dto.*;
import guessmarket.engine.exception.file.FileErrorCode;
import guessmarket.engine.exception.file.InvalidFileException;
import guessmarket.engine.exception.request.EventRequestErrorCode;
import guessmarket.engine.exception.request.InvalidEventRequestException;
import guessmarket.engine.exception.request.InvalidUserRequestException;
import guessmarket.engine.exception.request.UserRequestErrorCode;
import guessmarket.engine.model.*;
import guessmarket.engine.exception.request.NonPositiveQuantityException;
import guessmarket.engine.orderbook.*;
import guessmarket.engine.xml.SystemFileLoader;

import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// Manages loaded market events and exposes their operations through data transfer objects.
public class MarketManagerImpl implements MarketManager {

    private final Map<Integer, MarketEvent> events = new LinkedHashMap<>();
    private final UsersRegistry users = new UsersRegistry();
    private final SettlementService settlement = new SettlementService(users);
    private final SystemFileLoader loader = new SystemFileLoader();
    private final DtoMapper dtoMapper = new DtoMapper();
    private int nextEventId = 1;

    @Override
    public synchronized void registerUser(String username) {
        users.addUser(new User(username));
    }

    // Loads all candidate events before replacing the current state, so a failed load leaves it unchanged.
    @Override
    public void loadSystemFile(InputStream content, String uploaderName) throws InvalidFileException {
        getUserByName(uploaderName); // Confirm that the uploader exists
        List<MarketEvent> loadedEvents = loader.load(content, nextEventId); // Load and validate the new data

        validateNewEventNames(loadedEvents); // Reject names already in the system

        for (MarketEvent event : loadedEvents) {
            event.setMarketMakerName(uploaderName);
            events.put(event.getEventId(), event);
        }
        nextEventId += loadedEvents.size();
    }

    // Creates a summary for every event in its original file order.
    @Override
    public List<EventSummaryDto> getEventSummaries() {
        return events.values().stream()
                .map(dtoMapper::createEventSummary)
                .toList();
    }

    // Creates a complete snapshot of the requested event
    @Override
    public EventTradingStateDto getEventTradingState(int eventId) {
        return dtoMapper.createEventTradingState(getEventById(eventId), users.getAllUsers());
    }

    // Creates a complete snapshot of the requested Order Book event.
    @Override
    public OrderBookStateDto getOrderBookState(int eventId) {
        return dtoMapper.createOrderBookState(getOrderBookEvent(eventId), users.getAllUsers());
    }

    // Creates an account summary for every registered user.
    @Override
    public List<UserSummaryDto> getUserSummaries() {
        return users.getAllUsers().stream()
                .map(dtoMapper::createUserSummary)
                .toList();
    }

    // Returns the recorded balances of one user in chronological order.
    @Override
    public List<BalancePointDto> getUserBalanceHistory(String userName) {
        return dtoMapper.createBalanceHistory(getUserByName(userName));
    }

    // Selects events in which the user is either the market maker or a participant.
    @Override
    public List<EventParticipationDto> getUserEvents(String userName) {
        User user = getUserByName(userName);

        return events.values().stream()
                .map(event -> dtoMapper.createEventParticipation(event, user))
                .filter(participation -> participation.isMarketMaker() || participation.isParticipant())
                .toList();
    }

    // Creates the named user's personal trading and result data for one event.
    @Override
    public UserInvolvementDto getUserInvolvement(String userName, int eventId) {
        return dtoMapper.createUserInvolvement(getEventById(eventId), getUserByName(userName));
    }

    // Charges the opening cost, changes the event status and returns its updated state.
    @Override
    public EventTradingStateDto openEvent(String userName, int eventId) {
        User marketMaker = getActiveUserByName(userName); // Require an active user
        MarketEvent event = getEventById(eventId);

        validateMarketMaker(event, userName); // Check event ownership
        validateStatus(event, EventStatus.NOT_STARTED); // Require an unopened event
        validateSufficientBalance(marketMaker, event.calcOpeningCost()); // Check the opening cost

        settlement.settleOpening(event); // Charge the market maker
        event.open(); // Activate the event
        cancelBuyOrdersOfBlocked(Set.of(userName)); // Clear invalid buy orders after the charge

        return dtoMapper.createEventTradingState(event, users.getAllUsers());
    }

    // Settles the winning option before closing the event and returning its final state.
    @Override
    public EventTradingStateDto closeEvent(String userName, int eventId, int winningOptionIndex) {
        getUserByName(userName); // Confirm that the user exists
        MarketEvent event = getEventById(eventId);

        validateMarketMaker(event, userName); // Check event ownership
        validateStatus(event, EventStatus.ACTIVE); // Require active trading
        validateOptionIndex(event, winningOptionIndex); // Validate the winner

        settlement.settleClose(event, event.getOptions().get(winningOptionIndex)); // Pay the winners
        event.close(winningOptionIndex); // Record the result and close trading

        return dtoMapper.createEventTradingState(event, users.getAllUsers());
    }

    // Calculates an LMSR purchase without changing the event or user account.
    @Override
    public TradeDto quotePurchase(String userName, int eventId, int optionIndex, int quantity) {
        LmsrMarketEvent event = getLmsrEvent(eventId); // Require an LMSR event

        validateTradeRequest(event, optionIndex, quantity);

        return dtoMapper.createTradeDto(event.quotePurchase(userName, optionIndex, quantity)); // Calculate the quote
    }

    // Performs and settles an LMSR purchase, then returns the trade and updated event state.
    @Override
    public PurchaseResultDto buyShares(String userName, int eventId, int optionIndex, int quantity) {
        getActiveUserByName(userName); // Require an active user
        LmsrMarketEvent event = getLmsrEvent(eventId); // Require an LMSR event

        validateTradeRequest(event, optionIndex, quantity);

        Trade trade = event.buyShares(userName, optionIndex, quantity); // Execute the purchase
        settlement.settlePurchase(event, trade); // Update balances and holdings
        cancelBuyOrdersOfBlocked(Set.of(userName)); // Clear invalid buy orders after the charge

        return new PurchaseResultDto(dtoMapper.createTradeDto(trade), dtoMapper.createEventTradingState(event, users.getAllUsers()));
    }

    // Plans, settles and commits an Order Book request, then summarizes its execution.
    @Override
    public OrderResultDto submitOrder(String userName, int eventId, int optionIndex, OrderActionDto actionDto,
                                      int quantity, double pricePerShare) {
        User user = getActiveUserByName(userName); // Require an active user
        OrderBookMarketEvent event = getOrderBookEvent(eventId); // Require an Order Book event
        OrderAction action = toOrderAction(actionDto); // Convert the requested action

        validateTradeRequest(event, optionIndex, quantity);
        validateOrderPrice(event, pricePerShare); // Validate the order details

        if (action == OrderAction.SELL) {
            validateAvailableShares(user, event, optionIndex, quantity); // Check unreserved shares
        }

        Order order = event.createOrder(userName, action, quantity, pricePerShare); // Create the incoming order

        MatchPlan plan = event.planMatches(order, optionIndex); // Plan without changing the books
        double commissionPaid = settlement.settleOrder(event, userName, plan); // Settle the plan first

        event.commitOrder(order, optionIndex, plan); // Apply matches and keep any remainder
        settlement.registerParticipation(event, userName); // Record participation

        cancelBuyOrdersOfBlocked(plan.getParticipantNames()); // Remove blocked users' buy orders

        return dtoMapper.createOrderResult(
                new OrderExecution(action, plan, order.getRemainingQuantity(), commissionPaid));
    }

    // The creator becomes the market maker, and pays nothing until the event is opened.
    @Override
    public EventSummaryDto createEvent(String creatorName, NewEventDto request) {
        User creator = getActiveUserByName(creatorName); // Require an active creator

        validateNewEvent(request); // Validate all event details

        MarketEvent event = buildEvent(nextEventId++, request); // Build the selected event type
        event.setMarketMakerName(creator.getName()); // Assign event ownership
        events.put(event.getEventId(), event); // Add the event without opening it

        return dtoMapper.createEventSummary(event);
    }

    // Creates the concrete event type selected in the request and ignores the other method's values.
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

    // Removes waiting buy orders of users blocked by a completed settlement.
    private void cancelBuyOrdersOfBlocked(Collection<String> userNames) {
        userNames.stream()
                .map(users::getUserByName)
                .filter(User::isBlocked)
                .forEach(user -> events.values().forEach(event -> event.cancelBuyOrdersOf(user.getName())));
    }


    private void validateNewEventNames(List<MarketEvent> loadedEvents) throws InvalidFileException {
        Set<String> existingNames = events.values().stream()
                .map(event -> event.getEventName().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        for (MarketEvent event : loadedEvents) {
            if (existingNames.contains(event.getEventName().toLowerCase(Locale.ROOT))) {
                throw new InvalidFileException(FileErrorCode.EVENT_NAME_ALREADY_EXISTS, event.getEventName());
            }
        }
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
        if (!CommissionConfig.isValidPercent(commissionPercent)) {
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

    private void validateTradeRequest(MarketEvent event, int optionIndex, int quantity) {
        validateStatus(event, EventStatus.ACTIVE);
        validateOptionIndex(event, optionIndex);
        validateQuantity(quantity);
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

    // Pending sell orders reserve shares and must be excluded from the quantity available to sell.
    private void validateAvailableShares(User user, OrderBookMarketEvent event, int optionIndex, int quantity) {
        MarketOption option = event.getOptions().get(optionIndex);
        int available = user.getQuantityInEvent(event.getEventId(), option)
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

}
