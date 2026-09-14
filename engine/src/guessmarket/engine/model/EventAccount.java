package guessmarket.engine.model;

// Tracks the funds held for an event and the total commission collected.
public class EventAccount {
    private double balance;
    private double totalCommissionCollected;

    public double getBalance() {
        return balance;
    }

    public double getTotalCommissionCollected() {
        return totalCommissionCollected;
    }

    void deposit(double amount) {
        balance += amount;
    }

    void payOut(double amount) {
        balance -= amount;
    }

    void collectCommission(double amount) {
        totalCommissionCollected += amount;
    }
}
