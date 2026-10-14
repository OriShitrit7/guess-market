package guessmarket.client.components.account.accountdetails.wallet;

import guessmarket.client.components.DisplayText;
import guessmarket.client.components.account.dialogs.loadfunds.LoadFundsDialogController;
import guessmarket.client.util.Constants;
import guessmarket.client.util.http.HttpClientUtil;
import guessmarket.dto.AccountTransactionDto;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.controlsfx.control.SegmentedButton;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

// Shows the logged-in user's balance and account history, as a table or as a balance chart, and loads funds.
public class WalletController {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM HH:mm");
    private static final String LOAD_FUNDS_FAILED = "Load Funds Failed";
    private static final double BALANCE_AXIS_TICK_COUNT = 6.0;

    @FXML private Label accountTitleLabel;
    @FXML private Label accountBalanceLabel;
    @FXML private Button loadFundsButton;
    @FXML private TableView<AccountTransactionDto> accountActivityTable;
    @FXML private TableColumn<AccountTransactionDto, String> activityDateColumn;
    @FXML private TableColumn<AccountTransactionDto, String> activityDescriptionColumn;
    @FXML private TableColumn<AccountTransactionDto, String> activityAmountColumn;
    @FXML private SegmentedButton activityViewSelector;
    @FXML private ToggleButton activityToggle;
    @FXML private StackPane balanceHistoryView;
    @FXML private LineChart<Number, Number> balanceHistoryChart;
    @FXML private NumberAxis balanceHistoryXAxis;
    @FXML private VBox emptyBalanceHistoryState;

    private final ObservableList<AccountTransactionDto> transactions = FXCollections.observableArrayList();
    private final XYChart.Series<Number, Number> balanceSeries = new XYChart.Series<>();
    private BigDecimal balance;

    @FXML
    private void initialize() {
        activityDateColumn.setCellValueFactory(cell -> new SimpleStringProperty(formatDate(cell.getValue().getTimestamp())));
        activityDescriptionColumn.setCellValueFactory(cell -> new SimpleStringProperty(DisplayText.of(cell.getValue().getType())));
        activityAmountColumn.setCellValueFactory(cell -> new SimpleStringProperty(DisplayText.signedAmount(cell.getValue().getAmount())));
        accountActivityTable.setItems(transactions);
        balanceHistoryChart.getData().add(balanceSeries);
        configureBalanceXAxis();
        initActivityViewSelector();
    }

    private void initActivityViewSelector() {
        accountActivityTable.managedProperty().bind(accountActivityTable.visibleProperty());
        balanceHistoryView.managedProperty().bind(balanceHistoryView.visibleProperty());
        activityViewSelector.getToggleGroup().selectedToggleProperty().addListener((_, oldValue, newValue) -> {
            if (newValue == null) {
                activityViewSelector.getToggleGroup().selectToggle(oldValue);
            }
            showSelectedActivityView();
        });
        showSelectedActivityView();
    }

    private void showSelectedActivityView() {
        boolean showActivity = activityToggle.isSelected();

        accountActivityTable.setVisible(showActivity);
        balanceHistoryView.setVisible(!showActivity);
    }

    public void setUserName(String userName) {
        accountTitleLabel.setText(userName + "'s Account Details");
    }

    public void setBalance(BigDecimal balance) {
        this.balance = Objects.requireNonNull(balance);
        accountBalanceLabel.setText("Balance: " + DisplayText.amount(balance.doubleValue()));
    }

    @FXML
    private void loadFundsButtonAction() {
        try {
            FXMLLoader loader = new FXMLLoader(LoadFundsDialogController.class.getResource("load-funds-dialog.fxml"));
            DialogPane pane = loader.load();
            LoadFundsDialogController controller = loader.getController();
            if (balance != null) {
                controller.setCurrentBalance(balance);
            }
            controller.setSubmissionEnabled(balance != null);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Load Funds");
            dialog.initOwner(loadFundsButton.getScene().getWindow());
            dialog.setDialogPane(pane);
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                loadFunds(controller.getAmount());
            }
        } catch (IOException exception) {
            Alert error = new Alert(Alert.AlertType.ERROR,
                    "The Load Funds dialog could not be opened: " + exception.getMessage());
            error.setHeaderText(null);
            error.showAndWait();
        }
    }

    private void loadFunds(BigDecimal amount) {
        RequestBody body = new FormBody.Builder()
                .add(Constants.AMOUNT_PARAMETER, amount.toPlainString())
                .build();
        Request request = new Request.Builder().url(Constants.LOAD_FUNDS_URL).post(body).build();

        HttpClientUtil.runAsyncOrShowError(request, LOAD_FUNDS_FAILED);
    }

    // Appends the transactions that arrived in the latest pull to the table and to the chart.
    public void addTransactions(List<AccountTransactionDto> newTransactions) {
        if (newTransactions.isEmpty()) {
            return;
        }
        transactions.addAll(0, newTransactions.reversed()); // The newest transaction is shown first
        newTransactions.forEach(this::addBalancePoint);
        configureBalanceXAxis();
        emptyBalanceHistoryState.setVisible(false);
    }

    // The chart starts from the balance before the first transaction, so that the first change shows too.
    private void addBalancePoint(AccountTransactionDto transaction) {
        List<XYChart.Data<Number, Number>> points = balanceSeries.getData();

        if (points.isEmpty()) {
            points.add(new XYChart.Data<>(0, transaction.getBalanceAfter() - transaction.getAmount()));
        }
        points.add(new XYChart.Data<>(points.size(), transaction.getBalanceAfter()));
    }

    private void configureBalanceXAxis() {
        int lastPoint = Math.max(1, balanceSeries.getData().size() - 1);

        balanceHistoryXAxis.setLowerBound(0);
        balanceHistoryXAxis.setUpperBound(lastPoint);
        balanceHistoryXAxis.setTickUnit(Math.max(1, Math.ceil(lastPoint / BALANCE_AXIS_TICK_COUNT)));
    }

    private String formatDate(long timestamp) {
        return DATE_FORMAT.format(Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()));
    }
}
