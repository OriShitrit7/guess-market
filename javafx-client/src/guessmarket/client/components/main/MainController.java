package guessmarket.client.components.main;

import guessmarket.client.components.account.AccountController;
import guessmarket.client.components.events.EventsController;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;

import java.io.Closeable;

// Coordinates the main window, shared engine and top-level Events and Users views.
public class MainController implements Closeable {
    @FXML private EventsController eventsComponentController;
    @FXML private AccountController accountComponentController;

    private final StringProperty userName = new SimpleStringProperty();

    public void setActive() {
        eventsComponentController.startEventsRefresher();
        accountComponentController.setActive();
    }

    public void setUserName(String username) {
        userName.setValue(username);
    }

    public String getUserName() {
        return userName.getValue();
    }

    @Override
    public void close() {
        eventsComponentController.close();
        accountComponentController.close();
    }

}
