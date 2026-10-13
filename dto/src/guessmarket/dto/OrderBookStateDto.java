package guessmarket.dto;

import java.util.List;

// Carries a complete Order Book event snapshot for its event details and user trading views.
public final class OrderBookStateDto extends EventStateDto {
    private final List<OptionBookDto> optionBooks;
    private final List<List<Double>> priceHistory;

    public OrderBookStateDto(EventSummaryDto summary, List<OptionStateDto> optionStates, List<ParticipantHoldingsDto> participants,
                             List<OptionBookDto> optionBooks, List<List<Double>> priceHistory) {
        super(summary, optionStates, participants);
        this.optionBooks = List.copyOf(optionBooks);
        this.priceHistory = priceHistory.stream()
                .map(List::copyOf)
                .toList();
    }

    public List<OptionBookDto> getOptionBooks() {
        return optionBooks;
    }

    public List<List<Double>> getPriceHistory() {
        return priceHistory;
    }
}
