package guessmarket.client.components.account.loadfile;

import guessmarket.client.components.dialogs.Dialogs;
import guessmarket.client.util.Constants;
import guessmarket.client.util.http.HttpClientUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.FileChooser;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

// Lets the logged-in user upload an XML file to the server, and reports whether its events were loaded.
public class LoadFileController {
    @FXML private Button loadFileButton;
    @FXML private Label filePathLabel;
    @FXML private ProgressBar loadFileProgressBar;

    @FXML
    private void initialize() {
        loadFileProgressBar.managedProperty().bind(loadFileProgressBar.visibleProperty());
        filePathLabel.managedProperty().bind(filePathLabel.visibleProperty());
        loadFileProgressBar.setVisible(false);
        filePathLabel.setVisible(false);
    }

    @FXML
    // Lets the user choose an XML file and starts loading it in the background.
    private void loadFileButtonAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a Guess Market XML File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        File selectedFile = fileChooser.showOpenDialog(loadFileButton.getScene().getWindow());
        if (selectedFile == null) {
            return; // User cancelled the selection
        }

        Request request = createUploadRequest(selectedFile);
        startUpload(request, selectedFile);
    }

    private Request createUploadRequest(File selectedFile) {
        RequestBody fileBody = RequestBody.create(selectedFile, MediaType.parse("text/xml"));
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(Constants.FILE_PART_NAME, selectedFile.getName(), fileBody)
                .build();

        return new Request.Builder().url(Constants.UPLOAD_FILE_URL).post(body).build();
    }

    private void startUpload(Request request, File file) {
        loadFileButton.setDisable(true);
        filePathLabel.setVisible(false);
        loadFileProgressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        loadFileProgressBar.setVisible(true);

        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> onUploadFinished(false, "Something went wrong: " + e.getMessage(), file));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try (response) {
                    boolean success = response.isSuccessful();
                    String message = response.body().string().trim();
                    Platform.runLater(() -> onUploadFinished(success, message, file));
                }
            }
        });
    }

    private void onUploadFinished(boolean success, String message, File file) {
        loadFileProgressBar.setVisible(false);
        loadFileButton.setDisable(false);

        if (success) {
            filePathLabel.setText(file.getAbsolutePath());
            filePathLabel.setVisible(true);
            Dialogs.showInfo("File Loaded", message);
            return;
        }

        filePathLabel.setVisible(!filePathLabel.getText().isBlank());
        showLoadError(file.getName(), message);
    }

    private void showLoadError(String fileName, String message) {
        Dialogs.showError("Upload Failed", "The file '" + fileName + "' could not be loaded", message);
    }
}
