package guessmarket.ui.javafx.components.users;

import guessmarket.dto.TradeDto;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class TradeTableRow {
    private final SimpleStringProperty optionName;
    private final SimpleIntegerProperty quantity;
    private final SimpleDoubleProperty sharesCost;

    public TradeTableRow(TradeDto trade) {
        this.optionName = new SimpleStringProperty(trade.getOptionName());
        this.quantity = new SimpleIntegerProperty(trade.getQuantity());
        this.sharesCost = new SimpleDoubleProperty(trade.getSharesCost());
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

    public SimpleStringProperty optionNameProperty() {
        return optionName;
    }

    public SimpleIntegerProperty quantityProperty() {
        return quantity;
    }

    public SimpleDoubleProperty sharesCostProperty() {
        return sharesCost;
    }
}
