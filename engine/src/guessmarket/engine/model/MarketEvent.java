package guessmarket.engine.model;

import guessmarket.engine.trading.*;

import java.util.List;

// Represents a prediction market event and coordinates its trading and closing operations.
public class MarketEvent {
    public static final int REQUIRED_OPTION_COUNT = 2;

    private final int eventId;
    private final String eventName;
    private final String description;
    private EventStatus status = EventStatus.NOT_STARTED;

    private String marketMakerName;

    private final List<MarketOption> options;
    private MarketOption winningOption;

    private final EventAccount account = new EventAccount();
    private final CommissionConfig commissionConfig;
    private final TradingMethod tradingMethod;

    public MarketEvent(int eventId, String eventName, String description, List<MarketOption> options,
                       CommissionConfig commissionConfig, TradingMethod tradingMethod) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.description = description;
        this.options = List.copyOf(options);
        this.commissionConfig = commissionConfig;
        this.tradingMethod = tradingMethod;
    }

    public int getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public String getDescription() {
        return description;
    }

    public EventStatus getStatus() {
        return status;
    }

    public String getMarketMakerName() {
        return marketMakerName;
    }

    public void setMarketMakerName(String marketMakerName) {
        this.marketMakerName = marketMakerName;
    }
    
    public List<MarketOption> getOptions() {
        return options;
    }

    public MarketOption getWinningOption() {
        return winningOption;
    }

    public EventAccount getAccount() {
        return account;
    }

    public TradingMethodType getMethodType() {
        return tradingMethod.getType();
    }

    public CommissionPolicy getCommissionPolicy() {
        return commissionConfig.getCommissionPolicy();
    }

    public int getCommissionPercent() {
        return commissionConfig.getCommissionPercent();
    }

    public int getOptionCount() {
        return options.size();
    }

    // Calculates the current value of each option from the number of shares purchased.
    public double[] getOptionsValues() {
        return getLmsrTradingMethod().calcOptionsValues(getSharesCountPerOption());
    }

    public boolean isMarketMaker(String userName) {
        return userName.equals(marketMakerName);
    }

     public void open() {
        int sharesPerOption = tradingMethod.calcInitialSharesPerOption();

        account.processSubsidy(calcOpeningCost());
        options.forEach(option -> option.addShares(sharesPerOption));

        status = EventStatus.ACTIVE;
    }

    // Marks the event as resolved. The payouts themselves are performed by the caller.
    public void close(int winningOptionIndex) {
        winningOption = options.get(winningOptionIndex);
        status = EventStatus.CLOSED;
    }

    public void recordPayout(double amount) {
        account.payOut(amount);
    }

    public void collectCommission(double amount) {
        account.collectCommission(amount);
    }

    // Purchases shares in an option and records the resulting payment in the event account.
    public Trade buyShares(String buyerName, int optionIndex, int quantity) {
        MarketOption option = options.get(optionIndex);

        double sharesCost = calcSharesCost(optionIndex, quantity);
        double commissionCost = commissionConfig.calcPurchaseCommission(sharesCost);

        option.addShares(quantity);
        Trade trade = new Trade(buyerName, option, quantity, sharesCost, commissionCost);
        account.processPurchase(trade);

        return trade;
    }
    public List<OrderMatch> submitOrder(String userName, int optionIndex, OrderAction action,
                                        int quantity, double pricePerShare) {
        return getOrderBookTradingMethod()
                .submitOrder(userName, optionIndex, action, quantity, pricePerShare);
    }

    public boolean isValidOrderPrice(double pricePerShare) {
        return getOrderBookTradingMethod().isValidOrderPrice(pricePerShare);
    }

    public int getPendingSellQuantity(String userName, int optionIndex) {
        return getOrderBookTradingMethod().getPendingSellQuantity(userName, optionIndex);
    }

    public double calcPurchaseCommission(double amount) {
        return commissionConfig.calcPurchaseCommission(amount);
    }

    public int calcInitialSharesPerOption() {
        return tradingMethod.calcInitialSharesPerOption();
    }

    private OrderBookTradingMethod getOrderBookTradingMethod() {
        if (tradingMethod instanceof OrderBookTradingMethod orderBookTradingMethod) {
            return orderBookTradingMethod;
        }
        throw new IllegalStateException("Event '" + eventName + "' does not use the Order Book trading method");
    }
    public double calcOpeningCost() {
        return tradingMethod.calcOpeningCost(options.size());
    }

    // Calculates the price of a purchase without changing the current event state.
    private double calcSharesCost(int optionIndex, int sharesToBuy) {
        return getLmsrTradingMethod().calcSharesCost(getSharesCountPerOption(), optionIndex, sharesToBuy);
    }


    public double calcWinningPayout(int winningShares) {
        return tradingMethod.calcWinningPayout(winningShares);
    }

    public double calcCloseCommission(double payout) {
        return commissionConfig.calcCloseCommission(payout);
    }

    private int[] getSharesCountPerOption() {
        return options.stream()
                .mapToInt(MarketOption::getTotalSharesBought)
                .toArray();
    }

    private LmsrTradingMethod getLmsrTradingMethod() {
        if (tradingMethod instanceof LmsrTradingMethod lmsrTradingMethod) {
            return lmsrTradingMethod;
        }
        throw new IllegalStateException("Event '" + eventName + "' does not use the LMSR trading method");
    }

}
