package guessmarket.dto;

import java.util.List;

// Holds the event data shared by both trading methods. Each method adds its own details in a subclass.
public abstract class EventStateDto {
    private final EventSummaryDto summary;
    private final List<OptionStateDto> optionStates;
    private final List<ParticipantHoldingsDto> participants;

    protected EventStateDto(EventSummaryDto summary, List<OptionStateDto> optionStates, List<ParticipantHoldingsDto> participants) {
        this.summary = summary;
        this.optionStates = List.copyOf(optionStates);
        this.participants = List.copyOf(participants);
    }

    public EventSummaryDto getSummary() {
        return summary;
    }

    public List<OptionStateDto> getOptionStates() {
        return optionStates;
    }

    public List<ParticipantHoldingsDto> getParticipants() {
        return participants;
    }
}
