package guessmarket.engine.model;

import java.time.Instant;

// Represents one line in a user's account history.
public class AccountTransaction {
    private final AccountTransactionType type;
    // Positive when money entered the account, negative when it left.
    private final double amount;
    private final double balanceAfter;
    private final Instant occurredAt;

    public AccountTransaction(AccountTransactionType type, double amount, double balanceAfter, Instant occurredAt) {
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.occurredAt = occurredAt;
    }

    public AccountTransactionType getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
