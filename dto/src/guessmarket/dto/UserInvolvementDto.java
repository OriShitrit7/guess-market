package guessmarket.dto;

import java.util.List;

// Carries one user's activity and financial result in an event for the user trading view.
public final class UserInvolvementDto {
    private final List<TradeDto> trades;
    private final List<OptionHoldingDto> holdings;
    // Total commission paid by the user in this event, in dollars.
    private final double totalCommissionPaid;
    // Indicates whether the event is closed and the final profit or loss can be displayed.
    private final boolean finalProfitLossAvailable;
    private final double finalProfitLoss;

    public UserInvolvementDto(List<TradeDto> trades, List<OptionHoldingDto> holdings,
                              double totalCommissionPaid, boolean finalProfitLossAvailable,
                              double finalProfitLoss) {
        this.trades = List.copyOf(trades);
        this.holdings = List.copyOf(holdings);
        this.totalCommissionPaid = totalCommissionPaid;
        this.finalProfitLossAvailable = finalProfitLossAvailable;
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

    public boolean isFinalProfitLossAvailable() {
        return finalProfitLossAvailable;
    }

    public double getFinalProfitLoss() {
        return finalProfitLoss;
    }
}
