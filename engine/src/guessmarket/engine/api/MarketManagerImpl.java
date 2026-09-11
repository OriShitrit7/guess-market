package guessmarket.engine.api;

import guessmarket.dto.*;
import guessmarket.engine.exception.file.InvalidFileException;
import guessmarket.engine.exception.request.EventRequestErrorCode;
import guessmarket.engine.exception.request.InvalidEventRequestException;
import guessmarket.engine.exception.request.InvalidUserRequestException;
import guessmarket.engine.exception.request.NonPositiveQuantityException;
import guessmarket.engine.exception.request.UserRequestErrorCode;
import guessmarket.engine.model.EventStatus;
import guessmarket.engine.model.MarketEvent;
import guessmarket.engine.model.MarketOption;
import guessmarket.engine.model.Trade;
import guessmarket.engine.model.User;
import guessmarket.engine.model.UsersRegistry;
import guessmarket.engine.trading.OrderAction;
import guessmarket.engine.trading.OrderMatch;
import guessmarket.engine.trading.TradingMethodType;
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

    @Override
    public UserInvolvementDto getUserInvolvement(String userName, int eventId) {
        return dtoMapper.createUserInvolvement(getEventById(eventId), getUserByName(userName));
    }

    // Validates and performs a purchase, then returns both the trade and the updated event state.
    @Override
    public PurchaseResultDto buyShares(String userName, int eventId, int optionIndex, int quantity) {
        User buyer = getActiveUserByName(userName);
        MarketEvent event = getEventById(eventId);

        validateEventStatus(event, EventStatus.ACTIVE);
        validateTradingMethod(event, TradingMethodType.LMSR);
        validateOptionIndex(event, optionIndex);
        validateQuantity(quantity);

        Trade trade = event.buyShares(userName, optionIndex, quantity);
        buyer.withdraw(trade.getSharesCost() + trade.getCommissionCost());
        buyer.recordPurchase(eventId, trade);
        payCommissionToMarketMaker(event, trade.getCommissionCost());


        return new PurchaseResultDto(dtoMapper.createTradeDto(trade),
                dtoMapper.createEventTradingState(event));
    }

    @Override
    public OrderResultDto submitOrder(String userName, int eventId, int optionIndex, OrderActionDto actionDto,
                                      int quantity, double pricePerShare) {
        User user = getActiveUserByName(userName);
        MarketEvent event = getEventById(eventId);

        validateEventStatus(event, EventStatus.ACTIVE);
        validateTradingMethod(event, TradingMethodType.ORDER_BOOK);
        validateOptionIndex(event, optionIndex);
        validateQuantity(quantity);
        validateOrderPrice(event, pricePerShare);

        OrderAction action = toOrderAction(actionDto);
        validateAvailableShares(user, event, optionIndex, action, quantity);

        List<OrderMatch> matches = event.submitOrder(userName, optionIndex, action, quantity, pricePerShare);

        user.joinEvent(eventId);
        double totalCommissionPaid = settleOrderMatches(event, matches);

        return createOrderResult(matches, quantity, totalCommissionPaid);
    }

    private OrderResultDto createOrderResult(List<OrderMatch> matches, int quantity,
                                             double totalCommissionPaid) {
        int matchedQuantity = matches.stream().mapToInt(OrderMatch::getQuantity).sum();
        double totalPaid = matches.stream().mapToDouble(OrderMatch::getTotalPaid).sum();

        return new OrderResultDto(matchedQuantity, quantity - matchedQuantity, totalPaid,
                totalCommissionPaid);
    }

    // Applies every match to the two users involved and returns the commission the buyer side paid.
    private double settleOrderMatches(MarketEvent event, List<OrderMatch> matches) {
        double totalCommissionPaid = 0;

        for (OrderMatch match : matches) {
            totalCommissionPaid += settleOrderMatch(event, match);
        }
        return totalCommissionPaid;
    }

    private double settleOrderMatch(MarketEvent event, OrderMatch match) {
        User buyer = getUserByName(match.getBuyerName());
        User seller = getUserByName(match.getSellerName());
        MarketOption option = event.getOptions().get(match.getOptionIndex());

        double tradeAmount = match.getTotalPaid();
        double commission = event.calcPurchaseCommission(tradeAmount);

        seller.removeShares(event.getEventId(), option, match.getQuantity());
        buyer.addShares(event.getEventId(), option, match.getQuantity(), tradeAmount);

        buyer.withdraw(tradeAmount + commission);
        seller.deposit(tradeAmount);
        buyer.recordCommission(event.getEventId(), commission);
        payCommissionToMarketMaker(event, commission);

        return commission;
    }

    private void validateAvailableShares(User user, MarketEvent event, int optionIndex,
                                         OrderAction action, int quantity) {
        if (action != OrderAction.SELL) {
            return;
        }

        MarketOption option = event.getOptions().get(optionIndex);
        int heldQuantity = user.getQuantityInEvent(event.getEventId(), option);
        int pendingQuantity = event.getPendingSellQuantity(user.getName(), optionIndex);

        if (heldQuantity - pendingQuantity < quantity) {
            throw new InvalidUserRequestException(UserRequestErrorCode.INSUFFICIENT_SHARES);
        }
    }

    private OrderAction toOrderAction(OrderActionDto action) {
        if (action == null) {
            throw new InvalidEventRequestException(EventRequestErrorCode.INVALID_ORDER_ACTION);
        }
        return OrderAction.valueOf(action.name());
    }

    private void validateOrderPrice(MarketEvent event, double pricePerShare) {
        if (!event.isValidOrderPrice(pricePerShare)) {
            throw new InvalidEventRequestException(EventRequestErrorCode.INVALID_ORDER_PRICE);
        }
    }

    private void payCommissionToMarketMaker(MarketEvent event, double commissionCost) {
        if (commissionCost > 0) {
            getUserByName(event.getMarketMakerName()).deposit(commissionCost);
            event.collectCommission(commissionCost);
        }
    }

    private void validateTradingMethod(MarketEvent event, TradingMethodType requiredType) {
        if (event.getMethodType() != requiredType) {
            throw new InvalidEventRequestException(EventRequestErrorCode.WRONG_TRADING_METHOD);
        }
    }

    @Override
    public EventTradingStateDto openEvent(String userName, int eventId) {
        User user = getActiveUserByName(userName); // כאן נבדק אם חסום
        MarketEvent event = getEventById(eventId);

        validateMarketMaker(event, user);
        validateEventStatus(event, EventStatus.NOT_STARTED);

        double openingCost = event.calcOpeningCost();
        if (!user.hasSufficientBalance(openingCost)) {
            throw new InvalidUserRequestException(UserRequestErrorCode.INSUFFICIENT_BALANCE);
        }
        user.withdraw(openingCost);
        event.open();
        allocateInitialOrderBookShares(user, event, openingCost);

        return getEventTradingState(eventId);
    }

    private void allocateInitialOrderBookShares(User marketMaker, MarketEvent event, double openingCost) {
        if (event.getMethodType() != TradingMethodType.ORDER_BOOK) {
            return;
        }

        int quantity = event.calcInitialSharesPerOption();

        if (quantity == 0) {
            return;
        }

        double amountPaidPerOption = openingCost / event.getOptionCount();
        for (MarketOption option : event.getOptions()) {
            marketMaker.addShares(event.getEventId(), option, quantity, amountPaidPerOption);
        }
    }


    // Validates and closes an active event, then returns its final state.
    @Override
    public EventTradingStateDto closeEvent(String userName, int eventId, int winningOptionIndex) {
        User user = getActiveUserByName(userName); // כאן נבדק אם חסום
        MarketEvent event = getEventById(eventId);

        validateMarketMaker(event, user);
        validateEventStatus(event, EventStatus.ACTIVE);

        validateOptionIndex(event, winningOptionIndex);

        MarketOption winner = event.getOptions().get(winningOptionIndex);

        payWinners(event, winner, user);
        returnRemainingBalance(event, user);
        event.close(winningOptionIndex);

        return getEventTradingState(eventId);
    }

    private void payWinners(MarketEvent event, MarketOption winner, User marketMaker) {
        for (User user : users.getAllUsers()) {
            payWinner(event, winner, marketMaker, user);
        }
    }

    // Pays one winner their share of the event account, routing the closing commission to the market maker.
    private void payWinner(MarketEvent event, MarketOption winner, User marketMaker, User user) {
        int winningShares = user.getQuantityInEvent(event.getEventId(), winner);

        if (winningShares == 0) {
            return;
        }
        double payout = event.calcWinningPayout(winningShares);
        double commissionCost = event.calcCloseCommission(payout);

        event.recordPayout(payout);
        event.collectCommission(commissionCost);

        user.deposit(payout - commissionCost);
        user.recordCommission(event.getEventId(), commissionCost);
        marketMaker.deposit(commissionCost);
    }

    // Returns whatever is left in the event account to the market maker, once, after all winners are paid.
    private void returnRemainingBalance(MarketEvent event, User marketMaker) {
        double remaining = event.getAccount().getBalance();

        event.recordPayout(remaining);
        marketMaker.deposit(remaining);
    }

    private void validateEventStatus(MarketEvent event, EventStatus requiredStatus) {
        EventStatus actualStatus = event.getStatus();

        if (actualStatus != requiredStatus) {
            throw new InvalidEventRequestException(EventRequestErrorCode.INVALID_EVENT_STATE);
        }
    }

    private void validateMarketMaker(MarketEvent event, User user) {
        if (!event.isMarketMaker(user.getName())) {
            throw new InvalidEventRequestException(EventRequestErrorCode.UNAUTHORIZED_EVENT_ACTION);
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

    private MarketEvent getActiveEventById(int eventId) {
        MarketEvent event = getEventById(eventId);

        if (event.getStatus() != EventStatus.ACTIVE) {
            throw new InvalidEventRequestException(EventRequestErrorCode.INVALID_EVENT_STATE);
        }
        return event;
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

    private Map<Integer, MarketEvent> buildEventMap(List<MarketEvent> loadedEvents) {
        Map<Integer, MarketEvent> eventMap = new LinkedHashMap<>();

        for (MarketEvent event : loadedEvents) {
            eventMap.put(event.getEventId(), event);
        }
        return eventMap;
    }
}
