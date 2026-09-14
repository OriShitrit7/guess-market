package guessmarket.ui.javafx.components.events.lmsrdetails;

import guessmarket.dto.TradeDto;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class EventTradeTableRow {
    private final SimpleStringProperty userName;
    private final SimpleStringProperty optionName;
    private final SimpleIntegerProperty quantity;
    private final SimpleDoubleProperty paid;
    private final SimpleDoubleProperty commission;
    private final SimpleDoubleProperty totalPaid;

    public EventTradeTableRow(TradeDto trade) {
        this.userName = new SimpleStringProperty(trade.getBuyerName());
        this.optionName = new SimpleStringProperty(trade.getOptionName());
        this.quantity = new SimpleIntegerProperty(trade.getQuantity());
        this.paid = new SimpleDoubleProperty(trade.getSharesCost());
        this.commission = new SimpleDoubleProperty(trade.getCommissionCost());
        this.totalPaid = new SimpleDoubleProperty(
                trade.getSharesCost() + trade.getCommissionCost());
    }

    public String getUserName() {
        return userName.get();
    }

    public String getOptionName() {
        return optionName.get();
    }

    public int getQuantity() {
        return quantity.get();
    }

    public double getPaid() {
        return paid.get();
    }

    public double getCommission() {
        return commission.get();
    }

    public double getTotalPaid() {
        return totalPaid.get();
    }

    public SimpleStringProperty userNameProperty() {
        return userName;
    }

    public SimpleStringProperty optionNameProperty() {
        return optionName;
    }

    public SimpleIntegerProperty quantityProperty() {
        return quantity;
    }

    public SimpleDoubleProperty paidProperty() {
        return paid;
    }

    public SimpleDoubleProperty commissionProperty() {
        return commission;
    }

    public SimpleDoubleProperty totalPaidProperty() {
        return totalPaid;
    }
}
