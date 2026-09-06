package guessmarket.dto;

import java.util.List;

// Carries a complete snapshot of an event's current trading and account state.
public final class EventTradingStateDto {
    private final EventSummaryDto summary;
    private final List<OptionStateDto> options;
    private final double totalCommissionCollected;
    private final List<TradeDto> tradeHistory;
    private final String winningOptionName;

    public EventTradingStateDto(EventSummaryDto summary, List<OptionStateDto> options, double totalCommissionCollected,
                                List<TradeDto> tradeHistory, String winningOptionName) {
        this.summary = summary;
        this.options = List.copyOf(options);
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
