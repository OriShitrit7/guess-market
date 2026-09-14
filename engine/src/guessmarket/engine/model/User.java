package guessmarket.engine.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class User {
    private final String name;
    private double balance;
    private boolean blocked;

    private final Map<Integer, Position> positions = new LinkedHashMap<>();
    private final List<Double> balanceHistory = new ArrayList<>();

    public User(String name, double balance) {
        this.name = name;
        this.balance = balance;
        balanceHistory.add(balance);
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

    public List<Double> getBalanceHistory() {
        return List.copyOf(balanceHistory);
    }

    public Map<Integer, Position> getPositions() {
        return Collections.unmodifiableMap(positions);
    }

    public int getQuantityInEvent(int eventId, MarketOption option) {
        Position position = positions.get(eventId);

        return position == null ? 0 : position.getPositionQuantityByOption(option);
    }

    public void deposit(double amount) {
        balance += amount;
        balanceHistory.add(balance);
    }

    public void withdraw(double amount) {
        balance -= amount;
        balanceHistory.add(balance);

        if (balance < 0) {
            blocked = true;
        }
    }

    public boolean hasSufficientBalance(double amount) {
        return balance >= amount;
    }

    public void recordPurchase(int eventId, Trade trade) {
        positionFor(eventId).recordPurchase(trade);
    }

    public void addShares(int eventId, MarketOption option, int quantity, double amountPaid) {
        positionFor(eventId).addShares(option, quantity, amountPaid);
    }

    public void removeShares(int eventId, MarketOption option, int quantity) {
        positionFor(eventId).removeShares(option, quantity);
    }

    // Creates an empty position so a user with only a resting order still counts as a participant.
    public void joinEvent(int eventId) {
        positionFor(eventId);
    }

    public void recordCommission(int eventId, double amount) {
        positionFor(eventId).recordCommission(amount);
    }

    public void recordSale(int eventId, double amount) {
        positionFor(eventId).recordSale(amount);
    }

    public void recordPayout(int eventId, double amount) {
        positionFor(eventId).recordPayout(amount);
    }

    private Position positionFor(int eventId) {
        return positions.computeIfAbsent(eventId, id -> new Position());
    }
}
