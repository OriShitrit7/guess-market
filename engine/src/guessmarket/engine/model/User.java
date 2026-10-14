package guessmarket.engine.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Represents a system user and manages their balance and positions in market events.
public class User {
    private static final int INITIAL_BALANCE = 0;

    private final String name;
    private double balance = INITIAL_BALANCE;
    // Set when a withdrawal leaves the balance negative. Loading enough funds releases it.
    private boolean blocked;

    private final Map<Integer, Position> positions = new LinkedHashMap<>();
    private final List<AccountTransaction> transactions = new ArrayList<>();

    public User(String name) {
        this.name = name;
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

    public List<AccountTransaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    // Returns the shares held in one event option, or zero when no position exists.
    public int getQuantityInEvent(int eventId, MarketOption option) {
        Position position = positions.get(eventId);

        return position == null ? 0 : position.getPositionQuantityByOption(option);
    }

    // Adds money to the balance. Recording the account transaction is left to the caller.
    public void deposit(double amount) {
        balance += amount; // Update the account
    }

    // Removes money and blocks the user when the balance becomes negative.
    public void withdraw(double amount) {
        balance -= amount; // Update the account

        if (balance < 0) {
            blocked = true; // Prevent further trading
        }
    }

    // Lifts the block once the balance is no longer negative.
    public void releaseBlockIfSolvent() {
        if (balance >= 0) {
            blocked = false;
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

    // Adds a line to the account history, with the balance at that moment. A zero amount is not recorded.
    public void recordTransaction(AccountTransactionType type, double amount) {
        if (amount == 0) {
            return;
        }
        transactions.add(new AccountTransaction(type, amount, balance, Instant.now()));
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
