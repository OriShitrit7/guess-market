package guessmarket.ui.javafx.components.main;

import guessmarket.engine.api.MarketManager;
import guessmarket.ui.javafx.components.events.EventsController;
import guessmarket.ui.javafx.tasks.LoadSystemFileTask;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class MainController {
    @FXML private EventsController eventsComponentController;

    @FXML private Button loadFileButton;
    @FXML private Label filePathLabel;
    @FXML private ProgressBar loadFileProgressBar;

    private Stage primaryStage;
    private MarketManager marketManager;

    @FXML
    public void initialize() {
        if (eventsComponentController != null) {
            eventsComponentController.setMainController(this);
        }
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setMarketManager(MarketManager marketManager) {
        this.marketManager = marketManager;
    }

    @FXML
    public void loadFileButtonAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a Guess Market XML File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile == null) {
            return;
        }

        String absolutePath = selectedFile.getAbsolutePath();
        LoadSystemFileTask task = new LoadSystemFileTask(marketManager, absolutePath);

        loadFileProgressBar.progressProperty().bind(task.progressProperty());
        loadFileButton.setDisable(true);
        task.valueProperty().addListener((observable, oldValue, newValue) -> onLoadFinished(task, absolutePath));

        new Thread(task).start();
    }

    private void onLoadFinished(LoadSystemFileTask task, String absolutePath) {
        loadFileProgressBar.progressProperty().unbind();
        loadFileButton.setDisable(false);

        if (task.getValue()) {
            filePathLabel.setText(absolutePath);
            return;
        }

        loadFileProgressBar.setProgress(0);
        showLoadError(task.getErrorMessage());
    }

    private void showLoadError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Load Failed");
        alert.setHeaderText("The file could not be loaded");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
