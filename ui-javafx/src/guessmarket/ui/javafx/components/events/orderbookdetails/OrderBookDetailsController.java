package guessmarket.ui.javafx.components.events.orderbookdetails;

import guessmarket.dto.EventStatusDto;
import guessmarket.dto.EventSummaryDto;
import guessmarket.dto.OrderBookStateDto;
import guessmarket.ui.javafx.components.DisplayText;
import guessmarket.ui.javafx.components.events.eventholdings.EventHoldingsController;
import guessmarket.ui.javafx.components.events.eventresult.EventResultController;
import guessmarket.ui.javafx.components.events.orderbookdetails.optionbook.OptionBookController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class OrderBookDetailsController {
    private static final String NO_SELECTION = "-";
    private static final String EMPTY_TEXT = "";
    private static final int FIRST_OPTION_INDEX = 0;
    private static final int SECOND_OPTION_INDEX = 1;
    private static final int OPTION_COUNT = 2;

    @FXML
    private Label marketMakerLabel;
    @FXML
    private Label eventNameLabel;
    @FXML
    private Label eventStatusLabel;

    @FXML
    private HBox optionBooksRow;
    @FXML
    private OptionBookController firstOptionBookComponentController;
    @FXML
    private OptionBookController secondOptionBookComponentController;

    @FXML
    private VBox eventResultComponent;
    @FXML
    private EventResultController eventResultComponentController;
    @FXML
    private EventHoldingsController eventHoldingsComponentController;

    @FXML
    private void initialize() {
        eventResultComponent.managedProperty().bind(eventResultComponent.visibleProperty());
        optionBooksRow.managedProperty().bind(optionBooksRow.visibleProperty());

        clear();
    }

    public void refresh(OrderBookStateDto state) {
        EventSummaryDto summary = state.getSummary();

        marketMakerLabel.setText(summary.getMarketMakerName());
        eventNameLabel.setText(summary.getEventName());
        eventStatusLabel.setText(DisplayText.of(summary.getStatus()));

        showBooks(state);
        eventHoldingsComponentController.refresh(summary.getOptionNames(), state.getParticipants());
    }

    public void clear() {
        marketMakerLabel.setText(NO_SELECTION);
        eventNameLabel.setText(NO_SELECTION);
        eventStatusLabel.setText(EMPTY_TEXT);

        optionBooksRow.setVisible(true);
        eventResultComponent.setVisible(false);

        firstOptionBookComponentController.clear();
        secondOptionBookComponentController.clear();
        eventHoldingsComponentController.clear();
    }

    // A closed event has empty books, so the result replaces them.
    private void showBooks(OrderBookStateDto state) {
        EventSummaryDto summary = state.getSummary();
        boolean closed = summary.getStatus() == EventStatusDto.CLOSED;

        optionBooksRow.setVisible(!closed);
        eventResultComponent.setVisible(closed);

        if (closed) {
            eventResultComponentController.refresh(summary.getWinningOptionName(),
                    summary.getWinningSharePayout(), state.getOptionStates());
            return;
        }
        if (state.getOptionBooks().size() >= OPTION_COUNT) {
            firstOptionBookComponentController.refresh(state.getOptionBooks().get(FIRST_OPTION_INDEX));
            secondOptionBookComponentController.refresh(state.getOptionBooks().get(SECOND_OPTION_INDEX));
        }
    }
}
