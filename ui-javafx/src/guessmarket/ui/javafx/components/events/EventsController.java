package guessmarket.ui.javafx.components.events;

import guessmarket.dto.EventSummaryDto;
import guessmarket.ui.javafx.components.main.MainController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.controlsfx.control.SegmentedButton;

import java.util.List;

public class EventsController {
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

    private final ObservableList<EventTableRow> events = FXCollections.observableArrayList();
    private final FilteredList<EventTableRow> filteredEvents = new FilteredList<>(events, row -> true);

    @FXML
    private void initialize() {
        eventsTable.setPlaceholder(new Label("Load a file to view events"));
        clearFilterToggles();
        initEventsTable();
        initFilterListeners();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void refreshEvents() {
        List<EventSummaryDto> summaries = mainController.getMarketManager().getEventSummaries();
        List<EventTableRow> rows = summaries.stream().map(EventTableRow::new).toList();

        events.setAll(rows);
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
