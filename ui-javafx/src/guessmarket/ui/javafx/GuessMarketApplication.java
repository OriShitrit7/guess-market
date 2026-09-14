package guessmarket.ui.javafx;

import guessmarket.engine.api.MarketManager;
import guessmarket.engine.api.MarketManagerImpl;
import guessmarket.ui.javafx.components.main.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

// Starts the JavaFX application and connects the engine to the main view.
public class GuessMarketApplication extends Application {
    private static final String BASE = "/guessmarket/ui/javafx";
    public static final String MAIN_FXML = BASE + "/components/main/main.fxml";
    private static final String APP_CSS = BASE + "/components/main/app.css";
    private static final int MIN_WIDTH = 740;
    private static final int MIN_HEIGHT = 560;

    // Loads the main view and configures the primary application window.
    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(MAIN_FXML));
        Parent root = loader.load(); // Load the main layout

        MainController mainController = loader.getController();
        MarketManager marketManager = new MarketManagerImpl(); // Create the shared engine

        mainController.setPrimaryStage(primaryStage); // Connect the controller to the window and engine
        mainController.setMarketManager(marketManager);
        primaryStage.setTitle("Guess Market");

        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(APP_CSS), "Missing stylesheet: " + APP_CSS).toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);

        primaryStage.show(); // Display the configured window
    }

    // Launches the JavaFX application.
    public static void main(String[] args) {
        launch(args);
    }
}
