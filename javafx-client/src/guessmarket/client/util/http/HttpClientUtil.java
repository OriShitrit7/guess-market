package guessmarket.client.util.http;

import guessmarket.client.components.dialogs.Dialogs;
import javafx.application.Platform;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.Consumer;

public class HttpClientUtil {
    private static final SimpleCookieManager simpleCookieManager = new SimpleCookieManager();
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .cookieJar(simpleCookieManager)
            .followRedirects(false)
            .build();

    public static void runAsync(String finalUrl, Callback callback) {
        runAsync(new Request.Builder().url(finalUrl).build(), callback);
    }

    public static void runAsync(Request request, Callback callback) {
        Call call = HTTP_CLIENT.newCall(request);

        call.enqueue(callback);
    }

    public static void runAsyncIgnoringErrors(String finalUrl, Consumer<String> bodyConsumer) {
        runAsync(finalUrl, new Callback() {
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

                    Platform.runLater(() -> bodyConsumer.accept(body));
                }
            }
        });
    }

    public static void runAsyncOrShowError(Request request, String errorTitle) {
        runAsyncOrShowError(request, errorTitle, _ -> { });
    }

    public static void runAsyncOrShowError(Request request, String errorTitle, Consumer<String> bodyConsumer) {
        runAsync(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> Dialogs.showError(errorTitle, "Something went wrong: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try (response) {
                    String body = response.body().string();

                    if (response.isSuccessful()) {
                        Platform.runLater(() -> bodyConsumer.accept(body));
                    } else {
                        Platform.runLater(() -> Dialogs.showError(errorTitle, body.trim()));
                    }
                }
            }
        });
    }

    public static void removeCookiesOf(String domain) {
        simpleCookieManager.removeCookiesOf(domain);
    }

    public static void shutdown() {
        HTTP_CLIENT.dispatcher().executorService().shutdown();
        HTTP_CLIENT.connectionPool().evictAll();
    }
}
