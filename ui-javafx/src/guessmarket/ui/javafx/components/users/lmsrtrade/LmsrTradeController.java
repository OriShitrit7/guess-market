package guessmarket.ui.javafx.components.users.lmsrtrade;

import guessmarket.dto.EventStatusDto;
import guessmarket.dto.EventSummaryDto;
import guessmarket.dto.EventTradingStateDto;
import guessmarket.dto.OptionStateDto;
import guessmarket.dto.TradeDto;
import guessmarket.dto.UserInvolvementDto;
import guessmarket.dto.UserSummaryDto;
import guessmarket.ui.javafx.components.DisplayText;
import guessmarket.ui.javafx.components.main.MainController;
import guessmarket.engine.exception.InvalidRequestException;
import guessmarket.ui.javafx.components.users.ResultCardController;
import guessmarket.ui.javafx.components.users.dialogs.Dialogs;
import guessmarket.ui.javafx.components.users.dialogs.buyshares.BuySharesDialogController;
import guessmarket.ui.javafx.components.users.dialogs.closeevent.CloseEventDialogController;
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

public class LmsrTradeController {
    private static final String NO_SELECTION = "-";
    private static final String EMPTY_TEXT = "";
    private static final String EMPTY_AMOUNT = "0.00";
    private static final String AMOUNT_FORMAT = "%.2f";
    private static final String COMMISSION_FORMAT = "%d%% %s";
    private static final String OPEN_EVENT = "Open Event";
    private static final String CLOSE_EVENT = "Close Event";
    private static final String BUY_SHARES = "Buy Shares";
    private static final int OPTION_COUNT = 2;

    private static final String BUY_SHARES_DIALOG = "/guessmarket/ui/javafx/components/users/dialogs/buyshares/buy-shares-dialog.fxml";
    private static final String CLOSE_EVENT_DIALOG = "/guessmarket/ui/javafx/components/users/dialogs/closeevent/close-event-dialog.fxml";
    private static final String ACTION_FAILED = "Action Failed";
    private static final String ACCOUNT_BLOCKED = "Account Blocked";
    private static final String BLOCKED_MESSAGE = "Your balance is now negative. Your account has been blocked and cannot perform further actions.";

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

        EventTradingStateDto state = mainController.getMarketManager().getEventTradingState(eventId);
        this.summary = state.getSummary();
        UserInvolvementDto involvement =
                mainController.getMarketManager().getUserInvolvement(userName, eventId);

        showEventSummary(state.getSummary());
        showOptionPrices(state.getOptions());
        showActionButtons(state.getSummary().getStatus(), marketMaker, isBlocked());
        showUserTrades(involvement);
        showResultCard(state, involvement);
    }

    public void clear() {
        eventNameLabel.setText(NO_SELECTION);
        eventStatusLabel.setText(EMPTY_TEXT);
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
            runAction(() -> mainController.getMarketManager().openEvent(userName, eventId));
            return;
        }
        Dialogs.<CloseEventDialogController>show(getClass(), CLOSE_EVENT_DIALOG, CLOSE_EVENT,
                        dialog -> dialog.setEvent(summary.getEventName(), summary.getOptionNames()))
                .ifPresent(dialog -> runAction(() -> mainController.getMarketManager()
                        .closeEvent(userName, eventId, dialog.getWinningOptionIndex())));
    }

    @FXML
    public void buySharesButtonAction() {
        Dialogs.<BuySharesDialogController>show(getClass(), BUY_SHARES_DIALOG, BUY_SHARES,
                        dialog -> dialog.setEvent(currentOptions(), currentBalance(),
                                summary.getCommissionPercent(), this::quote))
                .ifPresent(dialog -> runAction(() -> mainController.getMarketManager()
                        .buyShares(userName, eventId, dialog.getSelectedOptionIndex(), dialog.getQuantity())));
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
    private void runAction(Runnable action) {
        boolean wasBlocked = isBlocked();

        try {
            action.run();
        } catch (InvalidRequestException exception) {
            Dialogs.showError(ACTION_FAILED, exception.getMessage());
            return;
        }
        mainController.refreshAll();
        refresh(userName, eventId, marketMaker);
        warnIfNewlyBlocked(wasBlocked);
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
        tradePaidColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Number value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : String.format(AMOUNT_FORMAT, value.doubleValue()));
            }
        });

        userTradesTable.setItems(userTrades);
    }

    private void showEventSummary(EventSummaryDto summary) {
        eventNameLabel.setText(summary.getEventName());
        eventStatusLabel.setText(DisplayText.of(summary.getStatus()));
        eventDescriptionLabel.setText(summary.getDescription());
        eventCommissionLabel.setText(String.format(COMMISSION_FORMAT, summary.getCommissionPercent(),
                DisplayText.of(summary.getCommissionPolicy())));
        eventAccountLabel.setText(String.format(AMOUNT_FORMAT, summary.getAccountBalance()));
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
        priceLabel.setText(String.format(AMOUNT_FORMAT, option.getCurrentValue()));
    }

    private void clearOptionPrices() {
        firstOptionNameLabel.setText(EMPTY_TEXT);
        firstOptionPriceLabel.setText(EMPTY_TEXT);
        secondOptionNameLabel.setText(EMPTY_TEXT);
        secondOptionPriceLabel.setText(EMPTY_TEXT);
    }

    // A blocked user keeps seeing the event but can no longer act on it.
    private void showActionButtons(EventStatusDto status, boolean marketMaker, boolean blocked) {
        boolean closed = status == EventStatusDto.CLOSED;

        changeEventStatusButton.setText(status == EventStatusDto.NOT_STARTED ? OPEN_EVENT : CLOSE_EVENT);
        changeEventStatusButton.setVisible(!closed);
        changeEventStatusButton.setManaged(!closed);
        changeEventStatusButton.setDisable(!marketMaker || blocked);

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
                String.format(AMOUNT_FORMAT, involvement.getTotalCommissionPaid()));
    }
}
