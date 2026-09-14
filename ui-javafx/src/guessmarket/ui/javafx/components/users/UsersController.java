package guessmarket.ui.javafx.components.users;

import guessmarket.dto.BalancePointDto;
import guessmarket.dto.EventParticipationDto;
import guessmarket.dto.EventStatusDto;
import guessmarket.dto.EventSummaryDto;
import guessmarket.dto.NewEventDto;
import guessmarket.dto.TradingMethodTypeDto;
import guessmarket.dto.UserSummaryDto;
import guessmarket.ui.javafx.components.main.MainController;
import guessmarket.engine.exception.request.InvalidRequestException;
import guessmarket.ui.javafx.components.users.dialogs.Dialogs;
import guessmarket.ui.javafx.components.users.dialogs.createevent.CreateEventDialogController;
import guessmarket.ui.javafx.components.users.dialogs.tradeinevent.TradeInEventDialogController;
import guessmarket.ui.javafx.components.users.lmsrtrade.LmsrTradeController;
import guessmarket.ui.javafx.components.users.orderbooktrade.OrderBookTradeController;
import guessmarket.ui.javafx.components.DisplayText;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.SegmentedButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Manages user selection, account history and event trading in the Users tab.
public class UsersController {
    private static final String EMPTY_AMOUNT = "$0.00";
    private static final String TRADE_IN_EVENT_DIALOG =
            "/guessmarket/ui/javafx/components/users/dialogs/tradeinevent/trade-in-event-dialog.fxml";
    private static final String TRADE_IN_NEW_EVENT = "Trade in New Event";
    private static final String CREATE_EVENT_DIALOG =
            "/guessmarket/ui/javafx/components/users/dialogs/createevent/create-event-dialog.fxml";
    private static final String CREATE_EVENT = "Create Event";
    private static final String CREATE_FAILED = "Event Not Created";
    private static final String USER_CARD_FXML = "user-card.fxml";
    private static final String EMPTY_USERS_TITLE_BEFORE_LOAD = "No users to display";
    private static final String EMPTY_USERS_MESSAGE_BEFORE_LOAD = "Load an XML file to get started.";
    private static final String EMPTY_USERS_TITLE_AFTER_LOAD = "No users available";
    private static final String EMPTY_USERS_MESSAGE_AFTER_LOAD =
            "The loaded file does not contain any users.";
    private static final String NO_USER_SELECTED = "Select a user to view their events";
    private static final String NO_EVENTS_FORMAT = "%s is not taking part in any event yet";
    private static final String NO_BALANCE_HISTORY_TITLE = "No balance history";
    private static final String NO_BALANCE_HISTORY_MESSAGE = "This user has no balance changes yet.";
    private static final String SELECT_USER_FOR_BALANCE_TITLE = "No user selected";
    private static final String SELECT_USER_FOR_BALANCE_MESSAGE =
            "Select a user to view their balance history.";
    private static final int MAX_VISIBLE_USER_EVENT_ROWS = 3;
    private static final double USER_EVENT_ROW_HEIGHT = 44.0;
    private static final double USER_EVENT_HEADER_HEIGHT = 44.0;
    private static final double USER_EVENT_TABLE_BORDER_HEIGHT = 2.0;

    private MainController mainController;
    // The event the details panel is showing, which is not always a row in the events table.
    private Integer displayedEventId;
    private UserTableRow selectedUser;
    private boolean usersWereLoaded;

    @FXML
    private VBox userCardsPane;
    @FXML
    private VBox emptyUsersState;
    @FXML
    private Label emptyUsersTitleLabel;
    @FXML
    private Label emptyUsersLabel;

    @FXML
    private Label userBalanceLabel;
    @FXML
    private Button createEventButton;
    @FXML
    private Button tradeInNewEventButton;

    @FXML
    private SegmentedButton userDetailsSelector;
    @FXML
    private ToggleButton userEventsToggle;
    @FXML
    private VBox userEventsView;
    @FXML
    private StackPane balanceHistoryView;

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
    private LineChart<Number, Number> balanceHistoryChart;
    @FXML
    private NumberAxis balanceHistoryXAxis;
    @FXML
    private VBox emptyBalanceHistoryState;
    @FXML
    private Label emptyBalanceHistoryTitleLabel;
    @FXML
    private Label emptyBalanceHistoryLabel;

