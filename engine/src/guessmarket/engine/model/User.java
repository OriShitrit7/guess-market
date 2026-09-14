package guessmarket.engine.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Represents a system user and manages their balance and positions in market events.
public class User {
    private final String name;
    private double balance;
    // Becomes true after the balance first drops below zero.
    private boolean blocked;

    private final Map<Integer, Position> positions = new LinkedHashMap<>();
    // Contains the initial balance followed by every updated balance.
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

    // Returns the shares held in one event option, or zero when no position exists.
    public int getQuantityInEvent(int eventId, MarketOption option) {
        Position position = positions.get(eventId);

        return position == null ? 0 : position.getPositionQuantityByOption(option);
    }

    // Adds money and records the updated balance.
    public void deposit(double amount) {
        balance += amount; // Update the account
        balanceHistory.add(balance); // Record the new balance
    }

    // Removes money, records the balance and blocks the user after an overdraft.
    public void withdraw(double amount) {
        balance -= amount; // Update the account
        balanceHistory.add(balance); // Record the new balance

        if (balance < 0) {
            blocked = true; // Prevent further trading
        }
    }

    public boolean hasSufficientBalance(double amount) {
        return balance >= amount;
    }

    // Records an LMSR purchase in the user's position for the event.
    public void recordPurchase(int eventId, Trade trade) {
        positionFor(eventId).recordPurchase(trade);
    }

    // Adds shares and their purchase cost to the user's event position.
    public void addShares(int eventId, MarketOption option, int quantity, double amountPaid) {
        positionFor(eventId).addShares(option, quantity, amountPaid);
    }

    // Removes sold shares from the user's event position.
    public void removeShares(int eventId, MarketOption option, int quantity) {
        positionFor(eventId).removeShares(option, quantity);
    }

    // Creates an empty position so a user with only a resting order still counts as a participant.
    public void joinEvent(int eventId) {
        positionFor(eventId);
    }

    // Records commission paid by the user in one event.
    public void recordCommission(int eventId, double amount) {
        positionFor(eventId).recordCommission(amount);
    }

    // Records money received from selling shares in one event.
    public void recordSale(int eventId, double amount) {
        positionFor(eventId).recordSale(amount);
    }

    // Records the gross payout received when an event closes.
    public void recordPayout(int eventId, double amount) {
        positionFor(eventId).recordPayout(amount);
    }

    private Position positionFor(int eventId) {
        return positions.computeIfAbsent(eventId, id -> new Position());
    }
}
