package guessmarket.client.util.http;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

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

    public static void removeCookiesOf(String domain) {
        simpleCookieManager.removeCookiesOf(domain);
    }

    public static void shutdown() {
        HTTP_CLIENT.dispatcher().executorService().shutdown();
        HTTP_CLIENT.connectionPool().evictAll();
    }
}
