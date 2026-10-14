package guessmarket.dto;

import java.util.List;

// Carries an LMSR event's summary, option state and participants. Its trade and price history is sent
// separately, as EventHistoryDto, so that only what is new has to travel.
public final class LmsrStateDto extends EventStateDto {

    public LmsrStateDto(EventSummaryDto summary, List<OptionStateDto> optionStates, List<ParticipantHoldingsDto> participants) {
        super(summary, optionStates, participants);
    }
}
