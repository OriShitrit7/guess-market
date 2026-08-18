package guessmarket.engine.model;

import guessmarket.engine.trading.TradingMethod;

import java.util.List;

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

    public MarketEvent(int eventId, String eventName, String description, List<MarketOption> options
            , CommissionConfig commissionConfig, TradingMethod tradingMethod) {
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

    public TradingMethod getTradingMethod() {
        return tradingMethod;
    }

    public int getOptionCount() {
        return options.size();
    }

    public double[] getOptionsValues() {
        return tradingMethod.calcOptionsValues(getSharesCountPerOption());
    }

    private int[] getSharesCountPerOption() {
        return options.stream()
                .mapToInt(MarketOption::getTotalSharesBought)
                .toArray();
    }

    public void processInitialSubsidy() {
        account.recordSubsidy(tradingMethod.calcInitialSubsidy(options.size()));
    }

    public Trade buyShares(int optionIndex, int quantity) {
        MarketOption option = options.get(optionIndex);

        double sharesCost = calcSharesCost(optionIndex, quantity);
        double commissionCost = calcPurchaseCommission(sharesCost);

        option.addShares(quantity);
        Trade trade = new Trade(option, quantity, sharesCost, commissionCost);
        account.recordPurchase(trade);

        return trade;
    }

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

    private double calcInvestedIn(MarketOption option) {
        return account.getTradeHistory().stream()
                .filter(trade -> trade.getOption() == option)
                .mapToDouble(Trade::getSharesCost)
                .sum();
    }

    public double calcSharesCost(int optionIndex, int sharesToBuy) {
        return tradingMethod.calcSharesCost(getSharesCountPerOption(), optionIndex, sharesToBuy);
    }

    public double calcPurchaseCommission(double sharesCost) {
        return commissionConfig.calcPurchaseCommission(sharesCost);
    }


}
