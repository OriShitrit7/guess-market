package guessmarket.client.components.events;

import com.google.gson.reflect.TypeToken;
import guessmarket.client.util.Constants;
import guessmarket.client.util.http.HttpClientUtil;
import guessmarket.dto.EventSummaryDto;
import guessmarket.dto.VersionedDto;
import okhttp3.HttpUrl;

import java.lang.reflect.Type;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;

// Pulls the list of events from the server. The Events tab runs it on a timer, every REFRESH_RATE milliseconds,
// and the list is passed on only when its version has changed.
public class EventsRefresher extends TimerTask {

    private final Consumer<List<EventSummaryDto>> eventsConsumer;
    private int version = -1;

    public EventsRefresher(Consumer<List<EventSummaryDto>> eventsConsumer) {
        this.eventsConsumer = eventsConsumer;
    }

    @Override
    public void run() {
        String finalUrl = HttpUrl.parse(Constants.EVENTS_URL).newBuilder()
                .addQueryParameter(Constants.VERSION_PARAMETER, String.valueOf(version))
                .build().toString();

        HttpClientUtil.runSyncIgnoringErrors(finalUrl, body -> {
            VersionedDto<List<EventSummaryDto>> versioned = parseEventSummaries(body);

            version = versioned.getVersion();
            eventsConsumer.accept(versioned.getData());
        });
    }

    // Converts the versioned event list sent by the server. The Account tab uses it too, for Trade in New Event.
    public static VersionedDto<List<EventSummaryDto>> parseEventSummaries(String json) {
        Type type = new TypeToken<VersionedDto<List<EventSummaryDto>>>() {}.getType();

        return Constants.GSON_INSTANCE.fromJson(json, type);
    }
}
