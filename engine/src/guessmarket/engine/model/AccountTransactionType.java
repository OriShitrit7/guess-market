package guessmarket.engine.model;

public enum AccountTransactionType {
    LOAD_FUNDS,
    EVENT_OPENING,
    SHARES_PURCHASE,
    SHARES_SALE,
    COMMISSION_RECEIVED,
    WINNING_PAYOUT,
    // What the market maker receives at close: the closing commission and the rest of the event account.
    EVENT_CLOSING
}
