package guessmarket.dto;

import java.util.List;

public final class AccountDetailsDto {
    private final UserSummaryDto summary;
    private final List<AccountTransactionDto> transactions;
    private final List<EventParticipationDto> events;

    public AccountDetailsDto(UserSummaryDto summary, List<AccountTransactionDto> transactions, List<EventParticipationDto> events) {
        this.summary = summary;
        this.transactions = List.copyOf(transactions);
        this.events = List.copyOf(events);
    }

    public UserSummaryDto getSummary() {
        return summary;
    }

    public List<AccountTransactionDto> getTransactions() {
        return transactions;
    }

    public List<EventParticipationDto> getEvents() {
        return events;
    }
}
