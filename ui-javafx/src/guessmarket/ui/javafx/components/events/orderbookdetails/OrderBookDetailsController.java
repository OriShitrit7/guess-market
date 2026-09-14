package guessmarket.ui.javafx.components.events.orderbookdetails;

import guessmarket.dto.EventStatusDto;
import guessmarket.dto.EventSummaryDto;
import guessmarket.dto.OptionBookDto;
import guessmarket.dto.OrderEntryDto;
import guessmarket.dto.OrderBookStateDto;
import guessmarket.ui.javafx.components.DisplayText;
import guessmarket.ui.javafx.components.StatusStyle;
import guessmarket.ui.javafx.components.events.eventresult.EventResultController;
import guessmarket.ui.javafx.components.events.orderbookdetails.optionbook.OptionBookController;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.stream.Stream;

// Displays order books, price history and results for the selected Order Book event.
public class OrderBookDetailsController {
    private static final String NO_SELECTION = "-";
    private static final String EMPTY_TEXT = "";
    private static final String EMPTY_AMOUNT = "$0.00";
    private static final String COMMISSION_FORMAT = "%d%% %s";
    private static final int FIRST_OPTION_INDEX = 0;
    private static final int SECOND_OPTION_INDEX = 1;
    private static final int OPTION_COUNT = 2;
    private static final double COMPACT_CLOSED_LAYOUT_BREAKPOINT = 780;
    private static final double RESULT_COLUMN_MIN_WIDTH = 360;
    private static final double RESULT_COLUMN_PREF_WIDTH = 400;
    private static final double RESULT_COLUMN_MAX_WIDTH = 460;

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
    private GridPane resultAndPriceHistoryPane;
    @FXML
    private ColumnConstraints resultColumn;
    @FXML
    private ColumnConstraints priceHistoryColumn;
    @FXML
    private VBox closedResultCard;
    @FXML
    private VBox priceHistorySection;
    @FXML
    private OptionBookController firstOptionBookComponentController;
    @FXML
    private OptionBookController secondOptionBookComponentController;
    @FXML
    private LineChart<Number, Number> priceHistoryChart;
    @FXML
    private NumberAxis priceHistoryXAxis;
    @FXML
    private NumberAxis priceHistoryYAxis;

    @FXML
    private VBox eventResultComponent;
    @FXML
    private EventResultController eventResultComponentController;
    @FXML
    private void initialize() {
        closedResultCard.managedProperty().bind(closedResultCard.visibleProperty());
        optionBooksSection.managedProperty().bind(optionBooksSection.visibleProperty());
        resultAndPriceHistoryPane.widthProperty().addListener((observable, oldWidth, newWidth) ->
                updateResultAndChartLayout(newWidth.doubleValue(), closedResultCard.isVisible()));

        clear();
    }

    // Refreshes the event summary, option books and price history from one snapshot.
    public void refresh(OrderBookStateDto state) {
        EventSummaryDto summary = state.getSummary(); // Read the shared event details

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

        showBooks(state); // Show live books or the result for a closed event
        showPriceHistory(summary, state.getPriceHistory());
    }

    // Resets the panel when no Order Book event is selected.
    public void clear() {
        marketMakerLabel.setText(NO_SELECTION);
        eventNameLabel.setText(NO_SELECTION);
        eventStatusLabel.setText(EMPTY_TEXT);
        StatusStyle.clear(eventStatusLabel);
        eventDescriptionLabel.setText(EMPTY_TEXT);
        eventCommissionLabel.setText(EMPTY_TEXT);
        eventAccountLabel.setText(EMPTY_AMOUNT);
        totalCommissionCollectedLabel.setText(EMPTY_AMOUNT);

        optionBooksSection.setVisible(true);
        closedResultCard.setVisible(false);
        updateResultAndChartLayout(resultAndPriceHistoryPane.getWidth(), false);

        firstOptionBookComponentController.clear();
        secondOptionBookComponentController.clear();
        clearPriceHistory();
    }

