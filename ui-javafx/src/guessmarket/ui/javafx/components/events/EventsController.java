package guessmarket.ui.javafx.components.events;

import guessmarket.dto.EventSummaryDto;
import guessmarket.ui.javafx.components.events.lmsrdetails.LmsrDetailsController;
import guessmarket.ui.javafx.components.events.orderbookdetails.OrderBookDetailsController;
import guessmarket.ui.javafx.components.main.MainController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.controlsfx.control.SegmentedButton;

import java.util.List;

public class EventsController {
    private static final String PERCENT_SIGN = "%";

    private MainController mainController;

    @FXML
    private SegmentedButton methodFilter;
    @FXML
    private SegmentedButton statusFilter;
    @FXML
    private SegmentedButton commissionFilter;

    @FXML
    private TableView<EventTableRow> eventsTable;

    @FXML
    private TableColumn<EventTableRow, String> eventNameColumn;
    @FXML
    private TableColumn<EventTableRow, String> eventStatusColumn;
    @FXML
    private TableColumn<EventTableRow, String> eventMethodColumn;
    @FXML
    private TableColumn<EventTableRow, Number> eventCommissionColumn;
    @FXML
    private TableColumn<EventTableRow, String> eventCommissionTypeColumn;
    @FXML
    private TableColumn<EventTableRow, Number> eventBalanceColumn;

    @FXML
    private Label noEventSelectedLabel;
    @FXML
    private VBox lmsrDetailsComponent;
    @FXML
    private LmsrDetailsController lmsrDetailsComponentController;
    @FXML
    private VBox orderBookDetailsComponent;
    @FXML
    private OrderBookDetailsController orderBookDetailsComponentController;

    private final ObservableList<EventTableRow> events = FXCollections.observableArrayList();
    private final FilteredList<EventTableRow> filteredEvents = new FilteredList<>(events, row -> true);

    @FXML
    private void initialize() {
        eventsTable.setPlaceholder(new Label("Load a file to view events"));
        noEventSelectedLabel.managedProperty().bind(noEventSelectedLabel.visibleProperty());
        lmsrDetailsComponent.managedProperty().bind(lmsrDetailsComponent.visibleProperty());
        orderBookDetailsComponent.managedProperty().bind(orderBookDetailsComponent.visibleProperty());

        clearFilterToggles();
        initEventsTable();
        initFilterListeners();
        initSelectionListener();
        clearEventDetails();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void refreshEvents() {
        List<EventSummaryDto> summaries = mainController.getMarketManager().getEventSummaries();
        List<EventTableRow> rows = summaries.stream().map(EventTableRow::new).toList();

        events.setAll(rows);
    }

    private void initSelectionListener() {
        eventsTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showEventDetails(newValue));
    }

    // Shows the panel that matches the selected event's trading method, and hides the other one.
    private void showEventDetails(EventTableRow selectedEvent) {
        if (selectedEvent == null) {
            clearEventDetails();
            return;
        }
        noEventSelectedLabel.setVisible(false);
        lmsrDetailsComponent.setVisible(!selectedEvent.isOrderBook());
        orderBookDetailsComponent.setVisible(selectedEvent.isOrderBook());

        if (selectedEvent.isOrderBook()) {
            orderBookDetailsComponentController.refresh(
                    mainController.getMarketManager().getOrderBookState(selectedEvent.getEventId()));
            return;
        }
        lmsrDetailsComponentController.refresh(
                mainController.getMarketManager().getEventTradingState(selectedEvent.getEventId()));
    }

    private void clearEventDetails() {
        noEventSelectedLabel.setVisible(true);
        lmsrDetailsComponent.setVisible(false);
        orderBookDetailsComponent.setVisible(false);

        lmsrDetailsComponentController.clear();
        orderBookDetailsComponentController.clear();
    }

    private void clearFilterToggles() {
        methodFilter.setToggleGroup(null);
        statusFilter.setToggleGroup(null);
        commissionFilter.setToggleGroup(null);
    }

    private void initEventsTable() {
        eventNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        eventStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        eventMethodColumn.setCellValueFactory(new PropertyValueFactory<>("methodType"));
        eventCommissionColumn.setCellValueFactory(new PropertyValueFactory<>("commissionPercent"));
        eventCommissionColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Number value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : value + PERCENT_SIGN);
            }
        });
        eventCommissionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("commissionPolicy"));
        eventBalanceColumn.setCellValueFactory(new PropertyValueFactory<>("accountBalance"));

        eventBalanceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Number value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : String.format("%.2f", value.doubleValue()));
            }
        });

        eventsTable.setItems(filteredEvents);
    }
    
    private void initFilterListeners() {
        List.of(methodFilter, statusFilter, commissionFilter)
                .forEach(filter -> filter.getButtons().forEach(button ->
                        button.selectedProperty().addListener(
                                (observable, oldValue, newValue) -> updateFilter()
                        )
                ));
    }

    private void updateFilter() {
        filteredEvents.setPredicate(row -> matchesFilter(methodFilter, row.getMethodType())
                                    && matchesFilter(statusFilter, row.getStatus())
                                    && matchesFilter(commissionFilter, row.getCommissionPolicy()));
    }

    private boolean matchesFilter(SegmentedButton filter, String rowValue) {
        boolean noFilterSelected = filter.getButtons().stream().noneMatch(ToggleButton::isSelected);
        boolean filterSelected = filter.getButtons().stream()
                .filter(ToggleButton::isSelected)
                .anyMatch(button -> button.getText().equals(rowValue));
        return noFilterSelected || filterSelected;
    }
}
