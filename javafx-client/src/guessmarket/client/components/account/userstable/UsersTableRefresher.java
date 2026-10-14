package guessmarket.client.components.account.userstable;

import com.google.gson.reflect.TypeToken;
import guessmarket.client.util.Constants;
import guessmarket.client.util.http.HttpClientUtil;
import guessmarket.dto.UserSummaryDto;
import guessmarket.dto.VersionedDto;
import okhttp3.HttpUrl;

import java.lang.reflect.Type;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;

// Pulls the list of users from the server. The users table runs it on a timer, every REFRESH_RATE milliseconds.
public class UsersTableRefresher extends TimerTask {
    private final Consumer<List<UserSummaryDto>> usersConsumer;
    private int version = -1;

    public UsersTableRefresher(Consumer<List<UserSummaryDto>> usersConsumer) {
        this.usersConsumer = usersConsumer;
    }

    @Override
    public void run() {
        String finalUrl = HttpUrl.parse(Constants.USERS_URL).newBuilder()
                .addQueryParameter(Constants.VERSION_PARAMETER, String.valueOf(version))
                .build().toString();

        HttpClientUtil.runSyncIgnoringErrors(finalUrl, body -> {
            Type type = new TypeToken<VersionedDto<List<UserSummaryDto>>>() {}.getType();
            VersionedDto<List<UserSummaryDto>> versioned = Constants.GSON_INSTANCE.fromJson(body, type);

            version = versioned.getVersion();
            usersConsumer.accept(versioned.getData());
        });
    }
}
