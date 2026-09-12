package guessmarket.ui.javafx.components.events.orderbookdetails.optionbook;

import guessmarket.dto.OptionBookDto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;

public class OptionBookController {
    private static final String NO_PRICE = "-";
    private static final String AMOUNT_FORMAT = "%.2f";
    private static final String NO_ORDERS = "No waiting orders";

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
    private ToggleButton buyOrdersToggle;
    @FXML
    private ToggleButton sellOrdersToggle;

    @FXML
    private TableView<BookOrderTableRow> ordersTable;
    @FXML
    private TableColumn<BookOrderTableRow, String> orderUserColumn;
    @FXML
    private TableColumn<BookOrderTableRow, Number> orderQuantityColumn;
    @FXML
    private TableColumn<BookOrderTableRow, Number> orderPriceColumn;

    private final ToggleGroup shownSide = new ToggleGroup();
    private final ObservableList<BookOrderTableRow> orders = FXCollections.observableArrayList();

    private OptionBookDto book;

    @FXML
    private void initialize() {
        buyOrdersToggle.setToggleGroup(shownSide);
        sellOrdersToggle.setToggleGroup(shownSide);

        keepOneSideSelected();
        initOrdersTable();
    }

    public void refresh(OptionBookDto book) {
        this.book = book;

        optionNameLabel.setText(book.getOptionName());
        lastLabel.setText(formatPrice(book.getLastTradePrice()));
        bidLabel.setText(formatPrice(book.getBid()));
        askLabel.setText(formatPrice(book.getAsk()));
        midLabel.setText(formatPrice(book.getMid()));
        spreadLabel.setText(formatPrice(book.getSpread()));

        showSelectedSide();
    }

    public void clear() {
        book = null;

        optionNameLabel.setText(NO_PRICE);
        lastLabel.setText(NO_PRICE);
        bidLabel.setText(NO_PRICE);
        askLabel.setText(NO_PRICE);
        midLabel.setText(NO_PRICE);
        spreadLabel.setText(NO_PRICE);

        orders.clear();
    }

    // A toggle group allows clearing the selection; one side must always stay shown.
    private void keepOneSideSelected() {
        shownSide.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                shownSide.selectToggle(oldValue);
            }
            showSelectedSide();
        });
    }

    private void initOrdersTable() {
        ordersTable.setPlaceholder(new Label(NO_ORDERS));

        orderUserColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        orderQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        orderPriceColumn.setCellValueFactory(new PropertyValueFactory<>("pricePerShare"));
        orderPriceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Number value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : String.format(AMOUNT_FORMAT, value.doubleValue()));
            }
        });

        ordersTable.setItems(orders);
    }

    private void showSelectedSide() {
        if (book == null) {
            orders.clear();
            return;
        }
        orders.setAll((buyOrdersToggle.isSelected() ? book.getBuyOrders() : book.getSellOrders())
                .stream().map(BookOrderTableRow::new).toList());
    }

    private String formatPrice(Double price) {
        return price == null ? NO_PRICE : String.format(AMOUNT_FORMAT, price);
    }
}
