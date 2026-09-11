package guessmarket.dto;

import java.util.List;

public final class UserInvolvementDto {
    private final List<TradeDto> trades;
    private final double totalCommissionPaid;

    public UserInvolvementDto(List<TradeDto> trades, double totalCommissionPaid) {
        this.trades = List.copyOf(trades);
        this.totalCommissionPaid = totalCommissionPaid;
    }

    public List<TradeDto> getTrades() {
        return trades;
    }

    public double getTotalCommissionPaid() {
        return totalCommissionPaid;
    }
}
