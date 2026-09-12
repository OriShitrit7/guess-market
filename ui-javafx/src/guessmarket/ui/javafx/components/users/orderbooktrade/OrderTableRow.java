package guessmarket.ui.javafx.components.users.orderbooktrade;

import guessmarket.dto.OrderEntryDto;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class OrderTableRow {
    private final SimpleStringProperty optionName;
    private final SimpleStringProperty side;
    private final SimpleIntegerProperty quantity;
    private final SimpleDoubleProperty pricePerShare;

    public OrderTableRow(String optionName, String side, OrderEntryDto order) {
        this.optionName = new SimpleStringProperty(optionName);
        this.side = new SimpleStringProperty(side);
        this.quantity = new SimpleIntegerProperty(order.getQuantity());
        this.pricePerShare = new SimpleDoubleProperty(order.getPricePerShare());
    }

    public String getOptionName() {
        return optionName.get();
    }

    public String getSide() {
        return side.get();
    }

    public int getQuantity() {
        return quantity.get();
    }

    public double getPricePerShare() {
        return pricePerShare.get();
    }

    public SimpleStringProperty optionNameProperty() {
        return optionName;
    }

    public SimpleStringProperty sideProperty() {
        return side;
    }

    public SimpleIntegerProperty quantityProperty() {
        return quantity;
    }

    public SimpleDoubleProperty pricePerShareProperty() {
        return pricePerShare;
    }
}
