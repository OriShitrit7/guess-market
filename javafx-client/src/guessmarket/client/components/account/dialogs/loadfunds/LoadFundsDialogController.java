package guessmarket.client.components.account.dialogs.loadfunds;

import guessmarket.client.components.DisplayText;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.util.Objects;

// Collects the amount of money to load and shows the balance it would lead to.
public class LoadFundsDialogController {
    @FXML private DialogPane dialogPane;
    @FXML private ButtonType loadFundsButtonType;
    @FXML private TextField amountField;
    @FXML private Label currentBalanceLabel;
    @FXML private Label newBalanceLabel;
    @FXML private Label validationLabel;
    @FXML private Label availabilityLabel;

    private BigDecimal currentBalance;
    private BigDecimal amount;
    private boolean submissionEnabled;

    @FXML
    private void initialize() {
        amountField.textProperty().addListener((observable, oldValue, newValue) -> updateState());
        updateState();
    }

    public void setCurrentBalance(BigDecimal balance) {
        currentBalance = Objects.requireNonNull(balance);
        currentBalanceLabel.setText(DisplayText.amount(balance.doubleValue()));
        updateState();
    }

    public void setSubmissionEnabled(boolean enabled) {
        submissionEnabled = enabled;
        availabilityLabel.setVisible(!enabled);
        availabilityLabel.setManaged(!enabled);
        updateState();
    }

    public BigDecimal getAmount() {
        return amount;
    }

    // Accepts only a positive amount with at most two decimal places, the same rule the server applies.
    private void updateState() {
        String input = amountField.getText().trim();
        amount = null;

        if (input.matches("\\d+(?:\\.\\d{1,2})?")) {
            BigDecimal parsed = new BigDecimal(input);
            if (parsed.signum() > 0) {
                amount = parsed;
            }
        }

        boolean showError = !input.isEmpty() && amount == null;
        validationLabel.setText(showError ? "Enter a positive amount with up to 2 decimal places." : "");
        validationLabel.setVisible(showError);
        validationLabel.setManaged(showError);
        newBalanceLabel.setText(amount != null && currentBalance != null
                ? DisplayText.amount(currentBalance.add(amount).doubleValue()) : "—");

        Button confirmButton = (Button) dialogPane.lookupButton(loadFundsButtonType);
        confirmButton.setDisable(amount == null || currentBalance == null || !submissionEnabled);
    }
}
