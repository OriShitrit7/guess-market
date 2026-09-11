package guessmarket.engine.model;

import java.util.List;

// Owns common event data and lifecycle state; subclasses own their trading behaviour.
public abstract class MarketEvent {
    public static final int REQUIRED_OPTION_COUNT = 2;

    private final int eventId;
    private final String eventName;
    private final String description;
    private final List<MarketOption> options;
    private final CommissionConfig commissionConfig;
    private final EventAccount account = new EventAccount();
    private EventStatus status = EventStatus.NOT_STARTED;
    private String marketMakerName;
    private MarketOption winningOption;

    protected MarketEvent(int eventId, String eventName, String description, List<MarketOption> options,
                          CommissionConfig commissionConfig) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.description = description;
        this.options = List.copyOf(options);
        this.commissionConfig = commissionConfig;
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

    public int getOptionCount() {
        return options.size();
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

    public int getCommissionPercent() {
        return commissionConfig.getCommissionPercent();
    }

    public double calcPurchaseCommission(double amount) {
        return commissionConfig.calcPurchaseCommission(amount);
    }

    public double calcCloseCommission(double payout) {
        return commissionConfig.calcCloseCommission(payout);
    }

    public boolean isMarketMaker(String userName) {
        return userName != null && userName.equals(marketMakerName);
    }

    // Hands the market maker's initial shares to the options and opens the event for trading.
    public final void open() {
        int sharesPerOption = calcInitialSharesPerOption();

        if (sharesPerOption > 0) {
            options.forEach(option -> option.addShares(sharesPerOption));
        }
        status = EventStatus.ACTIVE;
    }

    // Marks the event as resolved. The payouts themselves are performed by the caller.
    public final void close(int winningOptionIndex) {
        winningOption = options.get(winningOptionIndex);
        status = EventStatus.CLOSED;
        afterClose();
    }

    public abstract TradingMethodType getMethodType();

    public abstract double calcOpeningCost();

    public abstract double calcWinningPayout(int winningShares);

    // Shares of every option the market maker receives in return for the opening payment.
    public abstract int calcInitialSharesPerOption();

    // The part of the opening payment attributed to each option's holding.
    public abstract double calcInitialAmountPerOption();

    protected void afterClose() {
        // Most events have no outstanding orders to clear.
    }
}
