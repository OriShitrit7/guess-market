package guessmarket.client.components.main;

import guessmarket.client.components.account.AccountController;
import guessmarket.client.components.chat.ChatController;
import guessmarket.client.components.events.EventsController;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;

import java.io.Closeable;

// Holds the main tabs and starts and stops their refreshers.
public class MainController implements Closeable {
    @FXML private EventsController eventsComponentController;
    @FXML private AccountController accountComponentController;
    @FXML private Tab chatTab;
    @FXML private ChatController chatComponentController;
    private boolean active;

    @FXML
    private void initialize() {
        chatTab.selectedProperty().addListener((_, _, selected) -> {
            if (active) {
                chatComponentController.setPollingEnabled(selected);
            }
        });
    }

    // Starts pulling data from the server for the main screen of the logged-in user.
    public void setActive(String userName) {
        chatComponentController.setUserName(userName);
        eventsComponentController.startEventsRefresher();
        accountComponentController.setActive();
        active = true;
        chatComponentController.setPollingEnabled(chatTab.isSelected());
    }

    @Override
    public void close() {
        eventsComponentController.close();
        accountComponentController.close();
        chatComponentController.close();
    }
}
