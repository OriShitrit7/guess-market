package guessmarket.client.util.http;

import guessmarket.client.components.dialogs.Dialogs;
import guessmarket.client.util.Constants;
import javafx.application.Platform;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.function.Consumer;

// Sends every request of the client through a single OkHttp client, which keeps the session cookie.
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

    // Sends a GET request and hands the response body to the consumer on the JavaFX thread. Failures are
    // ignored, which suits requests that are sent again on the next refresh. A 204 answer means nothing
    // changed, so the consumer is not called.
    public static void runAsyncIgnoringErrors(String finalUrl, Consumer<String> bodyConsumer) {
        runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try (response) {
                    if (!response.isSuccessful() || response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                        return;
                    }
                    String body = response.body().string();

                    Platform.runLater(() -> bodyConsumer.accept(body));
                }
            }
        });
    }

    // Sends a GET request and waits for the response, so it must never be called from the JavaFX thread.
    // The refreshers use it, since their timer thread has nothing else to do meanwhile. The response body is
    // handed to the consumer on the calling thread. Failures and 204 answers are ignored, as in runAsyncIgnoringErrors.
    public static void runSyncIgnoringErrors(String finalUrl, Consumer<String> bodyConsumer) {
        Request request = new Request.Builder().url(finalUrl).build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (response.isSuccessful() && response.code() != HttpURLConnection.HTTP_NO_CONTENT) {
                bodyConsumer.accept(response.body().string());
            }
        } catch (IOException ignored) {
        }
    }

    // Sends a request whose successful response carries nothing to use, and shows any failure in an error dialog.
    public static void runAsyncOrShowError(Request request, String errorTitle) {
        runAsyncOrShowError(request, errorTitle, _ -> { });
    }

    // Sends a request and hands the response body to the consumer on the JavaFX thread. A failure, or the
    // server's error message, is shown in an error dialog with the given title.
    public static void runAsyncOrShowError(Request request, String errorTitle, Consumer<String> bodyConsumer) {
        runAsync(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> Dialogs.showError(errorTitle, Constants.SERVER_UNREACHABLE));
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

    // Releases the client's threads and connections so that the application can exit.
    public static void shutdown() {
        HTTP_CLIENT.dispatcher().executorService().shutdown();
        HTTP_CLIENT.connectionPool().evictAll();
    }
}
