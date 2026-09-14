package guessmarket.dto;

// Summarizes how an Order Book request was executed for the order receipt dialog.
public final class OrderResultDto {
    private final OrderActionDto action;
    // Shares executed against an existing order.
    private final int matchedQuantity;
    // Shares created by pairing buy orders for opposite options.
    private final int mintedQuantity;
    // Shares from the request that remain in the book.
    private final int remainingQuantity;
    // Amount paid or received for matched shares, before commission.
    private final double matchedAmount;
    // Amount paid for minted shares, before commission.
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
