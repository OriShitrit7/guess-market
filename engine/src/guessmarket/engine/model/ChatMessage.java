package guessmarket.engine.model;

// One message of the shared chat: who sent it, its text and when it was received.
public class ChatMessage {
    private final String username;
    private final String text;
    private final long timestamp;

    public ChatMessage(String username, String text, long timestamp) {
        this.username = username;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public String getUsername() {
        return username;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
