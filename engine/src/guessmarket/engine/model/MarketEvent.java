package guessmarket.engine.model;

import guessmarket.engine.trading.TradingMethod;

import java.util.List;

public class MarketEvent {
    private final int eventId;
    private final String eventName;
    private final String description;
    private EventStatus status = EventStatus.ACTIVE;

    private final List<MarketOption> options;
    private MarketOption winningOption;

    private final EventAccount account;
    private final CommissionConfig commissionConfig;
    private final TradingMethod tradingMethod;

    public MarketEvent(int eventId, String eventName, String description, List<MarketOption> options,
                       EventAccount account, CommissionConfig commissionConfig, TradingMethod tradingMethod) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.description = description;
        this.options = List.copyOf(options);
        this.account = account;
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

    public CommissionConfig getCommissionConfig() {
        return commissionConfig;
    }

    public TradingMethod getTradingMethod() {
        return tradingMethod;
    }

    public double[] getOptionsValues() {
        int[] quantities = new int[options.size()];
        for (int i = 0; i < options.size(); i++) {
            quantities[i] = options.get(i).getTotalSharesBought();
        }
        return tradingMethod.calcOptionsValues(quantities);
    }
}
