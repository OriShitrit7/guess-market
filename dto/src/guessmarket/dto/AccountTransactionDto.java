package guessmarket.dto;

public final class AccountTransactionDto {
    private final AccountTransactionTypeDto type;
    private final double amount;
    private final double balanceAfter;
    private final long timestamp;

    public AccountTransactionDto(AccountTransactionTypeDto type, double amount, double balanceAfter, long timestamp) {
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
        this.balanceAfter = balanceAfter;
    }

    public AccountTransactionTypeDto getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
