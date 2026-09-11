package guessmarket.ui.javafx.components.users;

import guessmarket.dto.EventParticipationDto;
import guessmarket.dto.EventStatusDto;
import guessmarket.dto.EventSummaryDto;
import guessmarket.dto.EventTradingStateDto;
import guessmarket.dto.OptionStateDto;
import guessmarket.dto.UserInvolvementDto;
import guessmarket.dto.UserSummaryDto;
import guessmarket.ui.javafx.components.DisplayText;
import guessmarket.ui.javafx.components.main.MainController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class UsersController {
    private static final String NO_SELECTION = "-";
    private static final String EMPTY_TEXT = "";
    private static final String EMPTY_AMOUNT = "0.00";
    private static final String AMOUNT_FORMAT = "%.2f";
    private static final String COMMISSION_FORMAT = "%d%% %s";
    private static final String OPEN_EVENT = "Open Event";
    private static final String CLOSE_EVENT = "Close Event";
    private static final int OPTION_COUNT = 2;

    private MainController mainController;

    @FXML
    private TableView<UserTableRow> usersTable;
    @FXML
    private TableColumn<UserTableRow, String> userNameColumn;
    @FXML
    private TableColumn<UserTableRow, String> userStatusColumn;

    @FXML
    private Label selectedUserNameLabel;
    @FXML
    private Label userBalanceLabel;

    @FXML
    private TableView<UserEventTableRow> userEventsTable;
    @FXML
    private TableColumn<UserEventTableRow, String> userEventNameColumn;
    @FXML
    private TableColumn<UserEventTableRow, String> userEventStatusColumn;
    @FXML
    private TableColumn<UserEventTableRow, String> userEventMethodColumn;
    @FXML
    private TableColumn<UserEventTableRow, String> userEventRoleColumn;

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
    private TableView<TradeTableRow> userTradesTable;
    @FXML
    private TableColumn<TradeTableRow, String> tradeOptionColumn;
    @FXML
    private TableColumn<TradeTableRow, Number> tradeSharesColumn;
    @FXML
    private TableColumn<TradeTableRow, Number> tradePaidColumn;
    @FXML
    private Label totalCommissionPaidLabel;

    @FXML
    private Button changeEventStatusButton;
    @FXML
    private Button buySharesButton;

    private final ObservableList<UserTableRow> users = FXCollections.observableArrayList();
    private final ObservableList<UserEventTableRow> userEvents = FXCollections.observableArrayList();
    private final ObservableList<TradeTableRow> userTrades = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        usersTable.setPlaceholder(new Label("Load a file to view users"));
        userEventsTable.setPlaceholder(new Label("Select a user to view their events"));

        initUsersTable();
        initUserEventsTable();
        initUserTradesTable();
        initSelectionListeners();

        clearUserDetails();
        clearEventDetails();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void refreshUsers() {
        List<UserSummaryDto> summaries = mainController.getMarketManager().getUserSummaries();
        List<UserTableRow> rows = summaries.stream().map(UserTableRow::new).toList();

        users.setAll(rows);
    }

    @FXML
    public void changeEventStatusButtonAction() {
    }

    @FXML
    public void buySharesButtonAction() {
    }

    private void initUsersTable() {
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        userStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        usersTable.setItems(users);
    }

    private void initUserEventsTable() {
        userEventNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        userEventStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        userEventMethodColumn.setCellValueFactory(new PropertyValueFactory<>("methodType"));
        userEventRoleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        userEventsTable.setItems(userEvents);
    }

    private void initUserTradesTable() {
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

    private void initSelectionListeners() {
        usersTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showUserDetails(newValue));

        userEventsTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showEventDetails(newValue));
    }

    private void showUserDetails(UserTableRow selectedUser) {
        if (selectedUser == null) {
            clearUserDetails();
            return;
        }
        selectedUserNameLabel.setText(selectedUser.getName());
        userBalanceLabel.setText(String.format(AMOUNT_FORMAT, selectedUser.getBalance()));

        refreshUserEvents(selectedUser.getName());
    }

    private void clearUserDetails() {
        selectedUserNameLabel.setText(NO_SELECTION);
        userBalanceLabel.setText(EMPTY_AMOUNT);

        userEvents.clear();
    }

    private void refreshUserEvents(String userName) {
        List<EventParticipationDto> participations =
                mainController.getMarketManager().getUserEvents(userName);

        userEvents.setAll(participations.stream().map(UserEventTableRow::new).toList());
    }

    private void showEventDetails(UserEventTableRow selectedEvent) {
        if (selectedEvent == null) {
            clearEventDetails();
            return;
        }
        EventTradingStateDto state =
                mainController.getMarketManager().getEventTradingState(selectedEvent.getEventId());

        showEventSummary(state.getSummary());
        showOptionPrices(state.getOptions());
        showActionButtons(state.getSummary().getStatus(), selectedEvent.isMarketMaker());
        showUserTrades(selectedEvent.getEventId());
    }

    private void showUserTrades(int eventId) {
        String userName = usersTable.getSelectionModel().getSelectedItem().getName();
        UserInvolvementDto involvement =
                mainController.getMarketManager().getUserInvolvement(userName, eventId);

        userTrades.setAll(involvement.getTrades().stream().map(TradeTableRow::new).toList());
        totalCommissionPaidLabel.setText(
                String.format(AMOUNT_FORMAT, involvement.getTotalCommissionPaid()));
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

    private void showActionButtons(EventStatusDto status, boolean marketMaker) {
        boolean closed = status == EventStatusDto.CLOSED;

        changeEventStatusButton.setText(status == EventStatusDto.NOT_STARTED ? OPEN_EVENT : CLOSE_EVENT);
        changeEventStatusButton.setVisible(!closed);
        changeEventStatusButton.setManaged(!closed);
        changeEventStatusButton.setDisable(!marketMaker);

        buySharesButton.setDisable(status != EventStatusDto.ACTIVE);
    }

    private void clearEventDetails() {
        eventNameLabel.setText(NO_SELECTION);
        eventStatusLabel.setText(EMPTY_TEXT);
        eventDescriptionLabel.setText(EMPTY_TEXT);
        eventCommissionLabel.setText(EMPTY_TEXT);
        eventAccountLabel.setText(EMPTY_AMOUNT);

        clearOptionPrices();

        userTrades.clear();
        totalCommissionPaidLabel.setText(EMPTY_AMOUNT);

        changeEventStatusButton.setDisable(true);
        buySharesButton.setDisable(true);
    }

    private void clearOptionPrices() {
        firstOptionNameLabel.setText(EMPTY_TEXT);
        firstOptionPriceLabel.setText(EMPTY_TEXT);
        secondOptionNameLabel.setText(EMPTY_TEXT);
        secondOptionPriceLabel.setText(EMPTY_TEXT);
    }
}
