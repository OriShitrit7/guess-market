package guessmarket.ui.javafx.components.users.dialogs.submitorder;

import guessmarket.ui.javafx.components.DisplayText;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;
import java.util.Objects;

// Collects an Order Book request and previews its value and availability limits.
public class SubmitOrderDialogController {
    private static final String HEADER_FORMAT = "%s - Submit Order";
    private static final String COMMISSION_FORMAT = "Commission (%d%%) :";
    private static final String EMPTY_AMOUNT = "$0.00";
    private static final String BALANCE_TITLE = "Available Balance :";
    private static final String SHARES_TITLE = "Available Shares :";
    private static final String MAXIMUM_VALUE_TITLE = "Maximum order value :";
    private static final String MINIMUM_VALUE_TITLE = "Minimum proceeds :";
    private static final String MAXIMUM_TOTAL_TITLE = "Maximum Total :";
    private static final String MINIMUM_TOTAL_TITLE = "Minimum Total :";
    private static final String INSUFFICIENT_BALANCE_MESSAGE =
            "This order exceeds your available balance. Your account will be blocked if it executes.";
    private static final String INSUFFICIENT_SHARES_FORMAT =
            "You only have %d available shares of this option.";
    private static final double PRICE_STEP = 0.01;
    private static final int FIRST_OPTION_INDEX = 0;
    private static final int SECOND_OPTION_INDEX = 1;
    private static final int NO_QUANTITY = 0;
    private static final double OPTION_ANIMATION_MILLIS = 220;

    @FXML
    private DialogPane dialogPane;
    @FXML
    private ToggleButton buyToggle;
    @FXML
    private ToggleButton sellToggle;
    @FXML
    private ToggleButton firstOptionToggle;
    @FXML
    private ToggleButton secondOptionToggle;
    @FXML
    private TextField sharesField;
    @FXML
    private Label availableTitleLabel;
    @FXML
    private Label availableLabel;
    @FXML
    private Spinner<Double> priceSpinner;
    @FXML
    private Label orderValueTitleLabel;
    @FXML
    private Label orderValueLabel;
    @FXML
    private Label commissionTitleLabel;
    @FXML
    private Label commissionLabel;
    @FXML
    private Label totalTitleLabel;
    @FXML
    private Label totalLabel;
    @FXML
    private Label availabilityWarningLabel;
    @FXML
    private ButtonType submitOrderButtonType;

    private final ToggleGroup selectedSide = new ToggleGroup();
    private final ToggleGroup selectedOption = new ToggleGroup();

    private List<Integer> availableShares = List.of();
    private double balance;
    private int commissionPercent;

    @FXML
    private void initialize() {
        buyToggle.setToggleGroup(selectedSide);
        sellToggle.setToggleGroup(selectedSide);
        firstOptionToggle.setToggleGroup(selectedOption);
        secondOptionToggle.setToggleGroup(selectedOption);

        availabilityWarningLabel.managedProperty().bind(availabilityWarningLabel.visibleProperty());

        keepOneSelected(selectedSide);
        keepOneSelected(selectedOption);
        animateOptionChanges();
        commitSpinnerTextOnFocusLoss();
        recalculateOnChange();
    }

    // Populates the dialog with the selected event and the user's current resources.
    public void setEvent(String eventName, List<String> optionNames,
                         List<Integer> availableShares, double balance,
                         int commissionPercent, int baseValue) {
        this.availableShares = List.copyOf(availableShares);
        this.balance = balance;
        this.commissionPercent = commissionPercent;

        dialogPane.setHeaderText(String.format(HEADER_FORMAT, eventName));
        firstOptionToggle.setText(optionNames.get(FIRST_OPTION_INDEX));
        secondOptionToggle.setText(optionNames.get(SECOND_OPTION_INDEX));
        commissionTitleLabel.setText(String.format(COMMISSION_FORMAT, commissionPercent));
        initPriceSpinner(baseValue);

        recalculate();
    }

    public boolean isBuy() {
        return selectedSide.getSelectedToggle() == buyToggle;
    }

    public int getSelectedOptionIndex() {
        return selectedOption.getSelectedToggle() == firstOptionToggle
                ? FIRST_OPTION_INDEX
                : SECOND_OPTION_INDEX;
    }

    public int getQuantity() {
        return readQuantity();
    }

    public double getPricePerShare() {
        return priceSpinner.getValue();
    }

