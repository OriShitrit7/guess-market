package guessmarket.ui.javafx.components.users;

import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.function.Consumer;

// Controls a selectable user card and presents the user's current status.
public class UserCardController {
    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");
    private static final List<String> STATUS_STYLE_CLASSES =
            List.of("user-status-active", "user-status-blocked");

    @FXML
    private HBox userCard;
    @FXML
    private Label userNameLabel;
    @FXML
    private Label userStatusLabel;

    private UserTableRow user;
    private Consumer<UserTableRow> selectionHandler;

    @FXML
    // Enables card selection with either the mouse or keyboard.
    private void initialize() {
        userCard.setOnMouseClicked(mouseEvent -> select());
        userCard.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER || keyEvent.getCode() == KeyCode.SPACE) {
                select();
                keyEvent.consume();
            }
        });
    }

    // Populates the card and registers the action used when it is selected.
    public void setUser(UserTableRow user, Consumer<UserTableRow> selectionHandler) {
        this.user = user;
        this.selectionHandler = selectionHandler;

        userNameLabel.setText(user.getName());
        userStatusLabel.setText(user.getStatus());
        userCard.setAccessibleText(user.getName() + ", " + user.getStatus());

        userStatusLabel.getStyleClass().removeAll(STATUS_STYLE_CLASSES);
        userStatusLabel.getStyleClass().add("Blocked".equals(user.getStatus())
                ? "user-status-blocked" : "user-status-active");
    }

    public String getUserName() {
        return user.getName();
    }

    public void setSelected(boolean selected) {
        userCard.pseudoClassStateChanged(SELECTED, selected);
    }

    private void select() {
        if (user != null && selectionHandler != null) {
            userCard.requestFocus();
            selectionHandler.accept(user);
        }
    }
}
