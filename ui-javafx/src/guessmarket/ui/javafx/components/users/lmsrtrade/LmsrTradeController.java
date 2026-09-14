package guessmarket.ui.javafx.components.users.lmsrtrade;

import guessmarket.dto.EventStatusDto;
import guessmarket.dto.EventSummaryDto;
import guessmarket.dto.EventTradingStateDto;
import guessmarket.dto.OptionStateDto;
import guessmarket.dto.PurchaseResultDto;
import guessmarket.dto.TradeDto;
import guessmarket.dto.UserInvolvementDto;
import guessmarket.dto.UserSummaryDto;
import guessmarket.ui.javafx.components.DisplayText;
import guessmarket.ui.javafx.components.StatusStyle;
import guessmarket.ui.javafx.components.main.MainController;
import guessmarket.engine.exception.request.InvalidRequestException;
import guessmarket.ui.javafx.components.users.ResultCardController;
import guessmarket.ui.javafx.components.users.dialogs.Dialogs;
import guessmarket.ui.javafx.components.users.dialogs.buyshares.BuySharesDialogController;
import guessmarket.ui.javafx.components.users.dialogs.closeevent.CloseEventDialogController;
import guessmarket.ui.javafx.components.users.dialogs.receipt.ReceiptDialogController;
import guessmarket.ui.javafx.components.users.TradeTableRow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

// Manages one user's view and available actions for an LMSR event.
public class LmsrTradeController {
    private static final String NO_SELECTION = "-";
    private static final String EMPTY_TEXT = "";
    private static final String EMPTY_AMOUNT = "$0.00";
    private static final String COMMISSION_FORMAT = "%d%% %s";
    private static final String OPEN_EVENT = "Open Event";
    private static final String CLOSE_EVENT = "Close Event";
    private static final String BUY_SHARES = "Buy Shares";
    private static final int OPTION_COUNT = 2;

    private static final String BUY_SHARES_DIALOG = "/guessmarket/ui/javafx/components/users/dialogs/buyshares/buy-shares-dialog.fxml";
    private static final String RECEIPT_DIALOG = "/guessmarket/ui/javafx/components/users/dialogs/receipt/receipt-dialog.fxml";
    private static final String PURCHASE_COMPLETED = "Purchase Completed";
    private static final String CLOSE_EVENT_DIALOG = "/guessmarket/ui/javafx/components/users/dialogs/closeevent/close-event-dialog.fxml";
    private static final String ACTION_FAILED = "Action Failed";
    private static final String ACCOUNT_BLOCKED = "Account Blocked";
    private static final String BLOCKED_MESSAGE = "Your balance is now negative. Your account has been blocked and cannot perform further actions.";
    private static final String OPEN_EVENT_FORMAT =
            "Opening \"%s\" will charge you %s.%nYour balance is %s.%n%nOpen the event ?";

    private MainController mainController;
    private String userName;
    private int eventId;
    private boolean marketMaker;
    private EventSummaryDto summary;

    @FXML
    private Label eventNameLabel;
    @FXML
    private Label eventStatusLabel;
    @FXML
    private Label eventDescriptionLabel;
    @FXML
    private Label eventCommissionLabel;
    @FXML
    private Label eventAccountLabel;

    @FXML
    private Label firstOptionNameLabel;
    @FXML
    private Label firstOptionPriceLabel;
    @FXML
    private Label secondOptionNameLabel;
    @FXML
    private Label secondOptionPriceLabel;

    @FXML
    private VBox tradingCard;
    @FXML
    private VBox resultCardComponent;
    @FXML
    private ResultCardController resultCardComponentController;

    @FXML
    private Button changeEventStatusButton;
    @FXML
    private Button buySharesButton;

    @FXML
    private TableView<TradeTableRow> userTradesTable;
    @FXML
    private TableColumn<TradeTableRow, String> tradeOptionColumn;
    @FXML
    private TableColumn<TradeTableRow, Number> tradeSharesColumn;
    @FXML
    private TableColumn<TradeTableRow, Number> tradePaidColumn;
    @FXML
    private TableColumn<TradeTableRow, Number> tradeCommissionColumn;
    @FXML
    private TableColumn<TradeTableRow, Number> tradeTotalPaidColumn;
    @FXML
    private Label totalCommissionPaidLabel;

    private final ObservableList<TradeTableRow> userTrades = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        tradingCard.managedProperty().bind(tradingCard.visibleProperty());
        resultCardComponent.managedProperty().bind(resultCardComponent.visibleProperty());

        userTradesTable.setPlaceholder(new Label("Select an event to view your holdings"));

