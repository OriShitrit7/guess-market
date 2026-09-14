package guessmarket.dto;

import java.util.List;

// Carries a complete Order Book event snapshot for its event details and user trading views.
public final class OrderBookStateDto {
    private final EventSummaryDto summary;
    private final List<OptionBookDto> optionBooks;
    private final List<List<Double>> priceHistory;
    private final List<OptionStateDto> optionStates;
    private final List<ParticipantHoldingsDto> participants;

    public OrderBookStateDto(EventSummaryDto summary, List<OptionBookDto> optionBooks,
                             List<List<Double>> priceHistory, List<OptionStateDto> optionStates,
                             List<ParticipantHoldingsDto> participants) {
        this.summary = summary;
        this.optionBooks = List.copyOf(optionBooks);
        this.priceHistory = priceHistory.stream()
                .map(List::copyOf)
                .toList();
        this.optionStates = List.copyOf(optionStates);
        this.participants = List.copyOf(participants);
    }

    public EventSummaryDto getSummary() {
        return summary;
    }

    public List<OptionBookDto> getOptionBooks() {
        return optionBooks;
    }

    public List<List<Double>> getPriceHistory() {
        return priceHistory;
    }

    public List<OptionStateDto> getOptionStates() {
        return optionStates;
    }

    public List<ParticipantHoldingsDto> getParticipants() {
        return participants;
    }
}
