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

public class GuessMarketApplication extends Application {
    private static final String BASE = "/guessmarket/ui/javafx";
    public static final String MAIN_FXML = BASE + "/components/main/main.fxml";

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(MAIN_FXML));
        Parent root = loader.load();

        MainController mainController = loader.getController();
        MarketManager marketManager = new MarketManagerImpl();

        mainController.setPrimaryStage(primaryStage);
        mainController.setMarketManager(marketManager);
        primaryStage.setTitle("Guess Market");

        Scene scene = new Scene(root, 1000, 650);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
