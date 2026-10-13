package guessmarket.client.components.account.userstable;

import guessmarket.dto.UserSummaryDto;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

// Adapts a user summary to JavaFX properties used by user cards and lists.
public class UserTableRow {
    private final boolean blocked;
    private final boolean marketMaker;

    private final SimpleStringProperty name;
    private final SimpleStringProperty status;
    private final SimpleDoubleProperty balance;

    public UserTableRow(UserSummaryDto summary) {
        this.blocked = summary.isBlocked();
        this.marketMaker = summary.isMarketMaker();
        this.name = new SimpleStringProperty(summary.getName());
        this.status = new SimpleStringProperty(toDisplayText(summary.isBlocked()));
        this.balance = new SimpleDoubleProperty(summary.getBalance());
    }

    public boolean isBlocked() {
        return blocked;
    }

    public boolean isMarketMaker() {
        return marketMaker;
    }

    public String getName() {
        return name.get();
    }

    public String getStatus() {
        return status.get();
    }

    public double getBalance() {
        return balance.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public SimpleStringProperty statusProperty() {
        return status;
    }

    public SimpleDoubleProperty balanceProperty() {
        return balance;
    }

    private String toDisplayText(boolean blocked) {
        return blocked ? "Blocked" : "Active";
    }

}
