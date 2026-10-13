package guessmarket.client.components.account.accountdetails.myevents.orderbooktrade;

import guessmarket.dto.OptionHoldingDto;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

// Adapts an option holding to JavaFX properties used by the holdings table.
public class HoldingTableRow {
    private final SimpleStringProperty optionName;
    private final SimpleIntegerProperty quantity;
    private final SimpleDoubleProperty amountPaid;

    public HoldingTableRow(OptionHoldingDto holding) {
        this.optionName = new SimpleStringProperty(holding.getOptionName());
        this.quantity = new SimpleIntegerProperty(holding.getQuantity());
        this.amountPaid = new SimpleDoubleProperty(holding.getAmountPaid());
    }

    public String getOptionName() {
        return optionName.get();
    }

    public int getQuantity() {
        return quantity.get();
    }

    public double getAmountPaid() {
        return amountPaid.get();
    }

    public SimpleStringProperty optionNameProperty() {
        return optionName;
    }

    public SimpleIntegerProperty quantityProperty() {
        return quantity;
    }

    public SimpleDoubleProperty amountPaidProperty() {
        return amountPaid;
    }
}