    @FXML
    private TitledPane eventDetailsPane;
    @FXML
    private VBox noEventSelectedState;
    @FXML
    private VBox lmsrTradeComponent;
    @FXML
    private LmsrTradeController lmsrTradeComponentController;
    @FXML
    private VBox orderBookComponent;
    @FXML
    private OrderBookTradeController orderBookComponentController;

    private final ObservableList<UserTableRow> users = FXCollections.observableArrayList();
    private final ObservableList<UserEventTableRow> userEvents = FXCollections.observableArrayList();
    private final List<UserCardController> displayedUserCards = new ArrayList<>();
    private final Label userEventsPlaceholder = new Label(NO_USER_SELECTED);
    private final XYChart.Series<Number, Number> balanceSeries = new XYChart.Series<>();

    @FXML
    // Configures the user cards, details selector, tables and initial empty state.
    private void initialize() {
        emptyUsersState.managedProperty().bind(emptyUsersState.visibleProperty());
        userEventsTable.setPlaceholder(userEventsPlaceholder);
        balanceHistoryChart.getData().add(balanceSeries);
        clearBalanceHistory();
        initUserDetailsSelector();

        noEventSelectedState.managedProperty().bind(noEventSelectedState.visibleProperty());
        lmsrTradeComponent.managedProperty().bind(lmsrTradeComponent.visibleProperty());
        orderBookComponent.managedProperty().bind(orderBookComponent.visibleProperty());

        initUserEventsTable();
        initSelectionListeners();
        rebuildUserCards();

        clearUserDetails();
        clearEventDetails();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        lmsrTradeComponentController.setMainController(mainController);
        orderBookComponentController.setMainController(mainController);
    }

    // Reloads users and restores the selected user and event when they still exist.
    public void refreshUsers() {
        String userNameToRestore = selectedUser == null ? null : selectedUser.getName(); // Preserve user selection
        Integer selectedEventId = displayedEventId; // Preserve the displayed event

        List<UserSummaryDto> summaries = mainController.getMarketManager().getUserSummaries();
        users.setAll(summaries.stream().map(UserTableRow::new).toList()); // Replace displayed user data
        usersWereLoaded = true;

        selectedUser = findUser(userNameToRestore); // Restore only an existing user
        rebuildUserCards();

        if (selectedUser == null) {
            clearUserDetails();
            return;
        }

        showUserDetails(selectedUser);
        restoreEventSelection(selectedEventId); // Restore the event after its table is rebuilt
    }

    private UserTableRow findUser(String userName) {
        if (userName == null) {
            return null;
        }
        return users.stream()
                .filter(user -> user.getName().equals(userName))
                .findFirst()
                .orElse(null);
    }

    private void restoreEventSelection(Integer eventId) {
        if (eventId == null) {
            return;
        }
        userEvents.stream().filter(event -> event.getEventId() == eventId).findFirst()
                .ifPresent(event -> userEventsTable.getSelectionModel().select(event));
    }

    // Recreates the user cards and updates the message shown when no user is available.
    private void rebuildUserCards() {
        userCardsPane.getChildren().clear();
        displayedUserCards.clear();

        for (UserTableRow user : users) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(USER_CARD_FXML));
                Node userCard = loader.load();
                UserCardController controller = loader.getController();

