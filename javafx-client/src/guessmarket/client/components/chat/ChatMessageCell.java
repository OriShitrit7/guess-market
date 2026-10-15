package guessmarket.client.components.chat;

import guessmarket.dto.ChatMessageDto;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

// Shows one chat message: a colored circle with the sender's initial, the sender's name and the time,
// and the text below them. Messages sent by the logged-in user are highlighted.
public class ChatMessageCell extends ListCell<ChatMessageDto> {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final String[] AVATAR_COLORS = {"#f8cfdc", "#c9ebd3", "#dcd3f6", "#fbd5c5", "#cde2f6",
            "#f3e3b3", "#c6ece9", "#f6d0f0", "#dde8c0", "#d4d9e3"};
    private static final double AVATAR_RADIUS = 17;
    private static final String OWN_MESSAGE_STYLE_CLASS = "own-message";

    private final String userName;
    private final Map<String, Integer> colorIndexes;
    private final Circle avatarCircle = new Circle(AVATAR_RADIUS);
    private final Label initialLabel = new Label();
    private final Label nameLabel = new Label();
    private final Label timeLabel = new Label();
    private final Label textLabel = new Label();
    private final HBox content = createContent();

    // colorIndexes holds each sender's color number, keyed by the sender's name in lower case.
    public ChatMessageCell(String userName, Map<String, Integer> colorIndexes) {
        this.userName = userName;
        this.colorIndexes = colorIndexes;
        setPrefWidth(0); // The cell takes the list's width, so long messages wrap instead of scrolling sideways
    }

    private HBox createContent() {
        initialLabel.getStyleClass().add("chat-avatar-initial");
        nameLabel.getStyleClass().add("chat-message-name");
        timeLabel.getStyleClass().add("chat-message-time");
        textLabel.getStyleClass().add("chat-message-text");
        textLabel.setWrapText(true);

        HBox header = new HBox(8, nameLabel, timeLabel);
        header.setAlignment(Pos.BASELINE_LEFT);
        VBox body = new VBox(3, header, textLabel);
        HBox.setHgrow(body, Priority.ALWAYS);

        HBox row = new HBox(12, new StackPane(avatarCircle, initialLabel), body);
        row.setFillHeight(false); // Keeps the circle at the top of a long message
        return row;
    }

    @Override
    protected void updateItem(ChatMessageDto message, boolean empty) {
        super.updateItem(message, empty);
        getStyleClass().remove(OWN_MESSAGE_STYLE_CLASS);
        setText(null);

        if (empty || message == null) {
            setGraphic(null);
            return;
        }
        String sender = message.getUsername();
        avatarCircle.setFill(Color.web(avatarColorOf(sender)));
        initialLabel.setText(sender.substring(0, 1).toUpperCase(Locale.ROOT));
        nameLabel.setText(sender);
        timeLabel.setText(TIME_FORMAT.format(Instant.ofEpochMilli(message.getTimestamp()).atZone(ZoneId.systemDefault())));
        textLabel.setText(message.getText());
        if (sender.equalsIgnoreCase(userName)) {
            getStyleClass().add(OWN_MESSAGE_STYLE_CLASS);
        }
        setGraphic(content);
    }

    // After the last color, the colors start over from the first.
    private String avatarColorOf(String sender) {
        return AVATAR_COLORS[colorIndexes.getOrDefault(sender.toLowerCase(Locale.ROOT), 0) % AVATAR_COLORS.length];
    }
}
