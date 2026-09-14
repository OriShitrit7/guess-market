package guessmarket.dto;

// Represents the user's balance after one balance-changing operation.
public final class BalancePointDto {
    private final int sequence;
    private final double balance;

    public BalancePointDto(int sequence, double balance) {
        this.sequence = sequence;
        this.balance = balance;
    }

    public int getSequence() {
        return sequence;
    }

    public double getBalance() {
        return balance;
    }
}
