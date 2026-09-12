package guessmarket.ui.javafx.components.events.orderbookdetails;

import guessmarket.dto.EventStatusDto;
import guessmarket.dto.EventSummaryDto;
import guessmarket.dto.OptionBookDto;
import guessmarket.dto.OrderEntryDto;
import guessmarket.dto.OrderBookStateDto;
import guessmarket.ui.javafx.components.DisplayText;
import guessmarket.ui.javafx.components.events.eventholdings.EventHoldingsController;
import guessmarket.ui.javafx.components.events.eventresult.EventResultController;
import guessmarket.ui.javafx.components.events.orderbookdetails.optionbook.OptionBookController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.stream.Stream;

public class OrderBookDetailsController {
    private static final String NO_SELECTION = "-";
    private static final String EMPTY_TEXT = "";
    private static final String EMPTY_AMOUNT = "$0.00";
    private static final String COMMISSION_FORMAT = "%d%% %s";
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
    private Label eventDescriptionLabel;
    @FXML
    private Label eventCommissionLabel;
    @FXML
    private Label eventAccountLabel;
    @FXML
    private Label totalCommissionCollectedLabel;

    @FXML
    private VBox optionBooksSection;
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
        optionBooksSection.managedProperty().bind(optionBooksSection.visibleProperty());

        clear();
    }

    public void refresh(OrderBookStateDto state) {
        EventSummaryDto summary = state.getSummary();

        marketMakerLabel.setText(summary.getMarketMakerName());
        eventNameLabel.setText(summary.getEventName());
        eventStatusLabel.setText(DisplayText.of(summary.getStatus()));
        eventDescriptionLabel.setText(summary.getDescription());
        eventCommissionLabel.setText(String.format(COMMISSION_FORMAT, summary.getCommissionPercent(),
                DisplayText.of(summary.getCommissionPolicy())));
        eventAccountLabel.setText(DisplayText.amount(summary.getAccountBalance()));
        totalCommissionCollectedLabel.setText(
                DisplayText.amount(summary.getTotalCommissionCollected()));

        showBooks(state);
        eventHoldingsComponentController.refresh(summary.getOptionNames(), state.getParticipants());
    }

    public void clear() {
        marketMakerLabel.setText(NO_SELECTION);
        eventNameLabel.setText(NO_SELECTION);
        eventStatusLabel.setText(EMPTY_TEXT);
        eventDescriptionLabel.setText(EMPTY_TEXT);
        eventCommissionLabel.setText(EMPTY_TEXT);
        eventAccountLabel.setText(EMPTY_AMOUNT);
        totalCommissionCollectedLabel.setText(EMPTY_AMOUNT);

        optionBooksSection.setVisible(true);
        eventResultComponent.setVisible(false);

        firstOptionBookComponentController.clear();
        secondOptionBookComponentController.clear();
        eventHoldingsComponentController.clear();
    }

    // A closed event has empty books, so the result replaces them.
    private void showBooks(OrderBookStateDto state) {
        EventSummaryDto summary = state.getSummary();
        boolean closed = summary.getStatus() == EventStatusDto.CLOSED;

        optionBooksSection.setVisible(!closed);
        eventResultComponent.setVisible(closed);

        if (closed) {
            eventResultComponentController.refresh(summary.getWinningOptionName(),
                    summary.getWinningSharePayout(), state.getOptionStates());
            return;
        }
        if (state.getOptionBooks().size() >= OPTION_COUNT) {
            int maximumOrderQuantity = maximumOrderQuantity(state.getOptionBooks());

            firstOptionBookComponentController.refresh(state.getOptionBooks().get(FIRST_OPTION_INDEX),
                    maximumOrderQuantity, summary.getMarketMakerName());
            secondOptionBookComponentController.refresh(state.getOptionBooks().get(SECOND_OPTION_INDEX),
                    maximumOrderQuantity, summary.getMarketMakerName());
        }
    }

    // Uses one scale across both option books, matching the depth bars in the reference simulation.
    private int maximumOrderQuantity(List<OptionBookDto> books) {
        return books.stream()
                .flatMap(book -> Stream.concat(book.getBuyOrders().stream(), book.getSellOrders().stream()))
                .mapToInt(OrderEntryDto::getQuantity)
                .max()
                .orElse(1);
    }
}
