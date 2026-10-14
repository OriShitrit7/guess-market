package guessmarket.client.components.account.accountdetails.myevents.lmsrtrade;

import guessmarket.dto.EventStatusDto;
import guessmarket.dto.EventSummaryDto;
import guessmarket.dto.LmsrStateDto;
import guessmarket.dto.OptionStateDto;
import guessmarket.dto.PurchaseResultDto;
import guessmarket.dto.UserInvolvementDto;
import guessmarket.dto.UserSummaryDto;
import guessmarket.client.components.DisplayText;
import guessmarket.client.components.StatusStyle;
import guessmarket.client.components.account.accountdetails.myevents.EventActions;
import guessmarket.client.components.account.accountdetails.myevents.ResultCardController;
import guessmarket.client.components.dialogs.Dialogs;
import guessmarket.client.components.account.dialogs.buyshares.BuySharesDialogController;
import guessmarket.client.components.account.dialogs.closeevent.CloseEventDialogController;
import guessmarket.client.components.account.dialogs.receipt.ReceiptDialogController;
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

    private static final String BUY_SHARES_DIALOG = "/guessmarket/client/components/account/dialogs/buyshares/buy-shares-dialog.fxml";
    private static final String RECEIPT_DIALOG = "/guessmarket/client/components/account/dialogs/receipt/receipt-dialog.fxml";
    private static final String PURCHASE_COMPLETED = "Purchase Completed";
    private static final String CLOSE_EVENT_DIALOG = "/guessmarket/client/components/account/dialogs/closeevent/close-event-dialog.fxml";
    private static final String OPEN_EVENT_FORMAT =
            "Opening \"%s\" will charge you %s.%nYour balance is %s.%n%nOpen the event ?";

    private EventSummaryDto summary;
    private List<OptionStateDto> options;
    private UserSummaryDto user;
    private UserInvolvementDto position;

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

    // Shows the event's details, prices and action buttons. The user's state decides which actions
    // are available.
    public void showState(LmsrStateDto state, UserSummaryDto user, boolean marketMaker) {
        this.summary = state.getSummary();
        this.options = state.getOptionStates();
        this.user = user;

        showEventSummary(summary);
        showOptionPrices(options);
        showActionButtons(summary.getStatus(), marketMaker, user.isBlocked());
        showResultCard();
    }

    // Shows the user's own activity in the event.
    public void showPosition(UserInvolvementDto position) {
        this.position = position;

        showUserTrades(position);
        showResultCard();
    }

    // Resets the panel when no LMSR event is selected.
    public void clear() {
        summary = null;
        position = null;

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
    public void changeEventStatusButtonAction() {
        if (summary.getStatus() == EventStatusDto.NOT_STARTED) {
            confirmAndOpenEvent();
            return;
        }
        Dialogs.<CloseEventDialogController>show(getClass(), CLOSE_EVENT_DIALOG, CLOSE_EVENT,
                        dialog -> dialog.setEvent(summary.getEventName(), summary.getOptionNames()))
                .ifPresent(dialog -> EventActions.closeEvent(summary.getEventId(), dialog.getWinningOptionIndex()));
    }

    // The market maker pays the opening cost, so the amount is shown before it is charged.
    private void confirmAndOpenEvent() {
        String message = String.format(OPEN_EVENT_FORMAT, summary.getEventName(),
                DisplayText.amount(summary.getOpeningCost()), DisplayText.amount(user.getBalance()));

        if (Dialogs.confirm(OPEN_EVENT, message)) {
            EventActions.openEvent(summary.getEventId());
        }
    }

    @FXML
    // Opens the purchase dialog and executes the confirmed LMSR purchase.
    public void buySharesButtonAction() {
        String dialogTitle = summary.getEventName() + " - " + BUY_SHARES;
        int eventId = summary.getEventId();

        Dialogs.<BuySharesDialogController>show(getClass(), BUY_SHARES_DIALOG, dialogTitle,
                        dialog -> dialog.setEvent(summary.getEventName(), options, user.getBalance(), summary.getCommissionPercent(),
                                (optionIndex, quantity) -> EventActions.quoteLmsrPurchase(eventId, optionIndex, quantity,
                                        trade -> dialog.showQuote(optionIndex, quantity, trade))))
                .ifPresent(dialog -> EventActions.buyLmsrShares(eventId, dialog.getSelectedOptionIndex(), dialog.getQuantity(),
                        this::showReceipt));
    }

    private void showReceipt(PurchaseResultDto result) {
        Dialogs.<ReceiptDialogController>showReceipt(getClass(), RECEIPT_DIALOG, PURCHASE_COMPLETED,
                receipt -> receipt.setPurchaseReceipt(result.getTrade(), result.getStateAfterPurchase().getSummary().getCommissionPercent(),
                        result.getBalanceAfter()));
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

    // Swaps the trading card for the result card once the event is resolved. It waits until both the
    // event state and the user's position have arrived.
    private void showResultCard() {
        if (summary == null || position == null) {
            return;
        }
        boolean closed = summary.getStatus() == EventStatusDto.CLOSED;

        tradingCard.setVisible(!closed);
        resultCardComponent.setVisible(closed);

        if (closed) {
            resultCardComponentController.refresh(summary.getWinningOptionName(),
                    summary.getWinningSharePayout(), position.getHoldings(),
                    position.getFinalProfitLoss());
        }
    }

    private void showUserTrades(UserInvolvementDto involvement) {
        userTrades.setAll(involvement.getTrades().stream().map(TradeTableRow::new).toList());
        totalCommissionPaidLabel.setText(
                DisplayText.amount(involvement.getTotalCommissionPaid()));
    }
}
