package guessmarket.ui.javafx.components.users;

import guessmarket.dto.BalancePointDto;
import guessmarket.dto.EventParticipationDto;
import guessmarket.dto.EventStatusDto;
import guessmarket.dto.EventSummaryDto;
import guessmarket.dto.NewEventDto;
import guessmarket.dto.TradingMethodTypeDto;
import guessmarket.dto.UserSummaryDto;
import guessmarket.ui.javafx.components.main.MainController;
import guessmarket.engine.exception.InvalidRequestException;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private static final String EMPTY_USERS_BEFORE_LOAD = "Load a file to view users";
    private static final String EMPTY_USERS_AFTER_LOAD = "No users are available";
    private static final String NO_USER_SELECTED = "Select a user to view their events";
    private static final String NO_EVENTS_FORMAT = "%s is not taking part in any event yet";
    private static final String NO_BALANCE_HISTORY = "No balance changes yet";
    private static final String SELECT_USER_FOR_BALANCE = "Select a user to view balance history";

    private MainController mainController;
    // The event the details panel is showing, which is not always a row in the events table.
    private Integer displayedEventId;
    private UserTableRow selectedUser;
    private boolean usersWereLoaded;

    @FXML
    private VBox userCardsPane;
    @FXML
    private Label emptyUsersLabel;

    @FXML
    private Label userBalanceLabel;
    @FXML
    private Button createEventButton;
    @FXML
    private Button tradeInNewEventButton;

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
    private Label emptyBalanceHistoryLabel;

    @FXML
    private TitledPane eventDetailsPane;
    @FXML
    private Label noEventSelectedLabel;
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
    private void initialize() {
        emptyUsersLabel.managedProperty().bind(emptyUsersLabel.visibleProperty());
        userEventsTable.setPlaceholder(userEventsPlaceholder);
        balanceHistoryChart.getData().add(balanceSeries);
        clearBalanceHistory();

        noEventSelectedLabel.managedProperty().bind(noEventSelectedLabel.visibleProperty());
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

    public void refreshUsers() {
        String userNameToRestore = selectedUser == null ? null : selectedUser.getName();
        Integer selectedEventId = displayedEventId;

        List<UserSummaryDto> summaries = mainController.getMarketManager().getUserSummaries();
        users.setAll(summaries.stream().map(UserTableRow::new).toList());
        usersWereLoaded = true;

        selectedUser = findUser(userNameToRestore);
        rebuildUserCards();

        if (selectedUser == null) {
            clearUserDetails();
            return;
        }

        showUserDetails(selectedUser);
        restoreEventSelection(selectedEventId);
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
        emptyUsersLabel.setText(usersWereLoaded ? EMPTY_USERS_AFTER_LOAD : EMPTY_USERS_BEFORE_LOAD);
        emptyUsersLabel.setVisible(empty);
    }

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

        userEventsTable.setItems(userEvents);
    }

    private void initSelectionListeners() {
        userEventsTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showEventDetails(newValue));
    }

    private void showUserDetails(UserTableRow selectedUser) {
        if (selectedUser == null) {
            clearUserDetails();
            return;
        }
        userBalanceLabel.setText(DisplayText.amount(selectedUser.getBalance()));
        tradeInNewEventButton.setDisable(false);
        createEventButton.setDisable(false);

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
        clearBalanceHistory();
        clearEventDetails();
    }

    private void refreshBalanceHistory(String userName) {
        List<BalancePointDto> history = mainController.getMarketManager().getUserBalanceHistory(userName);

        balanceSeries.getData().setAll(history.stream()
                .map(point -> new XYChart.Data<Number, Number>(point.getSequence(), point.getBalance()))
                .toList());
        configureBalanceXAxis(history.size());
        emptyBalanceHistoryLabel.setText(NO_BALANCE_HISTORY);
        emptyBalanceHistoryLabel.setVisible(history.size() <= 1);
    }

    private void clearBalanceHistory() {
        balanceSeries.getData().clear();
        configureBalanceXAxis(0);
        emptyBalanceHistoryLabel.setText(SELECT_USER_FOR_BALANCE);
        emptyBalanceHistoryLabel.setVisible(true);
    }

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
                        dialog -> { })
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
        noEventSelectedLabel.setVisible(false);
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
        noEventSelectedLabel.setVisible(true);
        lmsrTradeComponent.setVisible(false);
        orderBookComponent.setVisible(false);

        lmsrTradeComponentController.clear();
        orderBookComponentController.clear();
    }
}
