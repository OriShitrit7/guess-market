package guessmarket.client.util;

import com.google.gson.Gson;

public abstract class Constants {

    // global constants
    public static final int REFRESH_RATE = 500;

    // fxml locations
    public static final String APP_FXML = "/guessmarket/client/components/app/app.fxml";
    public static final String LOGIN_FXML = "/guessmarket/client/components/login/login.fxml";
    public static final String MAIN_FXML = "/guessmarket/client/components/main/main.fxml";

    // css locations
    public static final String APP_CSS = "/guessmarket/client/components/main/app.css";

    // Server resources locations
    public static final String BASE_DOMAIN = "localhost";
    private static final String BASE_URL = "http://" + BASE_DOMAIN + ":8080";
    private static final String CONTEXT_PATH = "/guessmarket";
    private static final String FULL_SERVER_PATH = BASE_URL + CONTEXT_PATH;

    public static final String LOGIN_URL = FULL_SERVER_PATH + "/login";
    public static final String UPLOAD_FILE_URL = FULL_SERVER_PATH + "/upload-file";
    public static final String EVENTS_URL = FULL_SERVER_PATH + "/events";
    public static final String EVENT_STATE_URL = FULL_SERVER_PATH + "/event-state";
    public static final String EVENT_POSITION_URL = FULL_SERVER_PATH + "/event-position";
    public static final String USERS_URL = FULL_SERVER_PATH + "/users";
    public static final String ACCOUNT_DETAILS_URL = FULL_SERVER_PATH + "/account-details";
    public static final String LOAD_FUNDS_URL = FULL_SERVER_PATH + "/load-funds";
    public static final String CREATE_EVENT_URL = FULL_SERVER_PATH + "/create-event";
    public static final String OPEN_EVENT_URL = FULL_SERVER_PATH + "/open-event";
    public static final String CLOSE_EVENT_URL = FULL_SERVER_PATH + "/close-event";
    public static final String SUBMIT_ORDER_URL = FULL_SERVER_PATH + "/submit-order";
    public static final String BUY_LMSR_SHARES_URL = FULL_SERVER_PATH + "/buy-lmsr-shares";
    public static final String QUOTE_LMSR_PURCHASE_URL = FULL_SERVER_PATH + "/quote-lmsr-purchase";

    // Request parameter names
    public static final String USERNAME_PARAMETER = "username";
    public static final String FILE_PART_NAME = "file";
    public static final String EVENT_ID_PARAMETER = "eventId";
    public static final String TRANSACTIONS_VERSION_PARAMETER = "transactionsVersion";
    public static final String AMOUNT_PARAMETER = "amount";
    public static final String WINNING_OPTION_INDEX_PARAMETER = "winningOptionIndex";
    public static final String OPTION_INDEX_PARAMETER = "optionIndex";
    public static final String ACTION_PARAMETER = "action";
    public static final String QUANTITY_PARAMETER = "quantity";
    public static final String PRICE_PARAMETER = "price";

    // GSON instance
    public static final Gson GSON_INSTANCE = new Gson();
}
