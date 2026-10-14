package guessmarket.client.components.account;

import com.google.gson.reflect.TypeToken;
import guessmarket.client.util.Constants;
import guessmarket.client.util.http.HttpClientUtil;
import guessmarket.dto.AccountDetailsDto;
import guessmarket.dto.VersionedDto;
import okhttp3.HttpUrl;

import java.lang.reflect.Type;
import java.util.TimerTask;
import java.util.function.Consumer;

// Pulls the logged-in user's account details from the server. Each request sends the number of
// transactions already received, so only the new ones come back, and the version of the events list.
// When neither has changed, the server answers 204 and nothing is passed on.
public class AccountRefresher extends TimerTask {
    private final Consumer<AccountDetailsDto> accountDetailsConsumer;
    private int transactionsVersion = 0;
    private int eventsVersion = -1;

    public AccountRefresher(Consumer<AccountDetailsDto> accountDetailsConsumer) {
        this.accountDetailsConsumer = accountDetailsConsumer;
    }

    @Override
    public void run() {
        String finalUrl = HttpUrl.parse(Constants.ACCOUNT_DETAILS_URL).newBuilder()
                .addQueryParameter(Constants.TRANSACTIONS_VERSION_PARAMETER, String.valueOf(transactionsVersion))
                .addQueryParameter(Constants.EVENTS_VERSION_PARAMETER, String.valueOf(eventsVersion))
                .build().toString();

        HttpClientUtil.runSyncIgnoringErrors(finalUrl, body -> {
            Type type = new TypeToken<VersionedDto<VersionedDto<AccountDetailsDto>>>() {}.getType();
            VersionedDto<VersionedDto<AccountDetailsDto>> versioned = Constants.GSON_INSTANCE.fromJson(body, type);
            VersionedDto<AccountDetailsDto> details = versioned.getData();

            transactionsVersion = details.getVersion();
            eventsVersion = versioned.getVersion();
            accountDetailsConsumer.accept(details.getData());
        });
    }
}
