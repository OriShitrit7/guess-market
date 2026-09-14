package guessmarket.dto;

// Carries one point in a user's balance history for the balance history chart.
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
