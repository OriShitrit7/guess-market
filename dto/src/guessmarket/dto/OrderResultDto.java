package guessmarket.dto;

public final class OrderResultDto {
    private final OrderActionDto action;
    private final int matchedQuantity;
    // Shares created against demand for the opposite option, rather than bought from a seller.
    private final int mintedQuantity;
    private final int remainingQuantity;
    private final double matchedAmount;
    private final double mintedAmount;
    private final double totalCommissionPaid;

    public OrderResultDto(OrderActionDto action, int matchedQuantity, int mintedQuantity,
                          int remainingQuantity, double matchedAmount, double mintedAmount,
                          double totalCommissionPaid) {
        this.action = action;
        this.matchedQuantity = matchedQuantity;
        this.mintedQuantity = mintedQuantity;
        this.remainingQuantity = remainingQuantity;
        this.matchedAmount = matchedAmount;
        this.mintedAmount = mintedAmount;
        this.totalCommissionPaid = totalCommissionPaid;
    }

    public OrderActionDto getAction() {
        return action;
    }

    public int getMatchedQuantity() {
        return matchedQuantity;
    }

    public int getMintedQuantity() {
        return mintedQuantity;
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    // Value of the matched shares, excluding commission: paid on BUY and received on SELL.
    public double getMatchedAmount() {
        return matchedAmount;
    }

    public double getMintedAmount() {
        return mintedAmount;
    }

    public double getTotalCommissionPaid() {
        return totalCommissionPaid;
    }
}
