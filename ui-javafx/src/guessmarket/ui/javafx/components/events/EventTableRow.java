package guessmarket.ui.javafx.components.events;

import guessmarket.dto.EventSummaryDto;
import guessmarket.dto.TradingMethodTypeDto;
import guessmarket.ui.javafx.components.DisplayText;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class EventTableRow {
    private final int eventId;
    private final boolean orderBook;

    private final SimpleStringProperty name;
    private final SimpleStringProperty status;
    private final SimpleStringProperty methodType;
    private final SimpleIntegerProperty commissionPercent;
    private final SimpleStringProperty commissionPolicy;
    private final SimpleDoubleProperty accountBalance;

    public EventTableRow(EventSummaryDto summary) {
        this.eventId = summary.getEventId();
        this.orderBook = summary.getMethodType() == TradingMethodTypeDto.ORDER_BOOK;
        this.name = new SimpleStringProperty(summary.getEventName());
        this.status = new SimpleStringProperty(DisplayText.of(summary.getStatus()));
        this.methodType = new SimpleStringProperty(DisplayText.of(summary.getMethodType()));
        this.commissionPercent = new SimpleIntegerProperty(summary.getCommissionPercent());
        this.commissionPolicy = new SimpleStringProperty(DisplayText.of(summary.getCommissionPolicy()));
        this.accountBalance = new SimpleDoubleProperty(summary.getAccountBalance());
    }

    public int getEventId() {
        return eventId;
    }

    public boolean isOrderBook() {
        return orderBook;
    }

    public String getName() {
        return name.get();
    }

    public String getMethodType() {
        return methodType.get();
    }

    public String getStatus() {
        return status.get();
    }

    public SimpleStringProperty statusProperty() {
        return status;
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public int getCommissionPercent() {
        return commissionPercent.get();
    }

    public String getCommissionPolicy() {
        return commissionPolicy.get();
    }

    public double getAccountBalance() {
        return accountBalance.get();
    }

    public SimpleDoubleProperty accountBalanceProperty() {
        return accountBalance;
    }

    public void setAccountBalance(double accountBalance) {
        this.accountBalance.set(accountBalance);
    }
}
