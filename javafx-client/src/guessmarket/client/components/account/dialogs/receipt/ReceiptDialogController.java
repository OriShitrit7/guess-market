package guessmarket.client.components.account.dialogs.receipt;

import guessmarket.dto.OrderActionDto;
import guessmarket.dto.OrderResultDto;
import guessmarket.dto.TradeDto;
import guessmarket.client.components.DisplayText;
import javafx.fxml.FXML;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;

// Presents the executed portions, totals and resulting balance of a trading action.
public class ReceiptDialogController {
    private static final String ORDER_SUMMARY_FORMAT = "%s %d \"%s\" at %s";
    private static final String PURCHASE_SUMMARY_FORMAT = "Bought %d \"%s\"";
    private static final String SHARES_FORMAT = "%d shares";
    private static final String AT_PRICE_FORMAT = "at %s";
    private static final String COMMISSION_FORMAT = "Commission (%d%%)";


    private static final String BUY = "Buy";
    private static final String SELL = "Sell";
    private static final String MATCHED = "Matched";
    private static final String MINTED = "Minted";
    private static final String WAITING = "Waiting";
    private static final String NO_MATCH = "No match yet.";
    private static final String SHARES_COST = "Shares cost";
    private static final String COMMISSION_PAID = "Commission paid";
    private static final String TOTAL_PAID = "Total paid";
    private static final String TOTAL_RECEIVED = "Total received";
    private static final String NEW_BALANCE = "New balance";
    private static final String BALANCE_UNCHANGED = "Balance unchanged";
    private static final String ORDER_SUBMITTED = "Order Submitted";
    private static final String PURCHASE_COMPLETED = "Purchase Completed";

    private static final int LABEL_COLUMN = 0;
    private static final int QUANTITY_COLUMN = 1;
    private static final int VALUE_COLUMN = 2;
    private static final int FULL_WIDTH = 3;
    private static final double NOTE_FONT_SIZE = 12.0;

    @FXML
    private DialogPane dialogPane;
    @FXML
    private Label summaryLabel;
    @FXML
    private Separator linesSeparator;
    @FXML
    private GridPane linesGrid;
    @FXML
    private Separator totalsSeparator;
    @FXML
    private GridPane totalsGrid;
    @FXML
    private Label balanceTitleLabel;
    @FXML
    private Label balanceLabel;

    @FXML
    private void initialize() {
        bindVisibility(linesSeparator, linesGrid);
        bindVisibility(totalsSeparator, totalsGrid);
    }

    // A group with nothing to show leaves no gap and no separator of its own behind.
    private void bindVisibility(Separator separator, GridPane grid) {
        separator.managedProperty().bind(separator.visibleProperty());
        grid.managedProperty().bind(grid.visibleProperty());
        separator.visibleProperty().bind(grid.visibleProperty());
    }

    // Reports what an order actually did, which the submitting user could not know in advance.
    public void setOrderReceipt(OrderResultDto result, String optionName, int quantity,
                                double pricePerShare, double newBalance) {
        boolean buy = result.getAction() == OrderActionDto.BUY;

        dialogPane.setHeaderText(ORDER_SUBMITTED);
        summaryLabel.setText(String.format(ORDER_SUMMARY_FORMAT, buy ? BUY : SELL, quantity,
                optionName, DisplayText.amount(pricePerShare)));

        showOrderLines(result, pricePerShare);
        linesGrid.setVisible(true);
        showOrderTotals(result, buy);
        showBalance(newBalance, executedQuantity(result) > 0);
    }

    // Reports a completed LMSR purchase, which always executes in full.
    public void setPurchaseReceipt(TradeDto trade, int commissionPercent, double newBalance) {
        dialogPane.setHeaderText(PURCHASE_COMPLETED);
        summaryLabel.setText(String.format(PURCHASE_SUMMARY_FORMAT, trade.getQuantity(), trade.getOptionName()));
        linesGrid.setVisible(false);

        addTotal(SHARES_COST, DisplayText.amount(trade.getSharesCost()));
        addTotal(String.format(COMMISSION_FORMAT, commissionPercent),
                DisplayText.amount(trade.getCommissionCost()));
        addTotal(TOTAL_PAID, DisplayText.amount(trade.getSharesCost() + trade.getCommissionCost()));
        showBalance(newBalance, true);
    }

    // Lists each executed or waiting portion of an Order Book request.
    private void showOrderLines(OrderResultDto result, double pricePerShare) {
        if (executedQuantity(result) == 0) {
            addNote(NO_MATCH);
        }
        if (result.getMatchedQuantity() > 0) {
            addLine(MATCHED, result.getMatchedQuantity(), DisplayText.amount(result.getMatchedAmount()));
        }
        if (result.getMintedQuantity() > 0) {
            addLine(MINTED, result.getMintedQuantity(), DisplayText.amount(result.getMintedAmount()));
        }
        if (result.getRemainingQuantity() > 0) {
            addLine(WAITING, result.getRemainingQuantity(),
                    String.format(AT_PRICE_FORMAT, DisplayText.amount(pricePerShare)));
        }
    }

    // Only a buyer pays commission, so a sale is worth exactly the value of its matched shares.
    private void showOrderTotals(OrderResultDto result, boolean buy) {
        double executedAmount = result.getMatchedAmount() + result.getMintedAmount();

        totalsGrid.setVisible(executedQuantity(result) > 0);

        if (executedQuantity(result) == 0) {
            return;
        }
        if (buy) {
            addTotal(COMMISSION_PAID, DisplayText.amount(result.getTotalCommissionPaid()));
        }
        addTotal(buy ? TOTAL_PAID : TOTAL_RECEIVED,
                DisplayText.amount(executedAmount + result.getTotalCommissionPaid()));
    }

    private void showBalance(double newBalance, boolean changed) {
        balanceTitleLabel.setText(changed ? NEW_BALANCE : BALANCE_UNCHANGED);
        balanceLabel.setText(DisplayText.amount(newBalance));
    }

    private int executedQuantity(OrderResultDto result) {
        return result.getMatchedQuantity() + result.getMintedQuantity();
    }

    private void addLine(String label, int quantity, String value) {
        int row = linesGrid.getRowCount();
        Label labelCell = new Label(label);
        Label quantityCell = new Label(String.format(SHARES_FORMAT, quantity));
        Label valueCell = new Label(value);

        labelCell.getStyleClass().add("receipt-line-label");
        quantityCell.getStyleClass().add("receipt-line-quantity");
        valueCell.getStyleClass().add("receipt-line-value");

        linesGrid.add(labelCell, LABEL_COLUMN, row);
        linesGrid.add(quantityCell, QUANTITY_COLUMN, row);
        linesGrid.add(valueCell, VALUE_COLUMN, row);
    }

    private void addNote(String text) {
        Label note = new Label(text);

        note.setFont(Font.font(null, NOTE_FONT_SIZE));
        note.setWrapText(true);
        note.getStyleClass().add("receipt-note");
        linesGrid.add(note, LABEL_COLUMN, linesGrid.getRowCount(), FULL_WIDTH, 1);
    }

    private void addTotal(String label, String value) {
        int row = totalsGrid.getRowCount();
        Label labelCell = new Label(label);
        Label valueCell = new Label(value);

        labelCell.getStyleClass().add("receipt-total-label");
        valueCell.getStyleClass().add("receipt-total-value");

        totalsGrid.add(labelCell, 0, row);
        totalsGrid.add(valueCell, 1, row);
    }

}
