package guessmarket.client.components.events.orderbookdetails.optionbook;

import guessmarket.dto.OptionBookDto;
import guessmarket.dto.OrderEntryDto;
import guessmarket.client.components.DisplayText;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

// Displays market prices and the buy and sell depth for one event option.
public class OptionBookController {
    private static final String NO_PRICE = "-";
    private static final String NO_ORDERS = "\u2014";
    private static final String MARKET_MAKER_TAG = "MM";
    private static final double MINIMUM_BAR_RATIO = 0.06;
    private static final double ORDER_ROW_HEIGHT = 32.0;

    @FXML
    private Label optionNameLabel;
    @FXML
    private Label lastLabel;
    @FXML
    private Label bidLabel;
    @FXML
    private Label askLabel;
    @FXML
    private Label midLabel;
    @FXML
    private Label spreadLabel;

    @FXML
    private VBox buyOrdersBox;
    @FXML
    private VBox sellOrdersBox;

    // Refreshes the option prices and both sides of its order book.
    public void refresh(OptionBookDto book, int maximumOrderQuantity, String marketMakerName) {
        optionNameLabel.setText(book.getOptionName());
        lastLabel.setText(DisplayText.optionalAmount(book.getLastTradePrice()));
        bidLabel.setText(DisplayText.optionalAmount(book.getBid()));
        askLabel.setText(DisplayText.optionalAmount(book.getAsk()));
        midLabel.setText(DisplayText.optionalAmount(book.getMid()));
        spreadLabel.setText(DisplayText.optionalAmount(book.getSpread()));

        showOrders(buyOrdersBox, book.getBuyOrders(), maximumOrderQuantity, marketMakerName, true);
        showOrders(sellOrdersBox, book.getSellOrders(), maximumOrderQuantity, marketMakerName, false);
    }

    // Resets prices and displays empty buy and sell books.
    public void clear() {
        optionNameLabel.setText(NO_PRICE);
        lastLabel.setText(NO_PRICE);
        bidLabel.setText(NO_PRICE);
        askLabel.setText(NO_PRICE);
        midLabel.setText(NO_PRICE);
        spreadLabel.setText(NO_PRICE);

        showOrders(buyOrdersBox, List.of(), 1, null, true);
        showOrders(sellOrdersBox, List.of(), 1, null, false);
    }

    // Rebuilds one side of the book or displays its empty-state marker.
    private void showOrders(VBox ordersBox, List<OrderEntryDto> orders, int maximumOrderQuantity,
                            String marketMakerName, boolean bid) {
        ordersBox.getChildren().clear();

        if (orders.isEmpty()) {
            Label placeholder = new Label(NO_ORDERS);
            placeholder.getStyleClass().add("empty-orders");
            ordersBox.getChildren().add(placeholder);
            return;
        }

        orders.stream()
                .map(order -> createOrderRow(order, maximumOrderQuantity, marketMakerName, bid))
                .forEach(ordersBox.getChildren()::add);
    }

    // Creates an order row whose depth bar is proportional to the largest visible order.
    private StackPane createOrderRow(OrderEntryDto order, int maximumOrderQuantity,
                                     String marketMakerName, boolean bid) {
        double barRatio = Math.min(1.0, Math.max(MINIMUM_BAR_RATIO,
                (double) order.getQuantity() / Math.max(1, maximumOrderQuantity))); // Keep small orders visible

        StackPane row = new StackPane();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMinHeight(ORDER_ROW_HEIGHT);
        row.setPrefHeight(ORDER_ROW_HEIGHT);
        row.setMaxWidth(Double.MAX_VALUE);
        row.getStyleClass().addAll("order-depth-row", bid ? "bid-order-row" : "ask-order-row");

        Region bar = new Region();
        bar.setMaxHeight(Double.MAX_VALUE);
        bar.prefWidthProperty().bind(row.widthProperty().multiply(barRatio)); // Scale depth with row width
        bar.maxWidthProperty().bind(row.widthProperty().multiply(barRatio));
        bar.getStyleClass().addAll("order-depth-bar", bid ? "bid-depth-bar" : "ask-depth-bar");
        StackPane.setAlignment(bar, Pos.CENTER_LEFT);

        HBox content = createOrderContent(order, marketMakerName, bid);
        row.getChildren().addAll(bar, content);
        return row;
    }

    // Builds the labels placed above an order's depth bar.
    private HBox createOrderContent(OrderEntryDto order, String marketMakerName, boolean bid) {
        Label userLabel = new Label(order.getUserName());
        userLabel.getStyleClass().add("order-user");

        HBox user = new HBox(5.0, userLabel);
        user.setAlignment(Pos.CENTER_LEFT);
        if (order.getUserName().equals(marketMakerName)) { // Mark liquidity supplied by the market maker
            Label marketMakerTag = new Label(MARKET_MAKER_TAG);
            marketMakerTag.getStyleClass().add("order-mm-tag");
            user.getChildren().add(marketMakerTag);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label quantity = new Label(Integer.toString(order.getQuantity()));
        quantity.getStyleClass().add("order-quantity");

        Label price = new Label(DisplayText.amount(order.getPricePerShare()));
        price.getStyleClass().addAll("order-price", bid ? "bid-order-price" : "ask-order-price");

        HBox content = new HBox(10.0, user, spacer, quantity, price);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(0.0, 9.0, 0.0, 9.0));
        content.setMaxWidth(Double.MAX_VALUE);
        return content;
    }

}
