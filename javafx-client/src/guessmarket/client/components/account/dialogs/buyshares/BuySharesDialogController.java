package guessmarket.client.components.account.dialogs.buyshares;

import guessmarket.dto.OptionStateDto;
import guessmarket.dto.TradeDto;
import guessmarket.client.components.DisplayText;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;
import java.util.function.BiConsumer;

// Collects an LMSR purchase and previews its current cost before confirmation.
public class BuySharesDialogController {
    private static final String HEADER_FORMAT = "%s - Buy Shares";
    private static final String OPTION_FORMAT = "%s   %s";
    private static final String COMMISSION_FORMAT = "Commission (%d%%) :";
    private static final String EMPTY_AMOUNT = "$0.00";
    private static final String YES_OPTION = "Yes";
    private static final int FIRST_OPTION_INDEX = 0;
    private static final int SECOND_OPTION_INDEX = 1;
    private static final int NO_QUANTITY = 0;
    private static final double OPTION_ANIMATION_MILLIS = 220;

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
    private BiConsumer<Integer, Integer> quoteRequester;
    private double balance;

    @FXML
    private void initialize() {
        firstOptionToggle.setToggleGroup(selectedOption);
        secondOptionToggle.setToggleGroup(selectedOption);

        insufficientBalanceLabel.managedProperty().bind(insufficientBalanceLabel.visibleProperty());
        insufficientBalanceLabel.visibleProperty().addListener(
                (observable, oldValue, newValue) -> Platform.runLater(this::resizeDialogToContent));

        keepOneOptionSelected();
        animateOptionChanges();
        recalculateOnChange();
    }

    // Receives everything the dialog needs. Prices are requested through quoteRequester, because only
    // the server can calculate them.
    public void setEvent(String eventName, List<OptionStateDto> options, double balance,
                         int commissionPercent,
                         BiConsumer<Integer, Integer> quoteRequester) {
        this.quoteRequester = quoteRequester;
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

    private void animateOptionChanges() {
        selectedOption.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue instanceof ToggleButton) {
                playOptionTransition((ToggleButton) oldValue);
            }
            if (newValue instanceof ToggleButton) {
                playOptionTransition((ToggleButton) newValue);
            }
        });
    }

    private void playOptionTransition(ToggleButton option) {
        FadeTransition fade = new FadeTransition(Duration.millis(OPTION_ANIMATION_MILLIS), option);
        fade.setFromValue(0.65);
        fade.setToValue(1.0);

        ScaleTransition scale = new ScaleTransition(Duration.millis(OPTION_ANIMATION_MILLIS), option);
        scale.setFromX(0.985);
        scale.setFromY(0.985);
        scale.setToX(1.0);
        scale.setToY(1.0);

        new ParallelTransition(fade, scale).play();
    }

    private void resizeDialogToContent() {
        dialogPane.applyCss();
        dialogPane.layout();

        if (dialogPane.getScene() != null
                && dialogPane.getScene().getWindow() instanceof Stage) {
            ((Stage) dialogPane.getScene().getWindow()).sizeToScene();
        }
    }

    private void recalculateOnChange() {
        sharesField.textProperty().addListener((observable, oldValue, newValue) -> recalculate());
        selectedOption.selectedToggleProperty().addListener((observable, oldValue, newValue) -> recalculate());
    }

    // Asks for the current cost of the purchase, and enables the button only when the input is valid.
    // The amounts appear once the quote arrives.
    private void recalculate() {
        int quantity = readQuantity();
        boolean valid = quantity > NO_QUANTITY && selectedOption.getSelectedToggle() != null;

        dialogPane.lookupButton(buyButtonType).setDisable(!valid);

        showAmounts(null);
        if (valid && quoteRequester != null) {
            quoteRequester.accept(getSelectedOptionIndex(), quantity);
        }
    }

    // Shows a quote from the server, unless the user has changed the option or quantity since it was requested.
    public void showQuote(int optionIndex, int quantity, TradeDto trade) {
        if (optionIndex == getSelectedOptionIndex() && quantity == readQuantity()) {
            showAmounts(trade);
        }
    }

    // Updates the displayed quote and warns when the purchase exceeds the user's balance.
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
