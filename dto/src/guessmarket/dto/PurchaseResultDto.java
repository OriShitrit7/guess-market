package guessmarket.dto;

public final class PurchaseResultDto {
    private final TradeDto trade;
    private final EventTradingStateDto stateAfterPurchase;

    public PurchaseResultDto(TradeDto trade, EventTradingStateDto stateAfterPurchase) {
        this.trade = trade;
        this.stateAfterPurchase = stateAfterPurchase;
    }

    public TradeDto getTrade() {
        return trade;
    }

    public EventTradingStateDto getStateAfterPurchase() {
        return stateAfterPurchase;
    }
}
