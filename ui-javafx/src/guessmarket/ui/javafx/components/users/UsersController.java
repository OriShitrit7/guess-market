package guessmarket.ui.javafx.components.users;

import guessmarket.dto.EventParticipationDto;
import guessmarket.dto.UserSummaryDto;
import guessmarket.ui.javafx.components.main.MainController;
import guessmarket.ui.javafx.components.users.lmsrtrade.LmsrTradeController;
import guessmarket.ui.javafx.components.users.orderbooktrade.OrderBookTradeController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.util.List;

public class UsersController {
    private static final String NO_SELECTION = "-";
    private static final String EMPTY_AMOUNT = "0.00";
    private static final String AMOUNT_FORMAT = "%.2f";

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

    @FXML
    private void initialize() {
        usersTable.setPlaceholder(new Label("Load a file to view users"));
        userEventsTable.setPlaceholder(new Label("Select a user to view their events"));

        noEventSelectedLabel.managedProperty().bind(noEventSelectedLabel.visibleProperty());
        lmsrTradeComponent.managedProperty().bind(lmsrTradeComponent.visibleProperty());
        orderBookComponent.managedProperty().bind(orderBookComponent.visibleProperty());

        initUsersTable();
        initUserEventsTable();
        initSelectionListeners();

        clearUserDetails();
        clearEventDetails();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        lmsrTradeComponentController.setMainController(mainController);
        orderBookComponentController.setMainController(mainController);
    }

    public void refreshUsers() {
        List<UserSummaryDto> summaries = mainController.getMarketManager().getUserSummaries();
        List<UserTableRow> rows = summaries.stream().map(UserTableRow::new).toList();

        users.setAll(rows);
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
        clearEventDetails();
    }

    private void refreshUserEvents(String userName) {
        List<EventParticipationDto> participations =
                mainController.getMarketManager().getUserEvents(userName);

        userEvents.setAll(participations.stream().map(UserEventTableRow::new).toList());
    }

    // Shows the panel that matches the selected event's trading method, and hides the other one.
    private void showEventDetails(UserEventTableRow selectedEvent) {
        UserTableRow selectedUser = usersTable.getSelectionModel().getSelectedItem();

        if (selectedEvent == null || selectedUser == null) {
            clearEventDetails();
            return;
        }
        noEventSelectedLabel.setVisible(false);
        eventDetailsPane.setExpanded(true);
        lmsrTradeComponent.setVisible(!selectedEvent.isOrderBook());
        orderBookComponent.setVisible(selectedEvent.isOrderBook());

        if (selectedEvent.isOrderBook()) {
            orderBookComponentController.refresh(selectedUser.getName(), selectedEvent.getEventId(),
                    selectedEvent.isMarketMaker());
        } else {
            lmsrTradeComponentController.refresh(selectedUser.getName(), selectedEvent.getEventId(),
                    selectedEvent.isMarketMaker());
        }
    }

    private void clearEventDetails() {
        noEventSelectedLabel.setVisible(true);
        lmsrTradeComponent.setVisible(false);
        orderBookComponent.setVisible(false);

        lmsrTradeComponentController.clear();
        orderBookComponentController.clear();
    }
}
