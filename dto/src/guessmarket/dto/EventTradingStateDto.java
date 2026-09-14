package guessmarket.dto;

import java.util.List;

// Carries a complete snapshot of an event's current trading and account state.
public final class EventTradingStateDto {
    private final EventSummaryDto summary;
    private final List<OptionStateDto> options;
    private final List<List<Double>> sharePriceHistory;
    private final List<TradeDto> tradeHistory;
    private final List<ParticipantHoldingsDto> participants;

    public EventTradingStateDto(EventSummaryDto summary, List<OptionStateDto> options,
                                List<List<Double>> sharePriceHistory, List<TradeDto> tradeHistory,
                                List<ParticipantHoldingsDto> participants) {
        this.summary = summary;
        this.options = List.copyOf(options);
        this.sharePriceHistory = sharePriceHistory.stream()
                .map(List::copyOf)
                .toList();
        this.tradeHistory = List.copyOf(tradeHistory);
        this.participants = List.copyOf(participants);
    }

    public EventSummaryDto getSummary() {
        return summary;
    }

    public List<OptionStateDto> getOptions() {
        return options;
    }

    public List<List<Double>> getSharePriceHistory() {
        return sharePriceHistory;
    }

    public List<TradeDto> getTradeHistory() {
        return tradeHistory;
    }

    public List<ParticipantHoldingsDto> getParticipants() {
        return participants;
    }
}
