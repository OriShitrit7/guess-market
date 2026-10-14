package guessmarket.dto;

import java.util.List;

// Carries only what was added to an event's history since the counts the client already holds.
public final class EventHistoryDto {
    // New LMSR purchases in chronological order. Always empty for an Order Book event.
    private final List<TradeDto> newTrades;
    // One list per option, with the price points added since the client's count for that option.
    private final List<List<Double>> newPrices;

    public EventHistoryDto(List<TradeDto> newTrades, List<List<Double>> newPrices) {
        this.newTrades = List.copyOf(newTrades);
        this.newPrices = newPrices.stream()
                .map(List::copyOf)
                .toList();
    }

    public List<TradeDto> getNewTrades() {
        return newTrades;
    }

    public List<List<Double>> getNewPrices() {
        return newPrices;
    }

    public boolean isEmpty() {
        return newTrades.isEmpty() && newPrices.stream().allMatch(List::isEmpty);
    }
}
