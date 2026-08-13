package guessmarket.engine.api;

import java.util.List;

public class EventTradingState {
    private final EventSummary summary;
    private final List<OptionTradingInfo> options;
    private final double accountBalance;
    private final double totalCommissionCollected;
    private final List<TradeInfo> tradeHistory;
    private final String winningOptionName;

    public EventTradingState(EventSummary summary, List<OptionTradingInfo> options,
                             double accountBalance, double totalCommissionCollected,
                             List<TradeInfo> tradeHistory, String winningOptionName) {
        this.summary = summary;
        this.options = List.copyOf(options);
        this.accountBalance = accountBalance;
        this.totalCommissionCollected = totalCommissionCollected;
        this.tradeHistory = List.copyOf(tradeHistory);
        this.winningOptionName = winningOptionName;
    }

    public EventSummary getSummary() {
        return summary;
    }

    public List<OptionTradingInfo> getOptions() {
        return options;
    }

    public double getAccountBalance() {
        return accountBalance;
    }

    public double getTotalCommissionCollected() {
        return totalCommissionCollected;
    }

    public List<TradeInfo> getTradeHistory() {
        return tradeHistory;
    }

    public String getWinningOptionName() {
        return winningOptionName;
    }
}
