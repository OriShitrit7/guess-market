package guessmarket.engine.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventAccount {
    private double balance;
    private double totalCommissionCollected;
    // initialised here, not in the constructor: a new account always starts with no history
    private final List<Trade> tradeHistory = new ArrayList<>();

    public EventAccount(double balance) {
        this.balance = balance;
    }

    public double getBalance() {
        return balance;
    }

    public double getTotalCommissionCollected() {
        return totalCommissionCollected;
    }

    public List<Trade> getTradeHistory() {
        return Collections.unmodifiableList(tradeHistory);
    }
}
