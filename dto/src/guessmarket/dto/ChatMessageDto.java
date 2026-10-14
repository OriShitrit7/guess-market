package guessmarket.dto;

public final class ChatMessageDto {
    private final String username;
    private final String text;
    private final long timestamp;

    public ChatMessageDto(String username, String text, long timestamp) {
        this.username = username;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public String getText() {
        return text;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
