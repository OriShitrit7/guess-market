package guessmarket.dto;

import java.util.List;

// Carries a complete snapshot of an event's current trading and account state.
public final class EventTradingStateDto {
    private final EventSummaryDto summary;
    private final List<OptionStateDto> options;
    // Current event account balance in dollars.
    private final double accountBalance;
    // Total commission collected by the event account in dollars.
    private final double totalCommissionCollected;
    private final List<TradeDto> tradeHistory;
    // Remains null until the event is closed.
    private final String winningOptionName;

    public EventTradingStateDto(EventSummaryDto summary, List<OptionStateDto> options,
                                double accountBalance, double totalCommissionCollected,
                                List<TradeDto> tradeHistory, String winningOptionName) {
        this.summary = summary;
        this.options = List.copyOf(options);
        this.accountBalance = accountBalance;
        this.totalCommissionCollected = totalCommissionCollected;
        this.tradeHistory = List.copyOf(tradeHistory);
        this.winningOptionName = winningOptionName;
    }

    public EventSummaryDto getSummary() {
        return summary;
    }

    public List<OptionStateDto> getOptions() {
        return options;
    }

    public double getAccountBalance() {
        return accountBalance;
    }

    public double getTotalCommissionCollected() {
        return totalCommissionCollected;
    }

    public List<TradeDto> getTradeHistory() {
        return tradeHistory;
    }

    public String getWinningOptionName() {
        return winningOptionName;
    }
}
