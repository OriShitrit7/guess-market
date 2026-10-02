package guessmarket.client.util;

public abstract class Constants {

    // global constants

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

    // Request parameter names
    public static final String USERNAME_PARAMETER = "username";
    public static final String FILE_PART_NAME = "file";

    // GSON instance

}
