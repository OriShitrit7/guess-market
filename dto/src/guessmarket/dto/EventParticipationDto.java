package guessmarket.dto;

// Combines an event summary with one user's roles in that event for the user events table.
public final class EventParticipationDto {
    private final EventSummaryDto event;
    private final boolean marketMaker;
    // True when the user has a position in the event, even one that holds only waiting orders.
    private final boolean participant;

    public EventParticipationDto(EventSummaryDto event, boolean marketMaker, boolean participant) {
        this.event = event;
        this.marketMaker = marketMaker;
        this.participant = participant;
    }

    public EventSummaryDto getEvent() {
        return event;
    }

    public boolean isMarketMaker() {
        return marketMaker;
    }

    public boolean isParticipant() {
        return participant;
    }
}
