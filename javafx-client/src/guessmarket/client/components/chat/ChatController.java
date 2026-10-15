package guessmarket.client.components.chat;

import guessmarket.client.components.dialogs.Dialogs;
import guessmarket.client.util.Constants;
import guessmarket.client.util.http.HttpClientUtil;
import guessmarket.dto.ChatMessageDto;
import guessmarket.dto.VersionedDto;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;

// Displays shared chat messages and sends new messages for the logged-in user.
public class ChatController implements Closeable {
    private static final String SEND_FAILED = "Send Message Failed";

    @FXML private ListView<ChatMessageDto> chatListView;
    @FXML private TextField messageTextField;
    @FXML private Button sendButton;

    private final ObservableList<ChatMessageDto> messages = FXCollections.observableArrayList();
    // Each sender's color number, in the order the senders first wrote. Every client receives the same history
    // in the same order, so a sender has the same color in every client.
    private final Map<String, Integer> colorIndexes = new HashMap<>();
    private Timer timer;
    private ChatRefresher chatRefresher;
    private int displayedVersion;
    private boolean sending;

    @FXML
    private void initialize() {
        chatListView.setItems(messages);
        chatListView.setPlaceholder(new Label("No messages yet."));
        messageTextField.textProperty().addListener((_, _, _) -> updateSendButton());
        updateSendButton();
    }

    // Sets the user whose own messages are highlighted. Called before polling starts.
    public void setUserName(String userName) {
        chatListView.setCellFactory(_ -> new ChatMessageCell(userName, colorIndexes));
    }

    // Keeps the refresher's version when switching away from and back to the Chat tab.
    public void setPollingEnabled(boolean enabled) {
        if (timer == null) {
            if (!enabled) {
                return;
            }
            chatRefresher = new ChatRefresher(result -> Platform.runLater(() -> showMessages(result)));
            timer = new Timer("chat-refresher", true);
            chatRefresher.setEnabled(true);
            timer.schedule(chatRefresher, 0, Constants.REFRESH_RATE);
        } else {
            chatRefresher.setEnabled(enabled);
        }
    }

    private void showMessages(VersionedDto<List<ChatMessageDto>> result) {
        if (result.getVersion() < displayedVersion) { // The server restarted, so its messages replace the old ones
            messages.clear();
            colorIndexes.clear();
        }
        for (ChatMessageDto message : result.getData()) {
            colorIndexes.putIfAbsent(message.getUsername().toLowerCase(Locale.ROOT), colorIndexes.size());
        }
        messages.addAll(result.getData());
        // Scrolls only after the list has laid out the new cells. Scrolling at once, before their heights are
        // known, sometimes left the earlier messages undrawn until the next update.
        Platform.runLater(() -> chatListView.scrollTo(messages.size() - 1));
        displayedVersion = result.getVersion();
    }

    @FXML
    private void sendMessage() {
        String text = messageTextField.getText();
        if (sending || text.isBlank()) {
            return;
        }

        RequestBody body = new FormBody.Builder()
                .add(Constants.MESSAGE_PARAMETER, text)
                .build();
        Request request = new Request.Builder().url(Constants.SEND_CHAT_URL).post(body).build();

        sending = true;
        updateSendButton();
        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> finishSending(text, Constants.SERVER_UNREACHABLE));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try (response) {
                    if (response.isSuccessful()) {
                        Platform.runLater(() -> finishSending(text, null));
                    } else {
                        String error = response.body() == null ? "The server rejected the message."
                                : response.body().string().trim();
                        Platform.runLater(() -> finishSending(text, error));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> finishSending(text, Constants.SERVER_UNREACHABLE));
                }
            }
        });
    }

    private void finishSending(String sentText, String error) {
        if (error == null && messageTextField.getText().equals(sentText)) {
            messageTextField.clear();
        }
        sending = false;
        updateSendButton();
        if (error != null) {
            Dialogs.showError(SEND_FAILED, error);
        }
    }

    private void updateSendButton() {
        sendButton.setDisable(sending || messageTextField.getText().isBlank());
    }

    @Override
    public void close() {
        if (timer != null) {
            chatRefresher.setEnabled(false);
            chatRefresher.cancel();
            timer.cancel();
        }
    }
}
