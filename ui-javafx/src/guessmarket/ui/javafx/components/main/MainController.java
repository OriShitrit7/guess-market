package guessmarket.ui.javafx.components.main;

import guessmarket.engine.api.MarketManager;
import guessmarket.ui.javafx.components.events.EventsController;
import guessmarket.ui.javafx.components.users.UsersController;
import guessmarket.ui.javafx.components.users.dialogs.Dialogs;
import guessmarket.ui.javafx.tasks.LoadSystemFileTask;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

// Coordinates the main window, shared engine and top-level Events and Users views.
public class MainController {
    @FXML private EventsController eventsComponentController;
    @FXML private UsersController usersComponentController;

    @FXML private Button loadFileButton;
    @FXML private Label filePathLabel;
    @FXML private ProgressBar loadFileProgressBar;

    private Stage primaryStage;
    private MarketManager marketManager;

    @FXML
    // Configures visibility bindings and connects the included tab controllers.
    public void initialize() {
        loadFileProgressBar.managedProperty().bind(loadFileProgressBar.visibleProperty());
        filePathLabel.managedProperty().bind(filePathLabel.visibleProperty());
        loadFileProgressBar.setVisible(false);
        filePathLabel.setVisible(false);

        if (eventsComponentController != null) {
            eventsComponentController.setMainController(this);
        }
        if (usersComponentController != null) {
            usersComponentController.setMainController(this);
        }
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setMarketManager(MarketManager marketManager) {
        this.marketManager = marketManager;
    }

    public MarketManager getMarketManager() {
        return marketManager;
    }

    // Called after any action that changes the system, so both tabs show the new state.
    public void refreshAll() {
        eventsComponentController.refreshEvents();
        usersComponentController.refreshUsers();
    }

    @FXML
    // Lets the user choose an XML file and starts loading it in the background.
    public void loadFileButtonAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a Guess Market XML File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile == null) {
            return; // User cancelled the selection
        }

        String absolutePath = selectedFile.getAbsolutePath();
        LoadSystemFileTask task = new LoadSystemFileTask(marketManager, absolutePath);

        loadFileProgressBar.progressProperty().bind(task.progressProperty()); // Show loading progress
        filePathLabel.setVisible(false);
        loadFileProgressBar.setVisible(true);
        loadFileButton.setDisable(true);
        task.valueProperty().addListener((observable, oldValue, newValue) -> onLoadFinished(task, absolutePath)); // Handle completion

        new Thread(task).start(); // Keep file loading off the UI thread
    }

    // Restores the controls and refreshes both tabs after a successful load.
    private void onLoadFinished(LoadSystemFileTask task, String absolutePath) {
        loadFileProgressBar.progressProperty().unbind();
        loadFileProgressBar.setVisible(false);
        loadFileButton.setDisable(false);

        if (task.getValue()) { // Publish the newly loaded system
            filePathLabel.setText(absolutePath);
            filePathLabel.setVisible(true);
            eventsComponentController.refreshEvents();
            usersComponentController.refreshUsers();
            return;
        }

        filePathLabel.setVisible(!filePathLabel.getText().isBlank());
        showLoadError(task.getErrorMessage()); // Keep the previous system visible after failure
    }

    private void showLoadError(String message) {
        Dialogs.showError("Load Failed", "The file could not be loaded", message);
    }
}
