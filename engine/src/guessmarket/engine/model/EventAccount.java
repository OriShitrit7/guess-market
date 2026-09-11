package guessmarket.engine.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Tracks the balance, collected commission and trade history of a market event.
public class EventAccount {
    private double balance;
    private double totalCommissionCollected;
    private final List<Trade> tradeHistory = new ArrayList<>();

    public double getBalance() {
        return balance;
    }

    public double getTotalCommissionCollected() {
        return totalCommissionCollected;
    }

    public List<Trade> getTradeHistory() {
        return Collections.unmodifiableList(tradeHistory);
    }

    // Adds the initial market subsidy to the event balance.
    void processSubsidy(double amount) {
        deposit(amount);
    }

    // Process the payment, commission and history entry created by a completed purchase.
    void processPurchase(Trade trade) {
        deposit(trade.getSharesCost());
        tradeHistory.add(trade);
    }

    // Records a payout leaving the event account.
    void payOut(double amount) {
        balance -= amount;
    }

    // Records commission collected from a payout on behalf of the market maker.
    void collectCommission(double amount) {
        totalCommissionCollected += amount;
    }

    private void deposit(double amount) {
        balance += amount;
    }
}
