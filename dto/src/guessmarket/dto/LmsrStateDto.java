package guessmarket.dto;

import java.util.List;

// Carries an event's summary, option state, LMSR history and participants for its detail views.
public final class LmsrStateDto extends EventStateDto {
    private final List<List<Double>> sharePriceHistory;
    private final List<TradeDto> tradeHistory;

    public LmsrStateDto(EventSummaryDto summary, List<OptionStateDto> optionStates, List<ParticipantHoldingsDto> participants,
                        List<List<Double>> sharePriceHistory, List<TradeDto> tradeHistory) {
        super(summary, optionStates, participants);
        this.sharePriceHistory = sharePriceHistory.stream()
                .map(List::copyOf)
                .toList();
        this.tradeHistory = List.copyOf(tradeHistory);
    }

    public List<List<Double>> getSharePriceHistory() {
        return sharePriceHistory;
    }

    public List<TradeDto> getTradeHistory() {
        return tradeHistory;
    }
}
