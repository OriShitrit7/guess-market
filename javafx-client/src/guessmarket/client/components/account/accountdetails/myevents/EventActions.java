package guessmarket.client.components.account.accountdetails.myevents;

import guessmarket.client.util.Constants;
import guessmarket.client.util.http.HttpClientUtil;
import guessmarket.dto.OrderActionDto;
import guessmarket.dto.OrderResultDto;
import guessmarket.dto.PurchaseResultDto;
import guessmarket.dto.TradeDto;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.util.function.Consumer;

// Sends the event actions that both trading panels use to the server.
public abstract class EventActions {
    private static final String ACTION_FAILED = "Action Failed";

    // Asks the server to open the event. A failure is shown to the user.
    public static void openEvent(int eventId) {
        RequestBody body = new FormBody.Builder()
                .add(Constants.EVENT_ID_PARAMETER, String.valueOf(eventId))
                .build();
        Request request = new Request.Builder().url(Constants.OPEN_EVENT_URL).post(body).build();

        HttpClientUtil.runAsyncOrShowError(request, ACTION_FAILED);
    }

    // Asks the server to close the event with the given winning option. A failure is shown to the user.
    public static void closeEvent(int eventId, int winningOptionIndex) {
        RequestBody body = new FormBody.Builder()
                .add(Constants.EVENT_ID_PARAMETER, String.valueOf(eventId))
                .add(Constants.WINNING_OPTION_INDEX_PARAMETER, String.valueOf(winningOptionIndex))
                .build();
        Request request = new Request.Builder().url(Constants.CLOSE_EVENT_URL).post(body).build();

        HttpClientUtil.runAsyncOrShowError(request, ACTION_FAILED);
    }

    // Submits an Order Book order and hands the way it was executed to resultConsumer.
    public static void submitOrder(int eventId, int optionIndex, OrderActionDto action, int quantity, double pricePerShare, Consumer<OrderResultDto> resultConsumer) {
        RequestBody body = new FormBody.Builder()
                .add(Constants.EVENT_ID_PARAMETER, String.valueOf(eventId))
                .add(Constants.OPTION_INDEX_PARAMETER, String.valueOf(optionIndex))
                .add(Constants.ACTION_PARAMETER, action.name())
                .add(Constants.QUANTITY_PARAMETER, String.valueOf(quantity))
                .add(Constants.PRICE_PARAMETER, String.valueOf(pricePerShare))
                .build();
        Request request = new Request.Builder().url(Constants.SUBMIT_ORDER_URL).post(body).build();

        HttpClientUtil.runAsyncOrShowError(request, ACTION_FAILED,
                json -> resultConsumer.accept(Constants.GSON_INSTANCE.fromJson(json, OrderResultDto.class)));
    }

    // Buys shares in an LMSR event and hands the purchase result to resultConsumer.
    public static void buyLmsrShares(int eventId, int optionIndex, int quantity, Consumer<PurchaseResultDto> resultConsumer) {
        RequestBody body = new FormBody.Builder()
                .add(Constants.EVENT_ID_PARAMETER, String.valueOf(eventId))
                .add(Constants.OPTION_INDEX_PARAMETER, String.valueOf(optionIndex))
                .add(Constants.QUANTITY_PARAMETER, String.valueOf(quantity))
                .build();
        Request request = new Request.Builder().url(Constants.BUY_LMSR_SHARES_URL).post(body).build();

        HttpClientUtil.runAsyncOrShowError(request, ACTION_FAILED,
                json -> resultConsumer.accept(Constants.GSON_INSTANCE.fromJson(json, PurchaseResultDto.class)));
    }

    // Asks the server for the price of an LMSR purchase. A failed quote is ignored, because the dialog
    // asks again on the next change.
    public static void quoteLmsrPurchase(int eventId, int optionIndex, int quantity, Consumer<TradeDto> quoteConsumer) {
        String finalUrl = HttpUrl.parse(Constants.QUOTE_LMSR_PURCHASE_URL).newBuilder()
                .addQueryParameter(Constants.EVENT_ID_PARAMETER, String.valueOf(eventId))
                .addQueryParameter(Constants.OPTION_INDEX_PARAMETER, String.valueOf(optionIndex))
                .addQueryParameter(Constants.QUANTITY_PARAMETER, String.valueOf(quantity))
                .build().toString();

        HttpClientUtil.runAsyncIgnoringErrors(finalUrl,
                json -> quoteConsumer.accept(Constants.GSON_INSTANCE.fromJson(json, TradeDto.class)));
    }
}
