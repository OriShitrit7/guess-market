package guessmarket.ui.javafx.components.users.orderbooktrade;

import guessmarket.dto.EventStatusDto;
import guessmarket.dto.EventSummaryDto;
import guessmarket.dto.OptionBookDto;
import guessmarket.dto.OrderBookStateDto;
import guessmarket.dto.OrderEntryDto;
import guessmarket.dto.OrderResultDto;
import guessmarket.dto.UserInvolvementDto;
import guessmarket.dto.UserSummaryDto;
import guessmarket.ui.javafx.components.DisplayText;
import guessmarket.ui.javafx.components.StatusStyle;
import guessmarket.ui.javafx.components.main.MainController;
import guessmarket.dto.OrderActionDto;
import guessmarket.engine.exception.InvalidRequestException;
import guessmarket.ui.javafx.components.users.ResultCardController;
import guessmarket.ui.javafx.components.users.dialogs.Dialogs;
import guessmarket.ui.javafx.components.users.dialogs.closeevent.CloseEventDialogController;
import guessmarket.ui.javafx.components.users.dialogs.receipt.ReceiptDialogController;
import guessmarket.ui.javafx.components.users.dialogs.submitorder.SubmitOrderDialogController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.controlsfx.control.SegmentedButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class OrderBookTradeController {
    private static final String NO_SELECTION = "-";
    private static final String EMPTY_TEXT = "";
    private static final String EMPTY_AMOUNT = "$0.00";
    private static final String NO_PRICE = "-";
    private static final String COMMISSION_FORMAT = "%d%% %s";
    private static final String OPEN_EVENT = "Open Event";
    private static final String CLOSE_EVENT = "Close Event";
    private static final String BUY_SIDE = "Buy";
    private static final String SELL_SIDE = "Sell";
    private static final String NO_ORDERS = "No open orders";
    private static final String NO_ORDERS_CLOSED = "No open orders - event is closed";
    private static final String NO_HOLDINGS = "No shares held";
    private static final int OPTION_COUNT = 2;

    private static final String SUBMIT_ORDER_DIALOG = "/guessmarket/ui/javafx/components/users/dialogs/submitorder/submit-order-dialog.fxml";
    private static final String RECEIPT_DIALOG = "/guessmarket/ui/javafx/components/users/dialogs/receipt/receipt-dialog.fxml";
    private static final String ORDER_SUBMITTED = "Order Submitted";
    private static final String CLOSE_EVENT_DIALOG = "/guessmarket/ui/javafx/components/users/dialogs/closeevent/close-event-dialog.fxml";
    private static final String SUBMIT_ORDER = "Submit Order";
    private static final String ACTION_FAILED = "Action Failed";
    private static final String ACCOUNT_BLOCKED = "Account Blocked";
    private static final String BLOCKED_MESSAGE = "Your balance is now negative. Your account has been blocked and cannot perform further actions.";
    private static final String OPEN_EVENT_FORMAT =
            "Opening \"%s\" will charge you %s.%nYour balance is %s.%n%nOpen the event ?";

    private MainController mainController;
    private String userName;
    private int eventId;
    private boolean marketMaker;
    private OrderBookStateDto state;

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
    private VBox tradingCard;
    @FXML
    private VBox resultCardComponent;
    @FXML
    private ResultCardController resultCardComponentController;

    @FXML
    private Label firstOptionNameLabel;
    @FXML
    private Label firstOptionBidLabel;
    @FXML
    private Label firstOptionAskLabel;
    @FXML
    private Label secondOptionNameLabel;
    @FXML
    private Label secondOptionBidLabel;
    @FXML
    private Label secondOptionAskLabel;

    @FXML
    private Button changeEventStatusButton;
    @FXML
    private Button submitOrderButton;

    @FXML
    private SegmentedButton tableSelector;
    @FXML
    private ToggleButton holdingsToggle;

    @FXML
    private TableView<HoldingTableRow> userHoldingsTable;
    @FXML
    private TableColumn<HoldingTableRow, String> holdingOptionColumn;
    @FXML
    private TableColumn<HoldingTableRow, Number> holdingSharesColumn;
    @FXML
    private TableColumn<HoldingTableRow, Number> holdingPaidColumn;

    @FXML
    private TableView<OrderTableRow> userOrdersTable;
    @FXML
    private TableColumn<OrderTableRow, String> orderOptionColumn;
    @FXML
    private TableColumn<OrderTableRow, String> orderSideColumn;
    @FXML
    private TableColumn<OrderTableRow, Number> orderSharesColumn;
    @FXML
    private TableColumn<OrderTableRow, Number> orderPriceColumn;

    @FXML
    private Label totalCommissionPaidLabel;

    private final ObservableList<HoldingTableRow> userHoldings = FXCollections.observableArrayList();
    private final ObservableList<OrderTableRow> pendingOrders = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        tradingCard.managedProperty().bind(tradingCard.visibleProperty());
        resultCardComponent.managedProperty().bind(resultCardComponent.visibleProperty());
        userHoldingsTable.managedProperty().bind(userHoldingsTable.visibleProperty());
        userOrdersTable.managedProperty().bind(userOrdersTable.visibleProperty());

        initHoldingsTable();
        initOrdersTable();
        initTableSelector();
        clear();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // Fills the panel with one user's involvement in one order book event.
    public void refresh(String userName, int eventId, boolean marketMaker) {
        this.userName = userName;
        this.eventId = eventId;
        this.marketMaker = marketMaker;

        state = mainController.getMarketManager().getOrderBookState(eventId);
        UserInvolvementDto involvement =
                mainController.getMarketManager().getUserInvolvement(userName, eventId);

        showEventSummary(state.getSummary());
        showBookPrices(state.getOptionBooks());
        showActionButtons(state.getSummary().getStatus(), marketMaker, isBlocked());
        showHoldings(involvement);
        showPendingOrders(userName, state);
        showResultCard(state.getSummary(), involvement);
    }

    public void clear() {
        eventNameLabel.setText(NO_SELECTION);
        eventStatusLabel.setText(EMPTY_TEXT);
        StatusStyle.clear(eventStatusLabel);
        eventDescriptionLabel.setText(EMPTY_TEXT);
        eventCommissionLabel.setText(EMPTY_TEXT);
        eventAccountLabel.setText(EMPTY_AMOUNT);

        clearBookPrices();

        tradingCard.setVisible(true);
        resultCardComponent.setVisible(false);

        userHoldings.clear();
        pendingOrders.clear();
        totalCommissionPaidLabel.setText(EMPTY_AMOUNT);

        changeEventStatusButton.setDisable(true);
        submitOrderButton.setDisable(true);
    }

    @FXML
    public void changeEventStatusButtonAction() {
        if (state.getSummary().getStatus() == EventStatusDto.NOT_STARTED) {
            confirmAndOpenEvent();
            return;
        }
        Dialogs.<CloseEventDialogController>show(getClass(), CLOSE_EVENT_DIALOG, CLOSE_EVENT,
                        dialog -> dialog.setEvent(state.getSummary().getEventName(),
                                state.getSummary().getOptionNames()))
                .ifPresent(dialog -> runAction(() -> mainController.getMarketManager()
                        .closeEvent(userName, eventId, dialog.getWinningOptionIndex())));
    }

    // The market maker pays the opening cost, so the amount is shown before it is charged.
    private void confirmAndOpenEvent() {
        String message = String.format(OPEN_EVENT_FORMAT, state.getSummary().getEventName(),
                DisplayText.amount(state.getSummary().getOpeningCost()),
                DisplayText.amount(currentBalance()));

        if (Dialogs.confirm(OPEN_EVENT, message)) {
            runAction(() -> mainController.getMarketManager().openEvent(userName, eventId));
        }
    }

    @FXML
    public void submitOrderButtonAction() {
        EventSummaryDto summary = state.getSummary();
        String dialogTitle = summary.getEventName() + " - " + SUBMIT_ORDER;

        Dialogs.<SubmitOrderDialogController>show(getClass(), SUBMIT_ORDER_DIALOG, dialogTitle,
                        dialog -> dialog.setEvent(summary.getEventName(), summary.getOptionNames(),
                                availableShares(), currentBalance(), summary.getCommissionPercent(),
                                baseValue()))
                .ifPresent(this::submitOrder);
    }

    private void submitOrder(SubmitOrderDialogController dialog) {
        int optionIndex = dialog.getSelectedOptionIndex();
        String optionName = state.getSummary().getOptionNames().get(optionIndex);
        int quantity = dialog.getQuantity();
        double pricePerShare = dialog.getPricePerShare();
        OrderActionDto action = dialog.isBuy() ? OrderActionDto.BUY : OrderActionDto.SELL;

        runAction(() -> mainController.getMarketManager().submitOrder(userName, eventId, optionIndex,
                action, quantity, pricePerShare))
                .ifPresent(result -> showReceipt(result, optionName, quantity, pricePerShare));
    }

    // The order may have executed below its price, or not at all, so the outcome is reported back.
    private void showReceipt(OrderResultDto result, String optionName, int quantity, double pricePerShare) {
        Dialogs.<ReceiptDialogController>showReceipt(getClass(), RECEIPT_DIALOG, ORDER_SUBMITTED,
                receipt -> receipt.setOrderReceipt(result, optionName, quantity, pricePerShare,
                        currentBalance()));
    }

    // Shares the user still owns and has not already committed to a pending sell order.
    private List<Integer> availableShares() {
        List<Integer> available = new ArrayList<>();

        for (OptionBookDto book : state.getOptionBooks()) {
            int held = userHoldings.stream()
                    .filter(holding -> holding.getOptionName().equals(book.getOptionName()))
                    .mapToInt(HoldingTableRow::getQuantity)
                    .findFirst()
                    .orElse(0);
            int pending = book.getSellOrders().stream()
                    .filter(order -> order.getUserName().equals(userName))
                    .mapToInt(OrderEntryDto::getQuantity)
                    .sum();

            available.add(held - pending);
        }
        return available;
    }

    private int baseValue() {
        return (int) Math.round(state.getSummary().getWinningSharePayout());
    }

    private double currentBalance() {
        return currentUser().map(UserSummaryDto::getBalance).orElse(0.0);
    }

    // Runs an engine action, reports a rejected request, and refreshes both tabs afterwards.
    // The result is empty when the engine rejected the request.
    private <T> Optional<T> runAction(Supplier<T> action) {
        boolean wasBlocked = isBlocked();
        T result;

        try {
            result = action.get();
        } catch (InvalidRequestException exception) {
            Dialogs.showError(ACTION_FAILED, exception.getMessage());
            return Optional.empty();
        }
        mainController.refreshAll();
        refresh(userName, eventId, marketMaker);
        warnIfNewlyBlocked(wasBlocked);

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


    private void initHoldingsTable() {
        holdingOptionColumn.setCellValueFactory(new PropertyValueFactory<>("optionName"));
        holdingSharesColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        holdingPaidColumn.setCellValueFactory(new PropertyValueFactory<>("amountPaid"));
        holdingPaidColumn.setCellFactory(column -> amountCell());

        userHoldingsTable.setPlaceholder(new Label(NO_HOLDINGS));
        userHoldingsTable.setItems(userHoldings);
    }

    private void initOrdersTable() {
        orderOptionColumn.setCellValueFactory(new PropertyValueFactory<>("optionName"));
        orderSideColumn.setCellValueFactory(new PropertyValueFactory<>("side"));
        orderSharesColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        orderPriceColumn.setCellValueFactory(new PropertyValueFactory<>("pricePerShare"));
        orderPriceColumn.setCellFactory(column -> amountCell());

        userOrdersTable.setItems(pendingOrders);
    }

    // Keeps exactly one table visible, and prevents the user from clearing both toggles.
    private void initTableSelector() {
        tableSelector.getToggleGroup().selectedToggleProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue == null) {
                        tableSelector.getToggleGroup().selectToggle(oldValue);
                    }
                    showSelectedTable();
                });
        showSelectedTable();
    }

    private void showSelectedTable() {
        userHoldingsTable.setVisible(holdingsToggle.isSelected());
        userOrdersTable.setVisible(!holdingsToggle.isSelected());
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

    private void showBookPrices(List<OptionBookDto> books) {
        if (books.size() < OPTION_COUNT) {
            clearBookPrices();
            return;
        }
        showBookPrice(books.get(0), firstOptionNameLabel, firstOptionBidLabel, firstOptionAskLabel);
        showBookPrice(books.get(1), secondOptionNameLabel, secondOptionBidLabel, secondOptionAskLabel);
    }

    private void showBookPrice(OptionBookDto book, Label nameLabel, Label bidLabel, Label askLabel) {
        nameLabel.setText(book.getOptionName());
        bidLabel.setText(DisplayText.optionalAmount(book.getBid()));
        askLabel.setText(DisplayText.optionalAmount(book.getAsk()));
    }

    private void clearBookPrices() {
        firstOptionNameLabel.setText(EMPTY_TEXT);
        firstOptionBidLabel.setText(NO_PRICE);
        firstOptionAskLabel.setText(NO_PRICE);
        secondOptionNameLabel.setText(EMPTY_TEXT);
        secondOptionBidLabel.setText(NO_PRICE);
        secondOptionAskLabel.setText(NO_PRICE);
    }

    // A blocked user keeps seeing the event but can no longer act on it.
    private void showActionButtons(EventStatusDto status, boolean marketMaker, boolean blocked) {
        boolean closed = status == EventStatusDto.CLOSED;

        changeEventStatusButton.setText(status == EventStatusDto.NOT_STARTED ? OPEN_EVENT : CLOSE_EVENT);
        changeEventStatusButton.setVisible(!closed);
        changeEventStatusButton.setManaged(!closed);
        changeEventStatusButton.setDisable(!marketMaker || blocked);

        submitOrderButton.setDisable(status != EventStatusDto.ACTIVE || blocked);
    }

    private void showHoldings(UserInvolvementDto involvement) {
        userHoldings.setAll(involvement.getHoldings().stream()
                .filter(holding -> holding.getQuantity() > 0)
                .map(HoldingTableRow::new)
                .toList());
        totalCommissionPaidLabel.setText(
                DisplayText.amount(involvement.getTotalCommissionPaid()));
    }

    private void showPendingOrders(String userName, OrderBookStateDto state) {
        boolean closed = state.getSummary().getStatus() == EventStatusDto.CLOSED;
        List<OrderTableRow> rows = new ArrayList<>();

        for (OptionBookDto book : state.getOptionBooks()) {
            addOrdersOf(userName, book, book.getBuyOrders(), BUY_SIDE, rows);
            addOrdersOf(userName, book, book.getSellOrders(), SELL_SIDE, rows);
        }

        userOrdersTable.setPlaceholder(new Label(closed ? NO_ORDERS_CLOSED : NO_ORDERS));
        pendingOrders.setAll(rows);
    }

    private void addOrdersOf(String userName, OptionBookDto book, List<OrderEntryDto> orders,
                             String side, List<OrderTableRow> rows) {
        orders.stream()
                .filter(order -> order.getUserName().equals(userName))
                .forEach(order -> rows.add(new OrderTableRow(book.getOptionName(), side, order)));
    }

    // Swaps the trading card for the result card once the event is resolved.
    private void showResultCard(EventSummaryDto summary, UserInvolvementDto involvement) {
        boolean closed = summary.getStatus() == EventStatusDto.CLOSED;

        tradingCard.setVisible(!closed);
        resultCardComponent.setVisible(closed);

        if (closed) {
            resultCardComponentController.refresh(summary.getWinningOptionName(),
                    summary.getWinningSharePayout(), involvement.getHoldings(),
                    involvement.getFinalProfitLoss());
        }
    }

    private <T> TableCell<T, Number> amountCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Number value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : DisplayText.amount(value.doubleValue()));
            }
        };
    }

}
