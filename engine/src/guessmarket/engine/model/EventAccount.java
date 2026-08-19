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
    void recordSubsidy(double amount) {
        deposit(amount);
    }

    // Records the payment, commission and history entry created by a completed purchase.
    void recordPurchase(Trade trade) {
        deposit(trade.getSharesCost() + trade.getCommissionCost());
        updateTotalCommission(trade.getCommissionCost());
        tradeHistory.add(trade);
    }

    // Records the commission and payout applied when the event is closed.
    void recordClose(double commissionCost, double payout) {
        updateTotalCommission(commissionCost);
        withdraw(payout);
    }

    private void deposit(double amount) {
        balance += amount;
    }

    private void withdraw(double amount) {
        balance -= amount;
    }

    private void updateTotalCommission(double commission) {
        totalCommissionCollected += commission;
    }
}
