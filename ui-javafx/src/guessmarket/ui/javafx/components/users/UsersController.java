package guessmarket.ui.javafx.components.users;

import guessmarket.dto.EventParticipationDto;
import guessmarket.dto.EventStatusDto;
import guessmarket.dto.EventSummaryDto;
import guessmarket.dto.TradingMethodTypeDto;
import guessmarket.dto.UserSummaryDto;
import guessmarket.ui.javafx.components.main.MainController;
import guessmarket.ui.javafx.components.users.dialogs.Dialogs;
import guessmarket.ui.javafx.components.users.dialogs.tradeinevent.TradeInEventDialogController;
import guessmarket.ui.javafx.components.users.lmsrtrade.LmsrTradeController;
import guessmarket.ui.javafx.components.users.orderbooktrade.OrderBookTradeController;
import guessmarket.ui.javafx.components.DisplayText;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UsersController {
    private static final String NO_SELECTION = "-";
    private static final String EMPTY_AMOUNT = "$0.00";
    private static final String TRADE_IN_EVENT_DIALOG =
            "/guessmarket/ui/javafx/components/users/dialogs/tradeinevent/trade-in-event-dialog.fxml";
    private static final String TRADE_IN_NEW_EVENT = "Trade in New Event";
    private static final String USER_CARD_FXML = "user-card.fxml";
    private static final String EMPTY_USERS_BEFORE_LOAD = "Load a file to view users";
    private static final String EMPTY_USERS_AFTER_LOAD = "No users are available";

    private MainController mainController;
    // The event the details panel is showing, which is not always a row in the events table.
    private Integer displayedEventId;
    private UserTableRow selectedUser;
    private boolean usersWereLoaded;

    @FXML
    private FlowPane userCardsPane;
    @FXML
    private Label emptyUsersLabel;

    @FXML
    private Label selectedUserNameLabel;
    @FXML
    private Label userBalanceLabel;
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

    @FXML
    private void initialize() {
        emptyUsersLabel.managedProperty().bind(emptyUsersLabel.visibleProperty());
        userEventsTable.setPlaceholder(new Label("Select a user to view their events"));

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
        selectedUserNameLabel.setText(selectedUser.getName());
        userBalanceLabel.setText(DisplayText.amount(selectedUser.getBalance()));
        tradeInNewEventButton.setDisable(false);

        refreshUserEvents(selectedUser.getName());
    }

    private void clearUserDetails() {
        selectedUser = null;
        displayedUserCards.forEach(card -> card.setSelected(false));
        selectedUserNameLabel.setText(NO_SELECTION);
        userBalanceLabel.setText(EMPTY_AMOUNT);
        tradeInNewEventButton.setDisable(true);

        userEvents.clear();
        clearEventDetails();
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

    private void refreshUserEvents(String userName) {
        List<EventParticipationDto> participations =
                mainController.getMarketManager().getUserEvents(userName);

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
