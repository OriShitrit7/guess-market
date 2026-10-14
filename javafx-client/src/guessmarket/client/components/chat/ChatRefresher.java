package guessmarket.client.components.chat;

import com.google.gson.reflect.TypeToken;
import guessmarket.client.util.Constants;
import guessmarket.client.util.http.HttpClientUtil;
import guessmarket.dto.ChatMessageDto;
import guessmarket.dto.VersionedDto;
import okhttp3.HttpUrl;

import java.lang.reflect.Type;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;

// Requests only messages added after the last version while the Chat tab is selected.
public class ChatRefresher extends TimerTask {
    private final Consumer<VersionedDto<List<ChatMessageDto>>> messagesConsumer;
    private volatile boolean enabled;
    private int version;

    public ChatRefresher(Consumer<VersionedDto<List<ChatMessageDto>>> messagesConsumer) {
        this.messagesConsumer = messagesConsumer;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void run() {
        if (!enabled) {
            return;
        }

        String finalUrl = HttpUrl.parse(Constants.CHAT_URL).newBuilder()
                .addQueryParameter(Constants.VERSION_PARAMETER, String.valueOf(version))
                .build().toString();

        HttpClientUtil.runSyncIgnoringErrors(finalUrl, body -> {
            Type type = new TypeToken<VersionedDto<List<ChatMessageDto>>>() {}.getType();
            VersionedDto<List<ChatMessageDto>> result = Constants.GSON_INSTANCE.fromJson(body, type);

            version = result.getVersion();
            messagesConsumer.accept(result);
        });
    }
}
