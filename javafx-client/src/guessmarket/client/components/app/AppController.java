package guessmarket.client.components.app;

import guessmarket.client.components.login.LoginController;
import guessmarket.client.components.main.MainController;
import guessmarket.client.util.Constants;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;

// Switches the window between the login screen and the main screen. Both screens are loaded once,
// at startup, so that switching later cannot fail on loading a file.
public class AppController {
    @FXML private StackPane contentArea;

    private Parent loginComponent;
    private LoginController loginController;
    private Parent mainComponent;
    private MainController mainController;

    @FXML
    public void initialize() throws IOException {
        loadLogin();
        loadMain();
        setContent(loginComponent);
    }

    private void setContent(Parent component) {
        contentArea.getChildren().setAll(component);
    }

    // Shows the main screen for the logged-in user and starts pulling its data from the server.
    public void switchToMain(String userName) {
        mainController.setActive(userName);
        setContent(mainComponent);
    }

    private void loadLogin() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.LOGIN_FXML));
        loginComponent = loader.load();
        loginController = loader.getController();
        loginController.setAppController(this);
    }

    private void loadMain() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.MAIN_FXML));
        mainComponent = loader.load();
        mainController = loader.getController();
    }

    public void close() {
        mainController.close();
    }
}
