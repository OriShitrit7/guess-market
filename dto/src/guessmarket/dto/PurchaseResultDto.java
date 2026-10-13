package guessmarket.dto;

// Groups a completed LMSR purchase with the event state immediately after it.
public final class PurchaseResultDto {
    private final TradeDto trade;
    private final LmsrStateDto stateAfterPurchase;
    private final double balanceAfter;

    public PurchaseResultDto(TradeDto trade, LmsrStateDto stateAfterPurchase, double balanceAfter) {
        this.trade = trade;
        this.stateAfterPurchase = stateAfterPurchase;
        this.balanceAfter = balanceAfter;
    }

    public TradeDto getTrade() {
        return trade;
    }

    public LmsrStateDto getStateAfterPurchase() {
        return stateAfterPurchase;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }
}