    // A closed event has empty books, so the result replaces them.
    private void showBooks(OrderBookStateDto state) {
        EventSummaryDto summary = state.getSummary();
        boolean closed = summary.getStatus() == EventStatusDto.CLOSED;

        optionBooksSection.setVisible(!closed);
        closedResultCard.setVisible(closed);
        updateResultAndChartLayout(resultAndPriceHistoryPane.getWidth(), closed);

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

    // Rebuilds one price series per option and updates the chart scale.
    private void showPriceHistory(EventSummaryDto summary, List<List<Double>> history) {
        List<String> optionNames = summary.getOptionNames();

        priceHistoryChart.getData().clear(); // Remove data from the previous event
        for (int optionIndex = 0; optionIndex < optionNames.size(); optionIndex++) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(optionNames.get(optionIndex));

            if (optionIndex < history.size()) {
                List<Double> optionHistory = history.get(optionIndex);

                for (int tradeIndex = 0; tradeIndex < optionHistory.size(); tradeIndex++) {
                    series.getData().add(new XYChart.Data<>(tradeIndex + 1, optionHistory.get(tradeIndex))); // Trades start at one
                }
            }
            priceHistoryChart.getData().add(series);
        }
        configurePriceHistoryAxes(summary.getWinningSharePayout(), history);
    }

    private void clearPriceHistory() {
        priceHistoryChart.getData().clear();
        priceHistoryXAxis.setLowerBound(0);
        priceHistoryXAxis.setUpperBound(1);
        priceHistoryXAxis.setTickUnit(1);
        priceHistoryYAxis.setLowerBound(0);
        priceHistoryYAxis.setUpperBound(1);
        priceHistoryYAxis.setTickUnit(0.25);
    }

    // Scales the trade axis to the longest history and the price axis to the event payout.
    private void configurePriceHistoryAxes(double maximumPrice, List<List<Double>> history) {
        int maximumTrade = history.stream()
                .mapToInt(List::size)
                .max()
                .orElse(0);
        int upperTradeBound = Math.max(1, maximumTrade);

        priceHistoryXAxis.setLowerBound(0);
        priceHistoryXAxis.setUpperBound(upperTradeBound);
        priceHistoryXAxis.setTickUnit(Math.max(1, Math.ceil(upperTradeBound / 6.0)));
        priceHistoryYAxis.setLowerBound(0);
        priceHistoryYAxis.setUpperBound(maximumPrice);
        priceHistoryYAxis.setTickUnit(maximumPrice / 4);
    }

    // Selects the chart and result layout according to panel width and event state.
    private void updateResultAndChartLayout(double width, boolean closed) {
        if (!closed) {
            configureFullWidthChart();
            return;
        }
        if (width < COMPACT_CLOSED_LAYOUT_BREAKPOINT) {
            configureStackedClosedLayout();
        } else {
            configureSideBySideClosedLayout();
        }
    }

    private void configureFullWidthChart() {
        configureEqualColumns();
        GridPane.setConstraints(priceHistorySection, 0, 0, 2, 1);
    }

    private void configureStackedClosedLayout() {
        configureEqualColumns();
        GridPane.setConstraints(closedResultCard, 0, 0, 2, 1);
        GridPane.setConstraints(priceHistorySection, 0, 1, 2, 1);
    }

    private void configureSideBySideClosedLayout() {
        resultColumn.setPercentWidth(-1);
        resultColumn.setMinWidth(RESULT_COLUMN_MIN_WIDTH);
        resultColumn.setPrefWidth(RESULT_COLUMN_PREF_WIDTH);
        resultColumn.setMaxWidth(RESULT_COLUMN_MAX_WIDTH);
        resultColumn.setHgrow(Priority.NEVER);
        priceHistoryColumn.setPercentWidth(-1);
        priceHistoryColumn.setMinWidth(0);
        priceHistoryColumn.setPrefWidth(-1);
        priceHistoryColumn.setMaxWidth(Double.MAX_VALUE);
        priceHistoryColumn.setHgrow(Priority.ALWAYS);
        GridPane.setConstraints(closedResultCard, 0, 0, 1, 1);
        GridPane.setConstraints(priceHistorySection, 1, 0, 1, 1);
    }

    private void configureEqualColumns() {
        resultColumn.setMinWidth(0);
        resultColumn.setPrefWidth(-1);
        resultColumn.setMaxWidth(Double.MAX_VALUE);
        resultColumn.setPercentWidth(50);
        resultColumn.setHgrow(Priority.ALWAYS);
        priceHistoryColumn.setMinWidth(0);
        priceHistoryColumn.setPrefWidth(-1);
        priceHistoryColumn.setMaxWidth(Double.MAX_VALUE);
        priceHistoryColumn.setPercentWidth(50);
        priceHistoryColumn.setHgrow(Priority.ALWAYS);
    }
}
