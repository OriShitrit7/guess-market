package guessmarket.client.components.account.userstable;

import guessmarket.client.components.DisplayText;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;

// Presents a user's status, Market Maker role and balance in the users list.
public class UserCardController {
    private static final List<String> STATUS_STYLE_CLASSES =
            List.of("user-status-active", "user-status-blocked");

    @FXML
    private VBox userCard;
    @FXML
    private Label userNameLabel;
    @FXML
    private Label userBalanceLabel;
    @FXML
    private Label marketMakerLabel;
    @FXML
    private Label userStatusLabel;

    @FXML
    private void initialize() {
        marketMakerLabel.managedProperty().bind(marketMakerLabel.visibleProperty());
    }

    // Fills the card with the user's name, status, market maker role and balance.
    public void setUser(UserTableRow user) {
        userNameLabel.setText(user.getName());
        userBalanceLabel.setText(DisplayText.amount(user.getBalance()));
        marketMakerLabel.setVisible(user.isMarketMaker());
        userStatusLabel.setText(user.getStatus());
        userCard.setAccessibleText(user.getName() + ", " + user.getStatus()
                + (user.isMarketMaker() ? ", Market Maker" : "")
                + ", Balance " + DisplayText.amount(user.getBalance()));

        userStatusLabel.getStyleClass().removeAll(STATUS_STYLE_CLASSES);
        userStatusLabel.getStyleClass().add(user.isBlocked()
                ? "user-status-blocked" : "user-status-active");
    }
}