        initTradesTable();
        clear();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // Fills the panel with one user's involvement in one LMSR event.
    public void refresh(String userName, int eventId, boolean marketMaker) {
        this.userName = userName;
        this.eventId = eventId;
        this.marketMaker = marketMaker;

        EventTradingStateDto state = mainController.getMarketManager().getEventTradingState(eventId); // Load event state
        this.summary = state.getSummary();
        UserInvolvementDto involvement =
                mainController.getMarketManager().getUserInvolvement(userName, eventId);

        showEventSummary(state.getSummary()); // Refresh visible event and user data
        showOptionPrices(state.getOptions());
        showActionButtons(state.getSummary().getStatus(), marketMaker, isBlocked());
        showUserTrades(involvement);
        showResultCard(state, involvement);
    }

    // Resets the panel when no LMSR event is selected.
    public void clear() {
        eventNameLabel.setText(NO_SELECTION);
        eventStatusLabel.setText(EMPTY_TEXT);
        StatusStyle.clear(eventStatusLabel);
        eventDescriptionLabel.setText(EMPTY_TEXT);
        eventCommissionLabel.setText(EMPTY_TEXT);
        eventAccountLabel.setText(EMPTY_AMOUNT);

        clearOptionPrices();

        tradingCard.setVisible(true);
        resultCardComponent.setVisible(false);

        userTrades.clear();
        totalCommissionPaidLabel.setText(EMPTY_AMOUNT);

        changeEventStatusButton.setDisable(true);
        buySharesButton.setDisable(true);
    }

    @FXML
    // Opens an inactive event or asks the market maker to choose its winner.
    public void changeEventStatusButtonAction() {
        if (summary.getStatus() == EventStatusDto.NOT_STARTED) {
            confirmAndOpenEvent();
            return;
        }
        Dialogs.<CloseEventDialogController>show(getClass(), CLOSE_EVENT_DIALOG, CLOSE_EVENT,
                        dialog -> dialog.setEvent(summary.getEventName(), summary.getOptionNames()))
                .ifPresent(dialog -> runAction(() -> mainController.getMarketManager()
                        .closeEvent(userName, eventId, dialog.getWinningOptionIndex())));
    }

    // The market maker pays the opening cost, so the amount is shown before it is charged.
    private void confirmAndOpenEvent() {
        String message = String.format(OPEN_EVENT_FORMAT, summary.getEventName(),
                DisplayText.amount(summary.getOpeningCost()), DisplayText.amount(currentBalance()));

        if (Dialogs.confirm(OPEN_EVENT, message)) {
            runAction(() -> mainController.getMarketManager().openEvent(userName, eventId));
        }
    }

    @FXML
    // Opens the purchase dialog and executes the confirmed LMSR purchase.
    public void buySharesButtonAction() {
        String dialogTitle = summary.getEventName() + " - " + BUY_SHARES;

        Dialogs.<BuySharesDialogController>show(getClass(), BUY_SHARES_DIALOG, dialogTitle,
                        dialog -> dialog.setEvent(summary.getEventName(), currentOptions(),
                                currentBalance(), summary.getCommissionPercent(), this::quote))
                .ifPresent(this::buyShares);
    }

    private void buyShares(BuySharesDialogController dialog) {
        runAction(() -> mainController.getMarketManager().buyShares(userName, eventId,
                dialog.getSelectedOptionIndex(), dialog.getQuantity()))
                .ifPresent(this::showReceipt);
    }

    private void showReceipt(PurchaseResultDto result) {
        Dialogs.<ReceiptDialogController>showReceipt(getClass(), RECEIPT_DIALOG, PURCHASE_COMPLETED,
                receipt -> receipt.setPurchaseReceipt(result.getTrade(), summary.getCommissionPercent(),
                        currentBalance()));
    }

    private TradeDto quote(int optionIndex, int quantity) {
        return mainController.getMarketManager().quotePurchase(userName, eventId, optionIndex, quantity);
    }

    private List<OptionStateDto> currentOptions() {
        return mainController.getMarketManager().getEventTradingState(eventId).getOptions();
    }

    private double currentBalance() {
        return currentUser().map(UserSummaryDto::getBalance).orElse(0.0);
    }

    // Runs an engine action, reports a rejected request, and refreshes both tabs afterwards.
    // The result is empty when the engine rejected the request.
    private <T> Optional<T> runAction(Supplier<T> action) {
        boolean wasBlocked = isBlocked();
        T result;

        try { // Execute the requested engine operation
            result = action.get();
        } catch (InvalidRequestException exception) {
            Dialogs.showError(ACTION_FAILED, exception.getMessage());
            return Optional.empty();
        }
        mainController.refreshAll(); // Publish the updated system state
        refresh(userName, eventId, marketMaker);
        warnIfNewlyBlocked(wasBlocked); // Report a new account restriction

        return Optional.of(result);
    }

