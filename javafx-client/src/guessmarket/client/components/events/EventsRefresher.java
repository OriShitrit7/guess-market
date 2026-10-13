package guessmarket.client.components.events;

import com.google.gson.reflect.TypeToken;
import guessmarket.client.util.Constants;
import guessmarket.client.util.http.HttpClientUtil;
import guessmarket.dto.EventSummaryDto;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;

public class EventsRefresher extends TimerTask {

    private final Consumer<List<EventSummaryDto>> eventsConsumer;

    public EventsRefresher(Consumer<List<EventSummaryDto>> eventsConsumer) {
        this.eventsConsumer = eventsConsumer;
    }

    @Override
    public void run() {
        HttpClientUtil.runAsync(Constants.EVENTS_URL, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try (response) {
                    if (!response.isSuccessful()) {
                        return;
                    }
                    eventsConsumer.accept(parseEventSummaries(response.body().string()));
                }
            }
        });
    }

    public static List<EventSummaryDto> parseEventSummaries(String json) {
        Type listType = new TypeToken<List<EventSummaryDto>>() {}.getType();

        return Constants.GSON_INSTANCE.fromJson(json, listType);
    }
}
