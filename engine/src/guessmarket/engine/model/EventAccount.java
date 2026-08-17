package guessmarket.engine.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventAccount {
    private double balance;
    private double totalCommissionCollected;
    // initialised here, not in the constructor: a new account always starts with no history
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

    private void deposit (double amount) {  // זמני
        balance += amount;
    }
}
