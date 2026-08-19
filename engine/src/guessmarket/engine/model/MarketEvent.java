package guessmarket.engine.model;

import guessmarket.engine.trading.TradingMethod;

import java.util.List;

// Represents a prediction market event and coordinates its trading and closing operations.
public class MarketEvent {
    public static final int REQUIRED_OPTION_COUNT = 2;

    private final int eventId;
    private final String eventName;
    private final String description;
    private EventStatus status = EventStatus.ACTIVE;

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

    public List<MarketOption> getOptions() {
        return options;
    }

    public MarketOption getWinningOption() {
        return winningOption;
    }

    public EventAccount getAccount() {
        return account;
    }

    public CommissionPolicy getCommissionPolicy() {
        return commissionConfig.getCommissionPolicy();
    }

    public int getCommissionPercentage() {
        return commissionConfig.getPercentage();
    }

    public int getOptionCount() {
        return options.size();
    }

    // Calculates the current value of each option from the number of shares purchased.
    public double[] getOptionsValues() {
        return tradingMethod.calcOptionsValues(getSharesCountPerOption());
    }

    private int[] getSharesCountPerOption() {
        return options.stream()
                .mapToInt(MarketOption::getTotalSharesBought)
                .toArray();
    }

    // Calculates and deposits the initial subsidy required by the trading method.
    public void processInitialSubsidy() {
        account.recordSubsidy(tradingMethod.calcInitialSubsidy(options.size()));
    }

    // Purchases shares in an option and records the resulting payment in the event account.
    public Trade buyShares(int optionIndex, int quantity) {
        MarketOption option = options.get(optionIndex);

        double sharesCost = calcSharesCost(optionIndex, quantity);
        double commissionCost = calcPurchaseCommission(sharesCost);

        option.addShares(quantity);
        Trade trade = new Trade(option, quantity, sharesCost, commissionCost);
        account.recordPurchase(trade);

        return trade;
    }

    // Closes the event, collects any closing commission and pays the winning shares.
    public void close(int winningOptionIndex) {
        MarketOption winner = options.get(winningOptionIndex);

        double commissionCost = commissionConfig.calcCloseCommission(calcInvestedIn(winner));
        double payout = calcPayout(winner, commissionCost);

        account.recordClose(commissionCost, payout);

        winningOption = winner;
        status = EventStatus.CLOSED;
    }

    private double calcPayout(MarketOption winner, double commissionCost) {
        return tradingMethod.calcWinningPayout(winner.getTotalSharesBought()) - commissionCost;
    }

    // Uses share costs only because purchase commission is not part of the winning investment.
    private double calcInvestedIn(MarketOption option) {
        return account.getTradeHistory().stream()
                .filter(trade -> trade.getOption() == option)
                .mapToDouble(Trade::getSharesCost)
                .sum();
    }

    // Calculates the price of a purchase without changing the current event state.
    public double calcSharesCost(int optionIndex, int sharesToBuy) {
        return tradingMethod.calcSharesCost(getSharesCountPerOption(), optionIndex, sharesToBuy);
    }

    // Calculates the purchase commission according to this event's commission policy.
    public double calcPurchaseCommission(double sharesCost) {
        return commissionConfig.calcPurchaseCommission(sharesCost);
    }

}
