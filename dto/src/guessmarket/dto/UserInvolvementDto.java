package guessmarket.dto;

import java.util.List;

// Carries one user's activity and financial result in an event for the user trading view.
public final class UserInvolvementDto {
    // The user's LMSR purchases, newest first. Empty for an Order Book event.
    private final List<TradeDto> trades;
    private final List<OptionHoldingDto> holdings;
    // Total commission paid by the user in this event, in dollars.
    private final double totalCommissionPaid;
    // The user's net result in the event. It is final only once the event is closed.
    private final double finalProfitLoss;

    public UserInvolvementDto(List<TradeDto> trades, List<OptionHoldingDto> holdings,
                              double totalCommissionPaid, double finalProfitLoss) {
        this.trades = List.copyOf(trades);
        this.holdings = List.copyOf(holdings);
        this.totalCommissionPaid = totalCommissionPaid;
        this.finalProfitLoss = finalProfitLoss;
    }

    public List<TradeDto> getTrades() {
        return trades;
    }

    public List<OptionHoldingDto> getHoldings() {
        return holdings;
    }

    public double getTotalCommissionPaid() {
        return totalCommissionPaid;
    }

    public double getFinalProfitLoss() {
        return finalProfitLoss;
    }
}
