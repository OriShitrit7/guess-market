package guessmarket.ui.javafx.components.users.dialogs;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

// Loads and shows the DialogPane based dialogs, and reports whether the user confirmed.
public abstract class Dialogs {
    private static final String APP_CSS = "/guessmarket/ui/javafx/components/main/app.css";

    // Returns the dialog's controller when the user confirms, and nothing when they cancel.
    public static <C> Optional<C> show(Class<?> owner, String fxmlName, String title, Consumer<C> setup) {
        FXMLLoader loader = new FXMLLoader(owner.getResource(fxmlName));
        DialogPane pane;

        try {
            pane = loader.load();
            pane.getStylesheets().add(Dialogs.class.getResource(APP_CSS).toExternalForm());
        } catch (IOException exception) {
            showError(title, "The dialog could not be opened: " + exception.getMessage());
            return Optional.empty();
        }

        C controller = loader.getController();
        setup.accept(controller);

        return isConfirmed(pane, title) ? Optional.of(controller) : Optional.empty();
    }

    // Shows a dialog that only reports what happened, and therefore has nothing to confirm.
    public static <C> void showReceipt(Class<?> owner, String fxmlName, String title, Consumer<C> setup) {
        show(owner, fxmlName, title, setup);
    }

    public static void showWarning(String title, String message) {
        show(Alert.AlertType.WARNING, title, message);
    }

    public static void showError(String title, String message) {
        show(Alert.AlertType.ERROR, title, message);
    }

    // Asks the user to approve an action, and reports whether they did.
    public static boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> choice = alert.showAndWait();

        return choice.isPresent() && choice.get() == ButtonType.OK;
    }

    private static void show(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static boolean isConfirmed(DialogPane pane, String title) {
        Dialog<ButtonType> dialog = new Dialog<>();

        dialog.setTitle(title);
        dialog.setDialogPane(pane);

        Optional<ButtonType> choice = dialog.showAndWait();

        return choice.isPresent() && choice.get().getButtonData() == ButtonBar.ButtonData.OK_DONE;
    }
}
