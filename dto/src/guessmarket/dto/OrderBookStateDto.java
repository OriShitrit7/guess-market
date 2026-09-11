package guessmarket.dto;

import java.util.List;

// Carries a complete snapshot of an order book event: its books and everyone taking part in it.
public final class OrderBookStateDto {
    private final EventSummaryDto summary;
    private final List<OptionBookDto> optionBooks;
    private final List<EventParticipantDto> participants;
    private final double totalCommissionCollected;
    private final String winningOptionName;

    public OrderBookStateDto(EventSummaryDto summary, List<OptionBookDto> optionBooks,
                             List<EventParticipantDto> participants, double totalCommissionCollected,
                             String winningOptionName) {
        this.summary = summary;
        this.optionBooks = List.copyOf(optionBooks);
        this.participants = List.copyOf(participants);
        this.totalCommissionCollected = totalCommissionCollected;
        this.winningOptionName = winningOptionName;
    }

    public EventSummaryDto getSummary() {
        return summary;
    }

    public List<OptionBookDto> getOptionBooks() {
        return optionBooks;
    }

    public List<EventParticipantDto> getParticipants() {
        return participants;
    }

    public double getTotalCommissionCollected() {
        return totalCommissionCollected;
    }

    public String getWinningOptionName() {
        return winningOptionName;
    }
}
