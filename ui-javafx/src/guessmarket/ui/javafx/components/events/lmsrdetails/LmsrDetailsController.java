package guessmarket.ui.javafx.components.events.lmsrdetails;

import guessmarket.dto.EventStatusDto;
import guessmarket.dto.EventSummaryDto;
import guessmarket.dto.EventTradingStateDto;
import guessmarket.dto.OptionStateDto;
import guessmarket.ui.javafx.components.DisplayText;
import guessmarket.ui.javafx.components.events.eventholdings.EventHoldingsController;
import guessmarket.ui.javafx.components.events.eventresult.EventResultController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.util.List;

public class LmsrDetailsController {
    private static final String NO_SELECTION = "-";
    private static final String EMPTY_TEXT = "";
    private static final String EMPTY_AMOUNT = "$0.00";
    private static final String NO_VALUE = "-";
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
    private Label firstOptionNameLabel;
    @FXML
    private Label firstOptionValueLabel;
    @FXML
    private Label firstOptionSharesLabel;
    @FXML
    private Label secondOptionNameLabel;
    @FXML
    private Label secondOptionValueLabel;
    @FXML
    private Label secondOptionSharesLabel;

    @FXML
    private TableView<EventTradeTableRow> tradeHistoryTable;
    @FXML
    private TableColumn<EventTradeTableRow, String> tradeUserColumn;
    @FXML
    private TableColumn<EventTradeTableRow, String> tradeOptionColumn;
    @FXML
    private TableColumn<EventTradeTableRow, Number> tradeQuantityColumn;
    @FXML
    private TableColumn<EventTradeTableRow, Number> tradePaidColumn;
    @FXML
    private TableColumn<EventTradeTableRow, Number> tradeCommissionColumn;

    @FXML
    private VBox eventResultComponent;
    @FXML
    private EventResultController eventResultComponentController;
    @FXML
    private EventHoldingsController eventHoldingsComponentController;

    private final ObservableList<EventTradeTableRow> trades = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        eventResultComponent.managedProperty().bind(eventResultComponent.visibleProperty());

        initTradeHistoryTable();
        clear();
    }

    public void refresh(EventTradingStateDto state) {
        EventSummaryDto summary = state.getSummary();

        showEventSummary(summary);
        showOptions(state.getOptions());
        showResult(summary, state.getOptions());

        trades.setAll(state.getTradeHistory().stream().map(EventTradeTableRow::new).toList());
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

        clearOptions();
        eventResultComponent.setVisible(false);

        trades.clear();
        eventHoldingsComponentController.clear();
    }

    private void initTradeHistoryTable() {
        tradeHistoryTable.setPlaceholder(new Label("No trades yet"));

        tradeUserColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        tradeOptionColumn.setCellValueFactory(new PropertyValueFactory<>("optionName"));
        tradeQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        tradePaidColumn.setCellValueFactory(new PropertyValueFactory<>("paid"));
        tradeCommissionColumn.setCellValueFactory(new PropertyValueFactory<>("commission"));
        tradePaidColumn.setCellFactory(column -> amountCell());
        tradeCommissionColumn.setCellFactory(column -> amountCell());

        tradeHistoryTable.setItems(trades);
    }

    private void showEventSummary(EventSummaryDto summary) {
        marketMakerLabel.setText(summary.getMarketMakerName());
        eventNameLabel.setText(summary.getEventName());
        eventStatusLabel.setText(DisplayText.of(summary.getStatus()));
        eventDescriptionLabel.setText(summary.getDescription());
        eventCommissionLabel.setText(String.format(COMMISSION_FORMAT, summary.getCommissionPercent(),
                DisplayText.of(summary.getCommissionPolicy())));
        eventAccountLabel.setText(DisplayText.amount(summary.getAccountBalance()));
        totalCommissionCollectedLabel.setText(
                DisplayText.amount(summary.getTotalCommissionCollected()));
    }

    private void showOptions(List<OptionStateDto> options) {
        if (options.size() < OPTION_COUNT) {
            clearOptions();
            return;
        }
        showOption(options.get(FIRST_OPTION_INDEX), firstOptionNameLabel, firstOptionValueLabel,
                firstOptionSharesLabel);
        showOption(options.get(SECOND_OPTION_INDEX), secondOptionNameLabel, secondOptionValueLabel,
                secondOptionSharesLabel);
    }

    private void showOption(OptionStateDto option, Label nameLabel, Label valueLabel, Label sharesLabel) {
        nameLabel.setText(option.getOptionName());
        valueLabel.setText(DisplayText.optionalAmount(option.getCurrentValue()));
        sharesLabel.setText(String.valueOf(option.getTotalSharesBought()));
    }

    private void clearOptions() {
        firstOptionNameLabel.setText(EMPTY_TEXT);
        firstOptionValueLabel.setText(NO_VALUE);
        firstOptionSharesLabel.setText("0");
        secondOptionNameLabel.setText(EMPTY_TEXT);
        secondOptionValueLabel.setText(NO_VALUE);
        secondOptionSharesLabel.setText("0");
    }

    private void showResult(EventSummaryDto summary, List<OptionStateDto> options) {
        boolean closed = summary.getStatus() == EventStatusDto.CLOSED;

        eventResultComponent.setVisible(closed);

        if (closed) {
            eventResultComponentController.refresh(summary.getWinningOptionName(),
                    summary.getWinningSharePayout(), options);
        }
    }

    private <T> TableCell<T, Number> amountCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Number value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : DisplayText.amount(value.doubleValue()));
            }
        };
    }
}
