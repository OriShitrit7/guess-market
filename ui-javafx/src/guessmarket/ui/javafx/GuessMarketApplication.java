package guessmarket.ui.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class GuessMarketApplication extends Application {
    private static final String BASE = "/guessmarket/ui/javafx";
    public static final String MAIN_FXML = BASE + "/components/main/main.fxml";

    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setTitle("Guess Market");
        Parent load = FXMLLoader.load(getClass().getResource(MAIN_FXML));
        Scene scene = new Scene(load, 1000, 650);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
