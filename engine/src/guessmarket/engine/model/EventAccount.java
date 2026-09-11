package guessmarket.engine.model;

// Tracks event funds and commissions; trading history belongs to the event.
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
