package guessmarket.client.components.account.userstable;

import com.google.gson.reflect.TypeToken;
import guessmarket.client.util.Constants;
import guessmarket.client.util.http.HttpClientUtil;
import guessmarket.dto.UserSummaryDto;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;

public class UsersTableRefresher extends TimerTask {
    private final Consumer<List<UserSummaryDto>> usersConsumer;

    public UsersTableRefresher(Consumer<List<UserSummaryDto>> usersConsumer) {
        this.usersConsumer = usersConsumer;
    }

    @Override
    public void run() {
        HttpClientUtil.runAsync(Constants.USERS_URL, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try (response) {
                    if (!response.isSuccessful()) {
                        return;
                    }
                    String body = response.body().string();
                    Type listType = new TypeToken<List<UserSummaryDto>>() {}.getType();
                    List<UserSummaryDto> usersList = Constants.GSON_INSTANCE.fromJson(body, listType);
                    usersConsumer.accept(usersList);
                }
            }
        });
    }
}
