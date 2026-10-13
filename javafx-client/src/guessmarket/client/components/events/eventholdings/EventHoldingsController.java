package guessmarket.client.components.events.eventholdings;

import guessmarket.dto.ParticipantHoldingsDto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

// Displays each participant's holdings and pending orders for the selected event.
public class EventHoldingsController {
    private static final String NO_EVENT_SELECTED = "Select an event to view its holdings";
    private static final String NO_PARTICIPANTS = "No one has taken part in this event yet";
    private static final int FIRST_OPTION_INDEX = 0;
    private static final int SECOND_OPTION_INDEX = 1;
    private static final int OPTION_COUNT = 2;
    private static final String NO_HEADER = "";
    private static final int MAX_VISIBLE_ROWS = 4;
    private static final double ROW_HEIGHT = 44.0;
    private static final double HEADER_HEIGHT = 58.0;
    private static final double TABLE_BORDER_HEIGHT = 2.0;
    private static final double EMPTY_TABLE_HEIGHT = 150.0;

    @FXML
    private TableView<ParticipantTableRow> holdingsTable;
    @FXML
    private TableColumn<ParticipantTableRow, String> userColumn;
    @FXML
    private TableColumn<ParticipantTableRow, String> firstOptionColumn;
    @FXML
    private TableColumn<ParticipantTableRow, String> secondOptionColumn;
    @FXML
    private TableColumn<ParticipantTableRow, Number> firstOptionQuantityColumn;
    @FXML
    private TableColumn<ParticipantTableRow, String> firstOptionValueColumn;
    @FXML
    private TableColumn<ParticipantTableRow, Number> secondOptionQuantityColumn;
    @FXML
    private TableColumn<ParticipantTableRow, String> secondOptionValueColumn;
    @FXML
    private TableColumn<ParticipantTableRow, Number> openOrdersColumn;

    private final ObservableList<ParticipantTableRow> participants = FXCollections.observableArrayList();
    private final Label placeholder = new Label(NO_EVENT_SELECTED);

    @FXML
    private void initialize() {
        holdingsTable.setPlaceholder(placeholder);

        userColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        firstOptionQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("firstOptionQuantity"));
        firstOptionValueColumn.setCellValueFactory(new PropertyValueFactory<>("firstOptionValue"));
        secondOptionQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("secondOptionQuantity"));
        secondOptionValueColumn.setCellValueFactory(new PropertyValueFactory<>("secondOptionValue"));
        openOrdersColumn.setCellValueFactory(new PropertyValueFactory<>("openOrders"));

        holdingsTable.setFixedCellSize(ROW_HEIGHT);
        holdingsTable.setItems(participants);
        clearHeaders();
        updateTableHeight();
    }

    // The column headers carry the option names, which are only known once a file is loaded.
    public void refresh(List<String> optionNames, List<ParticipantHoldingsDto> participantDtos, boolean orderBook) {
        placeholder.setText(NO_PARTICIPANTS);
        openOrdersColumn.setVisible(orderBook); // Only Order Book events have waiting orders
        if (optionNames.size() >= OPTION_COUNT) {
            showHeaders(optionNames);
        }
        participants.setAll(participantDtos.stream().map(ParticipantTableRow::new).toList());
        updateTableHeight();
    }

    // Clears participant data and option headers when no event is selected.
    public void clear() {
        participants.clear();
        placeholder.setText(NO_EVENT_SELECTED);
        clearHeaders();
        updateTableHeight();
    }

    // Without an event there are no option names, so the placeholder names must not stay on screen.
    private void clearHeaders() {
        firstOptionColumn.setText(NO_HEADER);
        secondOptionColumn.setText(NO_HEADER);
    }

    private void showHeaders(List<String> optionNames) {
        firstOptionColumn.setText(optionNames.get(FIRST_OPTION_INDEX));
        secondOptionColumn.setText(optionNames.get(SECOND_OPTION_INDEX));
    }

    // Shows only the rows that contain data and lets the table scroll after four participants.
    private void updateTableHeight() {
        double tableHeight = EMPTY_TABLE_HEIGHT;

        if (!participants.isEmpty()) {
            int visibleRows = Math.min(MAX_VISIBLE_ROWS, participants.size());
            tableHeight = HEADER_HEIGHT + visibleRows * ROW_HEIGHT + TABLE_BORDER_HEIGHT;
        }

        holdingsTable.setMinHeight(tableHeight);
        holdingsTable.setPrefHeight(tableHeight);
        holdingsTable.setMaxHeight(tableHeight);
    }
}
