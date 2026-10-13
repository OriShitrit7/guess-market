package guessmarket.client.main;

import guessmarket.client.components.app.AppController;
import guessmarket.client.util.Constants;
import guessmarket.client.util.http.HttpClientUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class GuessMarketClient extends Application {
    private static final int MIN_WIDTH = 740;
    private static final int MIN_HEIGHT = 560;
    private AppController appController;

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.APP_FXML));
        Parent root = loader.load();
        appController = loader.getController();

        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Constants.APP_CSS), "Missing stylesheet: " + Constants.APP_CSS).toExternalForm());

        primaryStage.setTitle("Guess Market");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.show();
    }

    @Override
    public void stop() {
        appController.close();
        HttpClientUtil.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
