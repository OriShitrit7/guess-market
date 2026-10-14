package guessmarket.dto;

import java.util.List;

// Carries an Order Book event's summary, option state, participants and books. Its price history is sent
// separately, as EventHistoryDto, so that only what is new has to travel.
public final class OrderBookStateDto extends EventStateDto {
    private final List<OptionBookDto> optionBooks;

    public OrderBookStateDto(EventSummaryDto summary, List<OptionStateDto> optionStates, List<ParticipantHoldingsDto> participants,
                             List<OptionBookDto> optionBooks) {
        super(summary, optionStates, participants);
        this.optionBooks = List.copyOf(optionBooks);
    }

    public List<OptionBookDto> getOptionBooks() {
        return optionBooks;
    }
}
