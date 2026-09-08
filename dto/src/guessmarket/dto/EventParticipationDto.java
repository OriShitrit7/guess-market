package guessmarket.dto;

public final class EventParticipationDto {
    private final EventSummaryDto event;
    private final boolean marketMaker;
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
