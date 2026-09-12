package guessmarket.ui.javafx.components.events.eventholdings;

import guessmarket.dto.ParticipantHoldingsDto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class EventHoldingsController {
    private static final String QUANTITY_HEADER = "%s Qty";
    private static final String VALUE_HEADER = "%s Value";
    private static final int FIRST_OPTION_INDEX = 0;
    private static final int SECOND_OPTION_INDEX = 1;
    private static final int OPTION_COUNT = 2;

    @FXML
    private TableView<ParticipantTableRow> holdingsTable;
    @FXML
    private TableColumn<ParticipantTableRow, String> userColumn;
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

    @FXML
    private void initialize() {
        holdingsTable.setPlaceholder(new Label("No one has taken part in this event yet"));

        userColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        firstOptionQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("firstOptionQuantity"));
        firstOptionValueColumn.setCellValueFactory(new PropertyValueFactory<>("firstOptionValue"));
        secondOptionQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("secondOptionQuantity"));
        secondOptionValueColumn.setCellValueFactory(new PropertyValueFactory<>("secondOptionValue"));
        openOrdersColumn.setCellValueFactory(new PropertyValueFactory<>("openOrders"));

        holdingsTable.setItems(participants);
    }

    // The column headers carry the option names, which are only known once a file is loaded.
    public void refresh(List<String> optionNames, List<ParticipantHoldingsDto> participantDtos) {
        if (optionNames.size() >= OPTION_COUNT) {
            showHeaders(optionNames);
        }
        participants.setAll(participantDtos.stream().map(ParticipantTableRow::new).toList());
    }

    public void clear() {
        participants.clear();
    }

    private void showHeaders(List<String> optionNames) {
        String firstOption = optionNames.get(FIRST_OPTION_INDEX);
        String secondOption = optionNames.get(SECOND_OPTION_INDEX);

        firstOptionQuantityColumn.setText(String.format(QUANTITY_HEADER, firstOption));
        firstOptionValueColumn.setText(String.format(VALUE_HEADER, firstOption));
        secondOptionQuantityColumn.setText(String.format(QUANTITY_HEADER, secondOption));
        secondOptionValueColumn.setText(String.format(VALUE_HEADER, secondOption));
    }
}
