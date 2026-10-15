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
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

// Manages the users and market events of the system and exposes their operations through data transfer objects.
public class MarketManagerImpl implements MarketManager {
    // Printable ASCII only: English letters, digits, spaces and punctuation.
    public static final Pattern ENGLISH_TEXT = Pattern.compile("[\\x20-\\x7E]+");

    private final Map<Integer, MarketEvent> events = new LinkedHashMap<>();
    private final UsersRegistry users = new UsersRegistry();
    private final SettlementService settlement = new SettlementService(users);
    private final SystemFileLoader loader = new SystemFileLoader();
    private final DtoMapper dtoMapper = new DtoMapper();
    private int nextEventId = 1;

    // Change counters, so that callers can tell whether data has changed since they last read it.
    private int eventsChangeCount;
    private int usersChangeCount;
    // Event ID to the number of changes in that event. An event that never changed is not in the map.
    private final Map<Integer, Integer> eventChangeCounts = new HashMap<>();

    // Registers a new user after checking that the name is in English. The registry rejects a taken name.
    @Override
    public void registerUser(String username) {
        validateUserName(username);
        users.addUser(new User(username));
        usersChangeCount++;
    }

    private void validateUserName(String username) {
        if (!ENGLISH_TEXT.matcher(username).matches()) {
            throw new InvalidUserRequestException(UserRequestErrorCode.USER_NAME_NOT_ENGLISH);
        }
    }

    // Loads and validates all the events of the file before adding any of them, so a failed load leaves
    // the system unchanged.
    @Override
    public void loadSystemFile(String fileName, InputStream content, String uploaderName) throws InvalidFileException {
        getUserByName(uploaderName); // Confirm that the uploader exists
        List<MarketEvent> loadedEvents = loader.load(fileName, content, nextEventId); // Load and validate the new data

        validateNewEventNames(loadedEvents); // Reject names already in the system

        for (MarketEvent event : loadedEvents) {
            event.setMarketMakerName(uploaderName);
            events.put(event.getEventId(), event);
        }
        nextEventId += loadedEvents.size();
        recordEventsListChange();
    }

    // Creates a summary for every event, in the order the events were added.
    @Override
    public List<EventSummaryDto> getEventSummaries() {
        return events.values().stream()
                .map(dtoMapper::createEventSummary)
                .toList();
    }

    // Creates a complete snapshot of the requested event.
    @Override
    public EventStateDto getEventState(int eventId) {
        return dtoMapper.createEventState(getEventById(eventId), users.getAllUsers());
    }

    // Creates an account summary for every registered user.
    @Override
    public List<UserSummaryDto> getUserSummaries() {
        return users.getAllUsers().stream()
                .map(user -> dtoMapper.createUserSummary(user, isMarketMakerOfAnyEvent(user.getName())))
                .toList();
    }

    private boolean isMarketMakerOfAnyEvent(String userName) {
        return events.values().stream().anyMatch(event -> event.isMarketMaker(userName));
    }

    @Override
    public int getEventsChangeCount() {
        return eventsChangeCount;
    }

    @Override
    public int getUsersChangeCount() {
        return usersChangeCount;
    }

    @Override
    public int getEventChangeCount(int eventId) {
        getEventById(eventId); // Confirm that the event exists
        return eventChangeCounts.getOrDefault(eventId, 0);
    }

    // Creates the user's account view, with only the transactions from the given index onward.
    @Override
    public AccountDetailsDto getAccountDetails(String userName, int fromTransaction) {
        return dtoMapper.createAccountDetails(getUserByName(userName), getUserEvents(userName), fromTransaction);
    }

