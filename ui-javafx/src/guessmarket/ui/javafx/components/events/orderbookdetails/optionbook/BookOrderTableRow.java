package guessmarket.ui.javafx.components.events.orderbookdetails.optionbook;

import guessmarket.dto.OrderEntryDto;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class BookOrderTableRow {
    private final SimpleStringProperty userName;
    private final SimpleIntegerProperty quantity;
    private final SimpleDoubleProperty pricePerShare;

    public BookOrderTableRow(OrderEntryDto order) {
        this.userName = new SimpleStringProperty(order.getUserName());
        this.quantity = new SimpleIntegerProperty(order.getQuantity());
        this.pricePerShare = new SimpleDoubleProperty(order.getPricePerShare());
    }

    public String getUserName() {
        return userName.get();
    }

    public int getQuantity() {
        return quantity.get();
    }

    public double getPricePerShare() {
        return pricePerShare.get();
    }

    public SimpleStringProperty userNameProperty() {
        return userName;
    }

    public SimpleIntegerProperty quantityProperty() {
        return quantity;
    }

    public SimpleDoubleProperty pricePerShareProperty() {
        return pricePerShare;
    }
}
