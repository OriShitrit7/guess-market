package guessmarket.client.components.account.accountdetails.myevents.lmsrtrade;

import guessmarket.dto.TradeDto;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

// Adapts a user's trade to JavaFX properties used by the trade history table.
public class TradeTableRow {
    private final SimpleStringProperty optionName;
    private final SimpleIntegerProperty quantity;
    private final SimpleDoubleProperty sharesCost;
    private final SimpleDoubleProperty commissionCost;
    private final SimpleDoubleProperty totalPaid;

    public TradeTableRow(TradeDto trade) {
        this.optionName = new SimpleStringProperty(trade.getOptionName());
        this.quantity = new SimpleIntegerProperty(trade.getQuantity());
        this.sharesCost = new SimpleDoubleProperty(trade.getSharesCost());
        this.commissionCost = new SimpleDoubleProperty(trade.getCommissionCost());
        this.totalPaid = new SimpleDoubleProperty(
                trade.getSharesCost() + trade.getCommissionCost());
    }

    public String getOptionName() {
        return optionName.get();
    }

    public int getQuantity() {
        return quantity.get();
    }

    public double getSharesCost() {
        return sharesCost.get();
    }

    public double getCommissionCost() {
        return commissionCost.get();
    }

    public double getTotalPaid() {
        return totalPaid.get();
    }

    public SimpleStringProperty optionNameProperty() {
        return optionName;
    }

    public SimpleIntegerProperty quantityProperty() {
        return quantity;
    }

    public SimpleDoubleProperty sharesCostProperty() {
        return sharesCost;
    }

    public SimpleDoubleProperty commissionCostProperty() {
        return commissionCost;
    }

    public SimpleDoubleProperty totalPaidProperty() {
        return totalPaid;
    }
}
