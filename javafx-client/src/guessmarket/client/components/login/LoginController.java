package guessmarket.client.components.login;

import guessmarket.client.components.app.AppController;
import guessmarket.client.util.Constants;
import guessmarket.client.util.http.HttpClientUtil;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

// Handles the login screen: sends the user name to the server and moves to the main screen once it is accepted.
public class LoginController {
    @FXML
    private TextField userNameTextField;
    @FXML
    private Label errorMessageLabel;
    @FXML
    private Button loginButton;

    private AppController appController;
    private final StringProperty errorMessageProperty = new SimpleStringProperty();
    private final BooleanProperty loginInProgress = new SimpleBooleanProperty(false);

    @FXML
    private void initialize() {
        errorMessageLabel.textProperty().bind(errorMessageProperty);
        loginButton.disableProperty().bind(loginInProgress);
    }

    @FXML
    private void loginButtonClicked(ActionEvent event) {
        String username = userNameTextField.getText().trim();
        if (username.isEmpty()) {
            errorMessageProperty.set("Please enter a user name.");
            return;
        }

        RequestBody body = new FormBody.Builder()
                .add(Constants.USERNAME_PARAMETER, username)
                .build();
        Request request = new Request.Builder()
                .url(Constants.LOGIN_URL)
                .post(body)
                .build();

        loginInProgress.set(true);
        HttpClientUtil.runAsync(request, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> {
                    errorMessageProperty.set("Something went wrong: " + e.getMessage());
                    loginInProgress.set(false);
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try (response) {
                    if (response.isSuccessful()) {
                        Platform.runLater(() -> appController.switchToMain(username));
                        return;
                    }

                    String errorMessage = response.body().string().trim();
                    Platform.runLater(() -> {
                        errorMessageProperty.set(errorMessage);
                        loginInProgress.set(false);
                    });
                }
            }
        });
    }

    @FXML
    private void userNameKeyTyped(KeyEvent event) {
        errorMessageProperty.set("");
    }

    public void setAppController(AppController appController) {
        this.appController = appController;
    }
}