    // The spinner enforces the legal price range, so an illegal price can never be submitted.
    private void initPriceSpinner(int baseValue) {
        double maximumPrice = baseValue - PRICE_STEP;
        double initialPrice = Math.min(maximumPrice, baseValue / 2.0);

        priceSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(
                PRICE_STEP, maximumPrice, initialPrice, PRICE_STEP));
        priceSpinner.setEditable(true);
    }

    // A toggle group allows clearing the selection; here one toggle must always stay chosen.
    private void keepOneSelected(ToggleGroup group) {
        group.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                group.selectToggle(oldValue);
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

    // Typed spinner text is not committed until the editor loses focus, so commit it here.
    private void commitSpinnerTextOnFocusLoss() {
        priceSpinner.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                priceSpinner.increment(0);
            }
        });
    }

    private void recalculateOnChange() {
        sharesField.textProperty().addListener((observable, oldValue, newValue) -> recalculate());
        selectedSide.selectedToggleProperty().addListener((observable, oldValue, newValue) -> recalculate());
        selectedOption.selectedToggleProperty().addListener((observable, oldValue, newValue) -> recalculate());
        priceSpinner.valueProperty().addListener((observable, oldValue, newValue) -> recalculate());
    }

    // Recalculates labels, warnings and submission availability after any input change.
    private void recalculate() {
        showSideTitles(); // Switch labels between buy and sell terminology
        showAvailable();
        showAmounts(); // Calculate the order's maximum financial effect

        dialogPane.lookupButton(submitOrderButtonType).setDisable(!isOrderValid());
    }

    private void showSideTitles() {
        availableTitleLabel.setText(isBuy() ? BALANCE_TITLE : SHARES_TITLE);
        orderValueTitleLabel.setText(isBuy() ? MAXIMUM_VALUE_TITLE : MINIMUM_VALUE_TITLE);
        totalTitleLabel.setText(isBuy() ? MAXIMUM_TOTAL_TITLE : MINIMUM_TOTAL_TITLE);
    }

    private void showAvailable() {
        availableLabel.setText(isBuy()
                ? DisplayText.amount(balance)
                : String.valueOf(availableSharesOfSelectedOption()));
    }

    // Only the buyer pays commission, so a sell order is worth exactly its order value.
    private void showAmounts() {
        int quantity = readQuantity();

        if (quantity <= NO_QUANTITY) {
            orderValueLabel.setText(EMPTY_AMOUNT);
            commissionLabel.setText(EMPTY_AMOUNT);
            totalLabel.setText(EMPTY_AMOUNT);
            updateAvailabilityWarning(availabilityWarningLabel.getText(), false);
            return;
        }
        double orderValue = quantity * getPricePerShare();
        double commission = isBuy() ? orderValue * commissionPercent / 100.0 : 0;
        double total = orderValue + commission;

        orderValueLabel.setText(DisplayText.amount(orderValue));
        commissionLabel.setText(DisplayText.amount(commission));
        totalLabel.setText(DisplayText.amount(total));
        showAvailabilityWarning(quantity, total);
    }

    // Shows the resource warning that applies to the selected order side.
    private void showAvailabilityWarning(int quantity, double total) {
        if (isBuy()) {
            updateAvailabilityWarning(INSUFFICIENT_BALANCE_MESSAGE, total > balance);
            return;
        }

        int availableShares = availableSharesOfSelectedOption();
        updateAvailabilityWarning(String.format(INSUFFICIENT_SHARES_FORMAT, availableShares),
                quantity > availableShares);
    }

    // Updates the warning and resizes the dialog only when its layout may have changed.
    private void updateAvailabilityWarning(String message, boolean visible) {
        boolean textChanged = !Objects.equals(message, availabilityWarningLabel.getText());
        boolean visibilityChanged = visible != availabilityWarningLabel.isVisible();

        availabilityWarningLabel.setText(message);
        availabilityWarningLabel.setVisible(visible);

        if (visibilityChanged || visible && textChanged) {
            Platform.runLater(this::resizeDialogToContent);
        }
    }

    private void resizeDialogToContent() {
        dialogPane.applyCss();
        dialogPane.layout();

        if (dialogPane.getScene() != null
                && dialogPane.getScene().getWindow() instanceof Stage) {
            ((Stage) dialogPane.getScene().getWindow()).sizeToScene();
        }
    }

    private boolean isOrderValid() {
        int quantity = readQuantity();

        return quantity > NO_QUANTITY && (isBuy() || quantity <= availableSharesOfSelectedOption());
    }

    private int availableSharesOfSelectedOption() {
        int optionIndex = getSelectedOptionIndex();

        return optionIndex < availableShares.size() ? availableShares.get(optionIndex) : NO_QUANTITY;
    }

    private int readQuantity() {
        try {
            return Integer.parseInt(sharesField.getText().trim());
        } catch (NumberFormatException ignored) {
            return NO_QUANTITY;
        }
    }
}
