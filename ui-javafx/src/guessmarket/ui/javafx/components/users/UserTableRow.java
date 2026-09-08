package guessmarket.ui.javafx.components.users;

import guessmarket.dto.UserSummaryDto;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class UserTableRow {
    private final SimpleStringProperty name;
    private final SimpleStringProperty status;
    private final SimpleDoubleProperty balance;

    public UserTableRow(UserSummaryDto summary) {
        this.name = new SimpleStringProperty(summary.getName());
        this.status = new SimpleStringProperty(toDisplayText(summary.isBlocked()));
        this.balance = new SimpleDoubleProperty(summary.getBalance());
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
