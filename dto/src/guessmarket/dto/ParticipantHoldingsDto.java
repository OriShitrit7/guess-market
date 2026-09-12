package guessmarket.dto;

import java.util.List;

// Represents one participant in an event, including a user whose orders have not executed yet.
public final class ParticipantHoldingsDto {
    private final String userName;
    private final List<OptionHoldingDto> holdings;
    // How many of this participant's orders are still waiting in the books.
    private final int openOrders;

    public ParticipantHoldingsDto(String userName, List<OptionHoldingDto> holdings, int openOrders) {
        this.userName = userName;
        this.holdings = List.copyOf(holdings);
        this.openOrders = openOrders;
    }

    public String getUserName() {
        return userName;
    }

    public List<OptionHoldingDto> getHoldings() {
        return holdings;
    }

    public int getOpenOrders() {
        return openOrders;
    }
}