    // A client sends this count back as the index of the first transaction it has not seen yet.
    @Override
    public int getTransactionCount(String userName) {
        return getUserByName(userName).getTransactions().size();
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

    @Override
    public EventHistoryDto getEventHistory(int eventId, int tradesFrom, List<Integer> pricesFrom) {
        return dtoMapper.createEventHistory(getEventById(eventId), tradesFrom, pricesFrom);
    }

    // Creates the named user's personal trading and result data for one event.
    @Override
    public UserInvolvementDto getUserInvolvement(String userName, int eventId) {
        return dtoMapper.createUserInvolvement(getEventById(eventId), getUserByName(userName));
    }

    // Charges the opening cost, changes the event status and returns its updated state.
    @Override
    public EventStateDto openEvent(String userName, int eventId) {
        User marketMaker = getActiveUserByName(userName); // Require an active user
        MarketEvent event = getEventById(eventId);

        validateMarketMaker(event, userName); // Check event ownership
        validateStatus(event, EventStatus.NOT_STARTED); // Require an unopened event
        validateSufficientBalance(marketMaker, event.calcOpeningCost()); // Check the opening cost

        settlement.settleOpening(event); // Charge the market maker
        event.open(); // Activate the event
        cancelBuyOrdersOfBlocked(Set.of(userName)); // Clear invalid buy orders after the charge
        recordEventChange(event);

        return dtoMapper.createEventState(event, users.getAllUsers());
    }

    // Settles the winning option before closing the event and returning its final state.
    @Override
    public EventStateDto closeEvent(String userName, int eventId, int winningOptionIndex) {
        getUserByName(userName); // Confirm that the user exists
        MarketEvent event = getEventById(eventId);

        validateMarketMaker(event, userName); // Check event ownership
        validateStatus(event, EventStatus.ACTIVE); // Require active trading
        validateOptionIndex(event, winningOptionIndex); // Validate the winner

        settlement.settleClose(event, event.getOptions().get(winningOptionIndex)); // Pay the winners
        event.close(winningOptionIndex); // Record the result and close trading
        recordEventChange(event);

        return dtoMapper.createEventState(event, users.getAllUsers());
    }

    // Calculates an LMSR purchase without changing the event or user account.
    @Override
    public TradeDto quoteLmsrPurchase(String userName, int eventId, int optionIndex, int quantity) {
        LmsrMarketEvent event = getLmsrEvent(eventId); // Require an LMSR event

        validateTradeRequest(event, optionIndex, quantity);

        return dtoMapper.createTradeDto(event.quotePurchase(userName, optionIndex, quantity)); // Calculate the quote
    }

    // Validates the amount and adds it to the user's balance. A blocked user may load funds too.
    @Override
    public void loadFunds(String userName, double amount) {
        User user = getUserByName(userName);
        // Reject amounts that are not valid money values or that would overflow the balance
        if (!Double.isFinite(amount) || amount <= 0 || BigDecimal.valueOf(amount).scale() > 2
                || !Double.isFinite(user.getBalance() + amount)) {
            throw new InvalidUserRequestException(UserRequestErrorCode.INVALID_LOAD_AMOUNT);
        }
        settlement.settleLoadFunds(userName, amount);
        usersChangeCount++;
    }

    // Performs and settles an LMSR purchase, then returns the trade, the updated event state and the
    // buyer's new balance.
    @Override
    public PurchaseResultDto buyLmsrShares(String userName, int eventId, int optionIndex, int quantity) {
        User user = getActiveUserByName(userName); // Require an active user
        LmsrMarketEvent event = getLmsrEvent(eventId); // Require an LMSR event

        validateTradeRequest(event, optionIndex, quantity);

        Trade trade = event.buyShares(userName, optionIndex, quantity); // Execute the purchase
        settlement.settlePurchase(event, trade); // Update balances and holdings
        cancelBuyOrdersOfBlocked(Set.of(userName)); // Clear invalid buy orders after the charge
        recordEventChange(event);

        return new PurchaseResultDto(dtoMapper.createTradeDto(trade),
                dtoMapper.createLmsrState(event, users.getAllUsers()), user.getBalance());
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
        recordEventChange(event);

        return dtoMapper.createOrderResult(
                new OrderExecution(action, plan, order.getRemainingQuantity(), commissionPaid), user.getBalance());
    }

    // The creator becomes the market maker, and pays nothing until the event is opened.
    @Override
    public EventSummaryDto createEvent(String creatorName, NewEventDto request) {
        User creator = getActiveUserByName(creatorName); // Require an active creator

        validateNewEvent(request); // Validate all event details

        MarketEvent event = buildEvent(nextEventId++, request); // Build the selected event type
        event.setMarketMakerName(creator.getName()); // Assign event ownership
        events.put(event.getEventId(), event); // Add the event without opening it
        recordEventsListChange();

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

    // Removes waiting buy orders of users blocked by a completed settlement. The orders may sit in other
    // events too, so every event that lost an order is counted as changed.
    private void cancelBuyOrdersOfBlocked(Collection<String> userNames) {
        userNames.stream()
                .map(users::getUserByName)
                .filter(User::isBlocked)
                .forEach(user -> events.values().stream()
                        .filter(event -> event.cancelBuyOrdersOf(user.getName()))
                        .forEach(this::recordEventChange));
    }

    // An operation on an event changes the event itself, the events list and the balances of its users.
    private void recordEventChange(MarketEvent event) {
        eventChangeCounts.merge(event.getEventId(), 1, Integer::sum);
        recordEventsListChange();
    }

    // Adding or changing an event may also change who is a market maker, so the users list changes too.
    private void recordEventsListChange() {
        eventsChangeCount++;
        usersChangeCount++;
    }

    // Event names are unique in the system, ignoring case.
    private void validateNewEventNames(List<MarketEvent> loadedEvents) throws InvalidFileException {
        for (MarketEvent event : loadedEvents) {
            if (isEventNameTaken(event.getEventName())) {
                throw new InvalidFileException(FileErrorCode.EVENT_NAME_ALREADY_EXISTS, event.getEventName());
            }
        }
    }

    private boolean isEventNameTaken(String eventName) {
        return events.values().stream().anyMatch(event -> event.getEventName().equalsIgnoreCase(eventName.trim()));
    }

    // A loaded event is checked by the file loader; a created one is checked here.
    private void validateNewEvent(NewEventDto request) {
        validateText(request.getEventName(), EventRequestErrorCode.MISSING_EVENT_NAME);
        if (isEventNameTaken(request.getEventName())) {
            throw new InvalidEventRequestException(EventRequestErrorCode.EVENT_NAME_TAKEN);
        }
        validateText(request.getDescription(), EventRequestErrorCode.MISSING_EVENT_DESCRIPTION);
        validateOptionNames(request.getOptionNames());
        validateCommissionPercent(request.getCommissionPercent());
        validateMethodValues(request);
    }

    private void validateText(String text, EventRequestErrorCode errorCode) {
        if (text == null || text.isBlank()) {
            throw new InvalidEventRequestException(errorCode);
        }
        if (!isEnglish(text)) {
            throw new InvalidEventRequestException(EventRequestErrorCode.TEXT_NOT_ENGLISH);
        }
    }

    // Checked line by line, so that a description may span several lines. Spaces and tabs around each line
    // are ignored, since a description in a file is often indented. The file loader uses it too.
    public static boolean isEnglish(String text) {
        return text.lines().map(String::strip).allMatch(line -> line.isEmpty() || ENGLISH_TEXT.matcher(line).matches());
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
            throw new InvalidEventRequestException(EventRequestErrorCode.MISSING_COMMISSION_POLICY);
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
