package guessmarket.engine.exception.request;

import guessmarket.engine.chat.ChatManager;

public enum ChatRequestErrorCode {
    EMPTY_MESSAGE("Please enter a message."),
    MULTILINE_MESSAGE("A message must be on one line."),
    MESSAGE_NOT_ENGLISH("A message must be in English: letters, digits, spaces and punctuation only."),
    MESSAGE_TOO_LONG("A message cannot exceed " + ChatManager.MAX_MESSAGE_LENGTH + " characters.");

    private final String message;

    ChatRequestErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
