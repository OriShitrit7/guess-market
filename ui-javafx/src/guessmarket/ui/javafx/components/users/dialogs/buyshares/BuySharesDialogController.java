package guessmarket.ui.javafx.components.users.dialogs.buyshares;

import guessmarket.dto.OptionStateDto;
import guessmarket.dto.TradeDto;
import guessmarket.ui.javafx.components.DisplayText;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

import java.util.List;
import java.util.function.BiFunction;

public class BuySharesDialogController {
    private static final String HEADER_FORMAT = "%s - Buy Shares";
    private static final String OPTION_FORMAT = "%s   %s";
    private static final String COMMISSION_FORMAT = "Commission (%d%%) :";
    private static final String EMPTY_AMOUNT = "$0.00";
    private static final String YES_OPTION = "Yes";
    private static final int FIRST_OPTION_INDEX = 0;
    private static final int SECOND_OPTION_INDEX = 1;
    private static final int NO_QUANTITY = 0;

    @FXML
    private DialogPane dialogPane;
    @FXML
    private ToggleButton firstOptionToggle;
    @FXML
    private ToggleButton secondOptionToggle;
    @FXML
    private TextField sharesField;
    @FXML
    private Label availableBalanceLabel;
    @FXML
    private Label sharesCostLabel;
    @FXML
    private Label commissionTitleLabel;
    @FXML
    private Label commissionLabel;
    @FXML
    private Label totalLabel;
    @FXML
    private Label insufficientBalanceLabel;
    @FXML
    private ButtonType buyButtonType;

    private final ToggleGroup selectedOption = new ToggleGroup();
    private BiFunction<Integer, Integer, TradeDto> quote;
    private double balance;

    @FXML
    private void initialize() {
        firstOptionToggle.setToggleGroup(selectedOption);
        secondOptionToggle.setToggleGroup(selectedOption);

        insufficientBalanceLabel.managedProperty().bind(insufficientBalanceLabel.visibleProperty());

        keepOneOptionSelected();
        recalculateOnChange();
    }

    // Receives everything the dialog needs without giving it access to the engine.
    public void setEvent(String eventName, List<OptionStateDto> options, double balance,
                         int commissionPercent,
                         BiFunction<Integer, Integer, TradeDto> quote) {
        this.quote = quote;
        this.balance = balance;

        dialogPane.setHeaderText(String.format(HEADER_FORMAT, eventName));
        firstOptionToggle.setText(toToggleText(options.get(FIRST_OPTION_INDEX)));
        secondOptionToggle.setText(toToggleText(options.get(SECOND_OPTION_INDEX)));
        availableBalanceLabel.setText(DisplayText.amount(balance));
        commissionTitleLabel.setText(String.format(COMMISSION_FORMAT, commissionPercent));

        selectDefaultOption(options);
        recalculate();
    }

    public int getSelectedOptionIndex() {
        return selectedOption.getSelectedToggle() == firstOptionToggle
                ? FIRST_OPTION_INDEX
                : SECOND_OPTION_INDEX;
    }

    public int getQuantity() {
        return readQuantity();
    }

    // A toggle group allows clearing the selection; here one option must always stay chosen.
    private void keepOneOptionSelected() {
        selectedOption.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                selectedOption.selectToggle(oldValue);
            }
        });
    }

    private void recalculateOnChange() {
        sharesField.textProperty().addListener((observable, oldValue, newValue) -> recalculate());
        selectedOption.selectedToggleProperty().addListener((observable, oldValue, newValue) -> recalculate());
    }

    // Asks the engine what the purchase would cost right now, and enables the button only when it can run.
    private void recalculate() {
        int quantity = readQuantity();
        boolean valid = quantity > NO_QUANTITY && selectedOption.getSelectedToggle() != null;

        dialogPane.lookupButton(buyButtonType).setDisable(!valid);

        if (!valid || quote == null) {
            showAmounts(null);
            return;
        }
        showAmounts(quote.apply(getSelectedOptionIndex(), quantity));
    }

    private void showAmounts(TradeDto trade) {
        if (trade == null) {
            sharesCostLabel.setText(EMPTY_AMOUNT);
            commissionLabel.setText(EMPTY_AMOUNT);
            totalLabel.setText(EMPTY_AMOUNT);
            insufficientBalanceLabel.setVisible(false);
            return;
        }
        double total = trade.getSharesCost() + trade.getCommissionCost();

        sharesCostLabel.setText(DisplayText.amount(trade.getSharesCost()));
        commissionLabel.setText(DisplayText.amount(trade.getCommissionCost()));
        totalLabel.setText(DisplayText.amount(total));
        insufficientBalanceLabel.setVisible(total > balance);
    }

    private int readQuantity() {
        try {
            return Integer.parseInt(sharesField.getText().trim());
        } catch (NumberFormatException ignored) {
            return NO_QUANTITY;
        }
    }

    // Prefer an explicitly named Yes option; otherwise the event's first option is the default.
    private void selectDefaultOption(List<OptionStateDto> options) {
        boolean secondOptionIsYes = options.get(SECOND_OPTION_INDEX).getOptionName()
                .trim().equalsIgnoreCase(YES_OPTION);

        selectedOption.selectToggle(secondOptionIsYes ? secondOptionToggle : firstOptionToggle);
    }

    private String toToggleText(OptionStateDto option) {
        return String.format(OPTION_FORMAT, option.getOptionName(),
                DisplayText.optionalAmount(option.getCurrentValue()));
    }
}
