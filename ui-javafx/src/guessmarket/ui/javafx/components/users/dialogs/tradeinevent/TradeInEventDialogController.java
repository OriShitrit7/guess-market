package guessmarket.ui.javafx.components.users.dialogs.tradeinevent;

import guessmarket.dto.EventSummaryDto;
import guessmarket.ui.javafx.components.DisplayText;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

// Lets a user select an available event before opening its trading panel.
public class TradeInEventDialogController {
    @FXML
    private DialogPane dialogPane;
    @FXML
    private TableView<EventSummaryDto> eventsTable;
    @FXML
    private TableColumn<EventSummaryDto, String> eventNameColumn;
    @FXML
    private TableColumn<EventSummaryDto, String> eventMethodColumn;
    @FXML
    private ButtonType continueButtonType;

    private final ObservableList<EventSummaryDto> events = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        eventNameColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getEventName()));
        eventMethodColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(DisplayText.of(cell.getValue().getMethodType())));
        eventsTable.setItems(events);

        dialogPane.lookupButton(continueButtonType).disableProperty()
                .bind(eventsTable.getSelectionModel().selectedItemProperty().isNull()); // Require an event selection
    }

    // Replaces the table contents with events available to the selected user.
    public void setEvents(List<EventSummaryDto> availableEvents) {
        events.setAll(availableEvents);
    }

    public EventSummaryDto getSelectedEvent() {
        return eventsTable.getSelectionModel().getSelectedItem();
    }
}
