package guessmarket.client.components.account.userstable;

import guessmarket.client.util.Constants;
import guessmarket.dto.UserSummaryDto;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class UsersTableController {
    private static final String USER_CARD_FXML = "user-card.fxml";
    private static final String EMPTY_USERS_TITLE_BEFORE_LOAD = "No users to display";
    private static final String EMPTY_USERS_MESSAGE_BEFORE_LOAD = "Load an XML file to get started.";
    private static final String EMPTY_USERS_TITLE_AFTER_LOAD = "No users available";
    private static final String EMPTY_USERS_MESSAGE_AFTER_LOAD = "The loaded file does not contain any users.";

    @FXML private VBox userCardsPane;
    @FXML private VBox emptyUsersState;
    @FXML private Label emptyUsersTitleLabel;
    @FXML private Label emptyUsersLabel;

    private Timer timer;
    private TimerTask usersTableRefresher;

    private List<UserTableRow> users = List.of();
    private boolean usersWereLoaded;

    @FXML
    private void initialize() {
        emptyUsersState.managedProperty().bind(emptyUsersState.visibleProperty());
        rebuildUserCards();
    }

    public void startUsersTableRefresher() {
        usersTableRefresher = new UsersTableRefresher(usersList -> Platform.runLater(() -> updateUsers(usersList)));
        timer = new Timer();
        timer.schedule(usersTableRefresher, Constants.REFRESH_RATE, Constants.REFRESH_RATE);
    }

    public void updateUsers(List<UserSummaryDto> summaries) {
        users = summaries.stream().map(UserTableRow::new).toList();
        usersWereLoaded = true;
        rebuildUserCards();
    }

    // Recreates the user cards and updates the message shown when no user is available.
    private void rebuildUserCards() {
        userCardsPane.getChildren().clear();

        for (UserTableRow user : users) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(USER_CARD_FXML));
                Node userCard = loader.load();
                UserCardController controller = loader.getController();

                controller.setUser(user);
                userCardsPane.getChildren().add(userCard);
            } catch (IOException exception) {
                throw new IllegalStateException("Could not load the user card", exception);
            }
        }

        emptyUsersTitleLabel.setText(usersWereLoaded ? EMPTY_USERS_TITLE_AFTER_LOAD : EMPTY_USERS_TITLE_BEFORE_LOAD);
        emptyUsersLabel.setText(usersWereLoaded ? EMPTY_USERS_MESSAGE_AFTER_LOAD : EMPTY_USERS_MESSAGE_BEFORE_LOAD);
        emptyUsersState.setVisible(users.isEmpty());
    }

    public void close() {
        if (usersTableRefresher != null && timer != null) {
            usersTableRefresher.cancel();
            timer.cancel();
        }
    }
}
