package guessmarket.dto;

// Carries the account details displayed in the users table and trading panels.
public final class UserSummaryDto {
    private final String name;
    private final double balance;
    private final boolean blocked;
    // Whether the user is the market maker of at least one event.
    private final boolean marketMaker;

    public UserSummaryDto(String name, double balance, boolean blocked, boolean marketMaker) {
        this.name = name;
        this.balance = balance;
        this.blocked = blocked;
        this.marketMaker = marketMaker;
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

    public boolean isMarketMaker() {
        return marketMaker;
    }
}
