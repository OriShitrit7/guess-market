package guessmarket.ui.javafx.components.events.lmsrdetails;

import guessmarket.dto.EventStatusDto;
import guessmarket.dto.EventSummaryDto;
import guessmarket.dto.EventTradingStateDto;
import guessmarket.dto.OptionStateDto;
import guessmarket.ui.javafx.components.DisplayText;
import guessmarket.ui.javafx.components.StatusStyle;
import guessmarket.ui.javafx.components.events.eventholdings.EventHoldingsController;
import guessmarket.ui.javafx.components.events.eventresult.EventResultController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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
    private static final double COMPACT_OPTIONS_BREAKPOINT = 520;
    private static final double COMPACT_CLOSED_LAYOUT_BREAKPOINT = 780;
    private static final double OPTION_COLUMN_MIN_WIDTH = 170;
    private static final double OPTION_COLUMN_PREF_WIDTH = 185;
    private static final double OPTION_COLUMN_MAX_WIDTH = 195;
    private static final double RESULT_COLUMN_MIN_WIDTH = 360;
    private static final double RESULT_COLUMN_PREF_WIDTH = 400;
    private static final double RESULT_COLUMN_MAX_WIDTH = 460;
    private static final double WIDE_OPTIONS_SECTION_HEIGHT = 260;

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
    private GridPane optionsAndPriceChartPane;
    @FXML
    private ColumnConstraints optionCardsColumn;
    @FXML
    private ColumnConstraints priceChartColumn;
    @FXML
    private VBox firstOptionCard;
    @FXML
    private VBox secondOptionCard;
    @FXML
    private VBox closedResultCard;
    @FXML
    private VBox priceHistoryCard;
    @FXML
    private LineChart<Number, Number> priceHistoryChart;
    @FXML
    private NumberAxis priceHistoryXAxis;

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
    private TableColumn<EventTradeTableRow, Number> tradeTotalPaidColumn;

    @FXML
    private VBox eventResultComponent;
    @FXML
    private EventResultController eventResultComponentController;
    @FXML
    private EventHoldingsController eventHoldingsComponentController;

    private final ObservableList<EventTradeTableRow> trades = FXCollections.observableArrayList();
    private boolean closedEvent;

    @FXML
    private void initialize() {
        firstOptionCard.managedProperty().bind(firstOptionCard.visibleProperty());
        secondOptionCard.managedProperty().bind(secondOptionCard.visibleProperty());
        closedResultCard.managedProperty().bind(closedResultCard.visibleProperty());
        optionsAndPriceChartPane.widthProperty().addListener((observable, oldValue, newValue) ->
                updateOptionsLayout(newValue.doubleValue()));

        initTradeHistoryTable();
        updateOptionsLayout(0);
        clear();
    }

    public void refresh(EventTradingStateDto state) {
        EventSummaryDto summary = state.getSummary();

        showEventSummary(summary);
        showOptions(state.getOptions());
        showPriceHistory(summary.getOptionNames(), state.getSharePriceHistory());
        showResult(summary, state.getOptions());

        trades.setAll(state.getTradeHistory().stream().map(EventTradeTableRow::new).toList());
        eventHoldingsComponentController.refresh(summary.getOptionNames(), state.getParticipants());
    }

    public void clear() {
        marketMakerLabel.setText(NO_SELECTION);
        eventNameLabel.setText(NO_SELECTION);
        eventStatusLabel.setText(EMPTY_TEXT);
        StatusStyle.clear(eventStatusLabel);
        eventDescriptionLabel.setText(EMPTY_TEXT);
        eventCommissionLabel.setText(EMPTY_TEXT);
        eventAccountLabel.setText(EMPTY_AMOUNT);
        totalCommissionCollectedLabel.setText(EMPTY_AMOUNT);

        clearOptions();
        clearPriceHistory();
        closedEvent = false;
        firstOptionCard.setVisible(true);
        secondOptionCard.setVisible(true);
        closedResultCard.setVisible(false);
        updateOptionsLayout(optionsAndPriceChartPane.getWidth());

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
        tradeTotalPaidColumn.setCellValueFactory(new PropertyValueFactory<>("totalPaid"));
        tradePaidColumn.setCellFactory(column -> amountCell());
        tradeCommissionColumn.setCellFactory(column -> amountCell());
        tradeTotalPaidColumn.setCellFactory(column -> amountCell());

        tradeHistoryTable.setItems(trades);
    }

    private void showEventSummary(EventSummaryDto summary) {
        marketMakerLabel.setText(summary.getMarketMakerName());
        eventNameLabel.setText(summary.getEventName());
        eventStatusLabel.setText(DisplayText.of(summary.getStatus()));
        StatusStyle.apply(eventStatusLabel, summary.getStatus());
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

    private void showPriceHistory(List<String> optionNames, List<List<Double>> history) {
        priceHistoryChart.getData().clear();

        for (int optionIndex = 0; optionIndex < optionNames.size(); optionIndex++) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(optionNames.get(optionIndex));

            if (optionIndex < history.size()) {
                List<Double> optionHistory = history.get(optionIndex);

                for (int tradeNumber = 0; tradeNumber < optionHistory.size(); tradeNumber++) {
                    series.getData().add(new XYChart.Data<>(tradeNumber, optionHistory.get(tradeNumber)));
                }
            }
            priceHistoryChart.getData().add(series);
        }
        configurePriceHistoryXAxis(history);
    }

    private void clearPriceHistory() {
        priceHistoryChart.getData().clear();
        priceHistoryXAxis.setLowerBound(0);
        priceHistoryXAxis.setUpperBound(1);
        priceHistoryXAxis.setTickUnit(1);
    }

    private void configurePriceHistoryXAxis(List<List<Double>> history) {
        int maximumTrade = history.stream()
                .mapToInt(optionHistory -> optionHistory.size() - 1)
                .max()
                .orElse(0);
        int upperBound = Math.max(1, maximumTrade);

        priceHistoryXAxis.setLowerBound(0);
        priceHistoryXAxis.setUpperBound(upperBound);
        priceHistoryXAxis.setTickUnit(Math.max(1, Math.ceil(upperBound / 6.0)));
    }

    private void updateOptionsLayout(double width) {
        if (closedEvent) {
            if (width < COMPACT_CLOSED_LAYOUT_BREAKPOINT) {
                showCompactClosedLayout();
            } else {
                showWideClosedLayout();
            }
            return;
        }
        if (width < COMPACT_OPTIONS_BREAKPOINT) {
            showCompactOptionsLayout();
        } else {
            showWideOptionsLayout();
        }
    }

    private void showWideOptionsLayout() {
        configureWideColumns();
        optionsAndPriceChartPane.setPrefHeight(WIDE_OPTIONS_SECTION_HEIGHT);
        optionsAndPriceChartPane.setMaxHeight(WIDE_OPTIONS_SECTION_HEIGHT);
        GridPane.setConstraints(firstOptionCard, 0, 0, 1, 1);
        GridPane.setConstraints(secondOptionCard, 0, 1, 1, 1);
        GridPane.setConstraints(priceHistoryCard, 1, 0, 1, 2);
    }

    private void showCompactOptionsLayout() {
        configureEqualColumns();
        optionsAndPriceChartPane.setPrefHeight(Region.USE_COMPUTED_SIZE);
        optionsAndPriceChartPane.setMaxHeight(Double.MAX_VALUE);
        GridPane.setConstraints(firstOptionCard, 0, 0, 1, 1);
        GridPane.setConstraints(secondOptionCard, 1, 0, 1, 1);
        GridPane.setConstraints(priceHistoryCard, 0, 1, 2, 1);
    }

    private void showWideClosedLayout() {
        configureClosedColumns();
        optionsAndPriceChartPane.setPrefHeight(WIDE_OPTIONS_SECTION_HEIGHT);
        optionsAndPriceChartPane.setMaxHeight(WIDE_OPTIONS_SECTION_HEIGHT);
        GridPane.setConstraints(closedResultCard, 0, 0, 1, 2);
        GridPane.setConstraints(priceHistoryCard, 1, 0, 1, 2);
    }

    private void showCompactClosedLayout() {
        configureEqualColumns();
        optionsAndPriceChartPane.setPrefHeight(Region.USE_COMPUTED_SIZE);
        optionsAndPriceChartPane.setMaxHeight(Double.MAX_VALUE);
        GridPane.setConstraints(closedResultCard, 0, 0, 2, 1);
        GridPane.setConstraints(priceHistoryCard, 0, 1, 2, 1);
    }

    private void configureWideColumns() {
        optionCardsColumn.setPercentWidth(Region.USE_COMPUTED_SIZE);
        optionCardsColumn.setMinWidth(OPTION_COLUMN_MIN_WIDTH);
        optionCardsColumn.setPrefWidth(OPTION_COLUMN_PREF_WIDTH);
        optionCardsColumn.setMaxWidth(OPTION_COLUMN_MAX_WIDTH);
        optionCardsColumn.setHgrow(Priority.NEVER);

        priceChartColumn.setPercentWidth(Region.USE_COMPUTED_SIZE);
        priceChartColumn.setMinWidth(0);
        priceChartColumn.setPrefWidth(Region.USE_COMPUTED_SIZE);
        priceChartColumn.setMaxWidth(Double.MAX_VALUE);
        priceChartColumn.setHgrow(Priority.ALWAYS);
    }

    private void configureEqualColumns() {
        configureEqualColumn(optionCardsColumn);
        configureEqualColumn(priceChartColumn);
    }

    private void configureClosedColumns() {
        optionCardsColumn.setPercentWidth(Region.USE_COMPUTED_SIZE);
        optionCardsColumn.setMinWidth(RESULT_COLUMN_MIN_WIDTH);
        optionCardsColumn.setPrefWidth(RESULT_COLUMN_PREF_WIDTH);
        optionCardsColumn.setMaxWidth(RESULT_COLUMN_MAX_WIDTH);
        optionCardsColumn.setHgrow(Priority.NEVER);

        priceChartColumn.setPercentWidth(Region.USE_COMPUTED_SIZE);
        priceChartColumn.setMinWidth(0);
        priceChartColumn.setPrefWidth(Region.USE_COMPUTED_SIZE);
        priceChartColumn.setMaxWidth(Double.MAX_VALUE);
        priceChartColumn.setHgrow(Priority.ALWAYS);
    }

    private void configureEqualColumn(ColumnConstraints column) {
        column.setPercentWidth(50);
        column.setMinWidth(0);
        column.setPrefWidth(Region.USE_COMPUTED_SIZE);
        column.setMaxWidth(Double.MAX_VALUE);
        column.setHgrow(Priority.ALWAYS);
    }

    private void showResult(EventSummaryDto summary, List<OptionStateDto> options) {
        boolean closed = summary.getStatus() == EventStatusDto.CLOSED;

        closedEvent = closed;
        firstOptionCard.setVisible(!closed);
        secondOptionCard.setVisible(!closed);
        closedResultCard.setVisible(closed);
        updateOptionsLayout(optionsAndPriceChartPane.getWidth());

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
