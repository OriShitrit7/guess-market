package guessmarket.dto;

import java.util.List;

public final class UserInvolvementDto {
    private final List<TradeDto> trades;
    private final List<OptionHoldingDto> holdings;
    private final double totalCommissionPaid;
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