    // The engine allows a balance to go negative and blocks the user; the user has to be told.
    private void warnIfNewlyBlocked(boolean wasBlocked) {
        if (!wasBlocked && isBlocked()) {
            Dialogs.showWarning(ACCOUNT_BLOCKED, BLOCKED_MESSAGE);
        }
    }

    private boolean isBlocked() {
        return currentUser().map(UserSummaryDto::isBlocked).orElse(false);
    }

    private Optional<UserSummaryDto> currentUser() {
        return mainController.getMarketManager().getUserSummaries().stream()
                .filter(user -> user.getName().equals(userName))
                .findFirst();
    }


    private void initTradesTable() {
        tradeOptionColumn.setCellValueFactory(new PropertyValueFactory<>("optionName"));
        tradeSharesColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        tradePaidColumn.setCellValueFactory(new PropertyValueFactory<>("sharesCost"));
        tradeCommissionColumn.setCellValueFactory(new PropertyValueFactory<>("commissionCost"));
        tradeTotalPaidColumn.setCellValueFactory(new PropertyValueFactory<>("totalPaid"));
        tradePaidColumn.setCellFactory(column -> amountCell());
        tradeCommissionColumn.setCellFactory(column -> amountCell());
        tradeTotalPaidColumn.setCellFactory(column -> amountCell());

        userTradesTable.setItems(userTrades);
    }

    private TableCell<TradeTableRow, Number> amountCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Number value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : DisplayText.amount(value.doubleValue()));
            }
        };
    }

    private void showEventSummary(EventSummaryDto summary) {
        eventNameLabel.setText(summary.getEventName());
        eventStatusLabel.setText(DisplayText.of(summary.getStatus()));
        StatusStyle.apply(eventStatusLabel, summary.getStatus());
        eventDescriptionLabel.setText(summary.getDescription());
        eventCommissionLabel.setText(String.format(COMMISSION_FORMAT, summary.getCommissionPercent(),
                DisplayText.of(summary.getCommissionPolicy())));
        eventAccountLabel.setText(DisplayText.amount(summary.getAccountBalance()));
    }

    private void showOptionPrices(List<OptionStateDto> options) {
        if (options.size() < OPTION_COUNT) {
            clearOptionPrices();
            return;
        }
        showOptionPrice(options.get(0), firstOptionNameLabel, firstOptionPriceLabel);
        showOptionPrice(options.get(1), secondOptionNameLabel, secondOptionPriceLabel);
    }

    private void showOptionPrice(OptionStateDto option, Label nameLabel, Label priceLabel) {
        nameLabel.setText(option.getOptionName());
        priceLabel.setText(DisplayText.optionalAmount(option.getCurrentValue()));
    }

    private void clearOptionPrices() {
        firstOptionNameLabel.setText(EMPTY_TEXT);
        firstOptionPriceLabel.setText(EMPTY_TEXT);
        secondOptionNameLabel.setText(EMPTY_TEXT);
        secondOptionPriceLabel.setText(EMPTY_TEXT);
    }

    // A blocked user keeps seeing the event but can no longer act on it, except for closing it.
    private void showActionButtons(EventStatusDto status, boolean marketMaker, boolean blocked) {
        boolean closed = status == EventStatusDto.CLOSED;
        boolean opening = status == EventStatusDto.NOT_STARTED;

        changeEventStatusButton.setText(opening ? OPEN_EVENT : CLOSE_EVENT);
        changeEventStatusButton.setVisible(!closed);
        changeEventStatusButton.setManaged(!closed);
        changeEventStatusButton.setDisable(!marketMaker || (blocked && opening));

        buySharesButton.setDisable(status != EventStatusDto.ACTIVE || blocked);
    }

    // Swaps the trading card for the result card once the event is resolved.
    private void showResultCard(EventTradingStateDto state, UserInvolvementDto involvement) {
        boolean closed = state.getSummary().getStatus() == EventStatusDto.CLOSED;

        tradingCard.setVisible(!closed);
        resultCardComponent.setVisible(closed);

        if (closed) {
            resultCardComponentController.refresh(state.getSummary().getWinningOptionName(),
                    state.getSummary().getWinningSharePayout(), involvement.getHoldings(),
                    involvement.getFinalProfitLoss());
        }
    }

    private void showUserTrades(UserInvolvementDto involvement) {
        userTrades.setAll(involvement.getTrades().stream().map(TradeTableRow::new).toList());
        totalCommissionPaidLabel.setText(
                DisplayText.amount(involvement.getTotalCommissionPaid()));
    }
}
