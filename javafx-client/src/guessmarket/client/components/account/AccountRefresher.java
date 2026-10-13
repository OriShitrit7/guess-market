package guessmarket.client.components.account;

import com.google.gson.reflect.TypeToken;
import guessmarket.client.util.Constants;
import guessmarket.client.util.http.HttpClientUtil;
import guessmarket.dto.AccountDetailsDto;
import guessmarket.dto.VersionedDto;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.TimerTask;
import java.util.function.Consumer;

public class AccountRefresher extends TimerTask {
    private final Consumer<AccountDetailsDto> accountDetailsConsumer;
    private int transactionsVersion = 0;

    public AccountRefresher(Consumer<AccountDetailsDto> accountDetailsConsumer) {
        this.accountDetailsConsumer = accountDetailsConsumer;
    }

    @Override
    public void run() {
        int requestedVersion = getTransactionsVersion();
        String finalUrl = HttpUrl.parse(Constants.ACCOUNT_DETAILS_URL).newBuilder()
                .addQueryParameter(Constants.TRANSACTIONS_VERSION_PARAMETER, String.valueOf(requestedVersion))
                .build().toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try (response) {
                    if (!response.isSuccessful()) {
                        return;
                    }
                    Type type = new TypeToken<VersionedDto<AccountDetailsDto>>() {}.getType();
                    VersionedDto<AccountDetailsDto> versioned = Constants.GSON_INSTANCE.fromJson(response.body().string(), type);
                    if (advanceVersion(requestedVersion, versioned.getVersion())) {
                        accountDetailsConsumer.accept(versioned.getData());
                    }
                }
            }
        });
    }

    private synchronized int getTransactionsVersion() {
        return transactionsVersion;
    }

    private synchronized boolean advanceVersion(int requestedVersion, int newVersion) {
        if (requestedVersion != transactionsVersion) {
            return false;
        }
        transactionsVersion = newVersion;
        return true;
    }
}
