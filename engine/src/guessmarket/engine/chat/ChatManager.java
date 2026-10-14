package guessmarket.engine.chat;

import guessmarket.dto.ChatMessageDto;
import guessmarket.engine.api.DtoMapper;
import guessmarket.engine.api.MarketManagerImpl;
import guessmarket.engine.exception.request.ChatRequestErrorCode;
import guessmarket.engine.exception.request.InvalidChatRequestException;

import java.util.ArrayList;
import java.util.List;

// Holds the messages of the chat shared by all logged-in users. Unlike the market engine, it is
// synchronized itself, since sending a message and reading new ones may happen at the same time.
public class ChatManager {
    public static final int MAX_MESSAGE_LENGTH = 1000;

    private final List<ChatMessage> messages = new ArrayList<>();
    private final DtoMapper dtoMapper = new DtoMapper();

    // Adds the user's message after checking it, without the spaces around it.
    public synchronized void addMessage(String username, String text) {
        String validatedText = validateMessage(text);
        messages.add(new ChatMessage(username, validatedText, System.currentTimeMillis()));
    }

    // Returns the messages from index fromIndex onward, so that a caller receives only the ones it
    // does not have yet. An index out of range returns every message.
    public synchronized List<ChatMessageDto> getNewMessages(int fromIndex) {
        if (fromIndex < 0 || fromIndex > messages.size()) {
            fromIndex = 0;
        }

        return messages.subList(fromIndex, messages.size()).stream()
                .map(dtoMapper::createChatMessage)
                .toList();
    }

    // Returns the number of messages, which serves as the version of the chat.
    public synchronized int getVersion() {
        return messages.size();
    }

    private String validateMessage(String text) {
        if (text == null || text.isBlank()) {
            throw new InvalidChatRequestException(ChatRequestErrorCode.EMPTY_MESSAGE);
        }
        if (text.indexOf('\n') >= 0 || text.indexOf('\r') >= 0) {
            throw new InvalidChatRequestException(ChatRequestErrorCode.MULTILINE_MESSAGE);
        }

        String trimmedText = text.strip();
        if (trimmedText.length() > MAX_MESSAGE_LENGTH) {
            throw new InvalidChatRequestException(ChatRequestErrorCode.MESSAGE_TOO_LONG);
        }
        if (!MarketManagerImpl.ENGLISH_TEXT.matcher(trimmedText).matches()) {
            throw new InvalidChatRequestException(ChatRequestErrorCode.MESSAGE_NOT_ENGLISH);
        }

        return trimmedText;
    }
}
