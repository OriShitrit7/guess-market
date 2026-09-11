package guessmarket.dto;

public final class OrderResultDto {
    private final OrderActionDto action;
    private final int matchedQuantity;
    private final int remainingQuantity;
    private final double tradeAmount;
    private final double totalCommissionPaid;

    public OrderResultDto(OrderActionDto action, int matchedQuantity, int remainingQuantity,
                          double tradeAmount, double totalCommissionPaid) {
        this.action = action;
        this.matchedQuantity = matchedQuantity;
        this.remainingQuantity = remainingQuantity;
        this.tradeAmount = tradeAmount;
        this.totalCommissionPaid = totalCommissionPaid;
    }

    public OrderActionDto getAction() {
        return action;
    }

    public int getMatchedQuantity() {
        return matchedQuantity;
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    // Executed value excluding commission: paid on BUY and received on SELL.
    public double getTradeAmount() {
        return tradeAmount;
    }

    public double getTotalCommissionPaid() {
        return totalCommissionPaid;
    }
}
