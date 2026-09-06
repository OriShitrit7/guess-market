package guessmarket.engine.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class User {
    private final String name;
    private double balance;
    private boolean blocked;

    private final Map<Integer, Position> positions = new LinkedHashMap<>();

    public User(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public Map<Integer, Position> getPositions() {
        return Collections.unmodifiableMap(positions);
    }

    public void deposit(double amount) {
        balance += amount;
    }

    public void withdraw(double amount) {
        balance -= amount;

        if (balance < 0) {
            blocked = true;
        }
    }

    public void recordPurchase(int eventId, Trade trade) {
        positionFor(eventId).recordPurchase(trade);
    }

    public void recordCommission(int eventId, double amount) {
        positionFor(eventId).recordCommission(amount);
    }

    private Position positionFor(int eventId) {
        return positions.computeIfAbsent(eventId, id -> new Position());
    }
}