                controller.setUser(user, this::selectUser);
                controller.setSelected(selectedUser != null && user.getName().equals(selectedUser.getName()));
                displayedUserCards.add(controller);
                userCardsPane.getChildren().add(userCard);
            } catch (IOException exception) {
                throw new IllegalStateException("Could not load the user card", exception);
            }
        }

        boolean empty = users.isEmpty();
        emptyUsersTitleLabel.setText(usersWereLoaded
                ? EMPTY_USERS_TITLE_AFTER_LOAD
                : EMPTY_USERS_TITLE_BEFORE_LOAD);
        emptyUsersLabel.setText(usersWereLoaded
                ? EMPTY_USERS_MESSAGE_AFTER_LOAD
                : EMPTY_USERS_MESSAGE_BEFORE_LOAD);
        emptyUsersState.setVisible(empty);
    }

    // Changes the active user and clears details that belonged to the previous selection.
    private void selectUser(UserTableRow user) {
        if (selectedUser != null && selectedUser.getName().equals(user.getName())) {
            return;
        }

        selectedUser = user;
        displayedUserCards.forEach(card -> card.setSelected(card.getUserName().equals(user.getName())));
        userEventsTable.getSelectionModel().clearSelection();
        clearEventDetails();
        showUserDetails(user);
    }

    private void initUserEventsTable() {
        userEventNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        userEventStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        userEventMethodColumn.setCellValueFactory(new PropertyValueFactory<>("methodType"));
        userEventRoleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        userEventsTable.setFixedCellSize(USER_EVENT_ROW_HEIGHT);
        userEventsTable.setItems(userEvents);
        updateUserEventsTableHeight();
    }

    private void initSelectionListeners() {
        userEventsTable.getSelectionModel().selectedItemProperty().addListener(
                (_, _, newValue) -> showEventDetails(newValue));
    }

    // Keeps exactly one user-details view visible when the selector changes.
    private void initUserDetailsSelector() {
        userEventsView.managedProperty().bind(userEventsView.visibleProperty());
        balanceHistoryView.managedProperty().bind(balanceHistoryView.visibleProperty());
        userDetailsSelector.getToggleGroup().selectedToggleProperty().addListener(
                (_, oldValue, newValue) -> {
                    if (newValue == null) {
                        userDetailsSelector.getToggleGroup().selectToggle(oldValue);
                    }
                    showSelectedUserDetail();
                });
        showSelectedUserDetail();
    }

    private void showSelectedUserDetail() {
        boolean showEvents = userEventsToggle.isSelected();

        userEventsView.setVisible(showEvents);
        balanceHistoryView.setVisible(!showEvents);
    }

    private void showUserDetails(UserTableRow selectedUser) {
        if (selectedUser == null) {
            clearUserDetails();
            return;
        }
        userBalanceLabel.setText(DisplayText.amount(selectedUser.getBalance()));
        tradeInNewEventButton.setDisable(selectedUser.isBlocked());
        createEventButton.setDisable(selectedUser.isBlocked());

        refreshUserEvents(selectedUser.getName());
        refreshBalanceHistory(selectedUser.getName());
    }

    private void clearUserDetails() {
        selectedUser = null;
        displayedUserCards.forEach(card -> card.setSelected(false));
        userBalanceLabel.setText(EMPTY_AMOUNT);
        tradeInNewEventButton.setDisable(true);
        createEventButton.setDisable(true);
        userEventsPlaceholder.setText(NO_USER_SELECTED);

        userEvents.clear();
        updateUserEventsTableHeight();
        clearBalanceHistory();
        clearEventDetails();
    }

    // Rebuilds the selected user's balance series and updates its empty state.
    private void refreshBalanceHistory(String userName) {
        List<BalancePointDto> history = mainController.getMarketManager().getUserBalanceHistory(userName);

        balanceSeries.getData().setAll(history.stream()
                .map(point -> new XYChart.Data<Number, Number>(point.getSequence(), point.getBalance()))
                .toList());
        configureBalanceXAxis(history.size());
        emptyBalanceHistoryTitleLabel.setText(NO_BALANCE_HISTORY_TITLE);
        emptyBalanceHistoryLabel.setText(NO_BALANCE_HISTORY_MESSAGE);
        emptyBalanceHistoryState.setVisible(history.size() <= 1);
    }

    private void clearBalanceHistory() {
        balanceSeries.getData().clear();
        configureBalanceXAxis(0);
        emptyBalanceHistoryTitleLabel.setText(SELECT_USER_FOR_BALANCE_TITLE);
        emptyBalanceHistoryLabel.setText(SELECT_USER_FOR_BALANCE_MESSAGE);
        emptyBalanceHistoryState.setVisible(true);
    }

    // Scales the sequence axis to the number of available balance points.
    private void configureBalanceXAxis(int pointCount) {
        int maximumSequence = Math.max(1, pointCount - 1);

        balanceHistoryXAxis.setLowerBound(0);
        balanceHistoryXAxis.setUpperBound(maximumSequence);
        balanceHistoryXAxis.setTickUnit(Math.max(1, Math.ceil(maximumSequence / 6.0)));
    }

    // The selected user creates the event and becomes its market maker.
    @FXML
    private void createEventButtonAction() {
        if (selectedUser == null) {
            return;
        }
        Dialogs.<CreateEventDialogController>show(getClass(), CREATE_EVENT_DIALOG, CREATE_EVENT,
                        _ -> { })
                .ifPresent(dialog -> createEvent(dialog.getRequest()));
    }

    private void createEvent(NewEventDto request) {
        try {
            mainController.getMarketManager().createEvent(selectedUser.getName(), request);
        } catch (InvalidRequestException exception) {
            Dialogs.showError(CREATE_FAILED, exception.getMessage());
            return;
        }
        mainController.refreshAll();
    }

    @FXML
    private void tradeInNewEventButtonAction() {
        if (selectedUser == null) {
            return;
        }
        List<EventSummaryDto> availableEvents = availableEventsFor(selectedUser.getName());

        Dialogs.<TradeInEventDialogController>show(
                        getClass(), TRADE_IN_EVENT_DIALOG, TRADE_IN_NEW_EVENT,
                        dialog -> dialog.setEvents(availableEvents))
                .ifPresent(dialog -> showNewEventDetails(selectedUser, dialog.getSelectedEvent()));
    }

    // An empty table means something different before a user is chosen and after.
    private void refreshUserEvents(String userName) {
        List<EventParticipationDto> participations =
                mainController.getMarketManager().getUserEvents(userName);

        userEventsPlaceholder.setText(String.format(NO_EVENTS_FORMAT, userName));

        userEvents.setAll(participations.stream().map(UserEventTableRow::new).toList());
        updateUserEventsTableHeight();
    }

    // Sizes the table for up to three event rows and avoids unused empty space.
    private void updateUserEventsTableHeight() {
        int visibleRows = Math.max(1, Math.min(MAX_VISIBLE_USER_EVENT_ROWS, userEvents.size()));
        double tableHeight = USER_EVENT_HEADER_HEIGHT
                + visibleRows * USER_EVENT_ROW_HEIGHT
                + USER_EVENT_TABLE_BORDER_HEIGHT;

        userEventsTable.setPrefHeight(tableHeight);
    }

    // An event is available for a first trade only while active and unrelated to the selected user.
    private List<EventSummaryDto> availableEventsFor(String userName) {
        Set<Integer> relatedEventIds = new HashSet<>();

        mainController.getMarketManager().getUserEvents(userName).stream()
                .map(EventParticipationDto::getEvent)
                .map(EventSummaryDto::getEventId)
                .forEach(relatedEventIds::add);

        return mainController.getMarketManager().getEventSummaries().stream()
                .filter(event -> event.getStatus() == EventStatusDto.ACTIVE)
                .filter(event -> !relatedEventIds.contains(event.getEventId()))
                .toList();
    }

    private void showNewEventDetails(UserTableRow selectedUser, EventSummaryDto selectedEvent) {
        userEventsTable.getSelectionModel().clearSelection();
        showEventDetails(selectedUser.getName(), selectedEvent.getEventId(),
                selectedEvent.getMethodType() == TradingMethodTypeDto.ORDER_BOOK, false);
    }

    // Shows the panel that matches the selected event's trading method, and hides the other one.
    private void showEventDetails(UserEventTableRow selectedEvent) {
        if (selectedEvent == null || selectedUser == null) {
            clearEventDetails();
            return;
        }
        showEventDetails(selectedUser.getName(), selectedEvent.getEventId(),
                selectedEvent.isOrderBook(), selectedEvent.isMarketMaker());
    }

    private void showEventDetails(String userName, int eventId, boolean orderBook, boolean marketMaker) {
        displayedEventId = eventId;
        noEventSelectedState.setVisible(false);
        eventDetailsPane.setExpanded(true);
        lmsrTradeComponent.setVisible(!orderBook);
        orderBookComponent.setVisible(orderBook);

        if (orderBook) {
            orderBookComponentController.refresh(userName, eventId, marketMaker);
        } else {
            lmsrTradeComponentController.refresh(userName, eventId, marketMaker);
        }
    }

    private void clearEventDetails() {
        displayedEventId = null;
        noEventSelectedState.setVisible(true);
        lmsrTradeComponent.setVisible(false);
        orderBookComponent.setVisible(false);

        lmsrTradeComponentController.clear();
        orderBookComponentController.clear();
    }
}
