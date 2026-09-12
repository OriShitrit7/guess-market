package guessmarket.ui.javafx.components.users;

import guessmarket.dto.OptionHoldingDto;
import guessmarket.ui.javafx.components.DisplayText;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.List;

public class ResultCardController {
    private static final String NO_WINNER = "-";
    private static final String EMPTY_TEXT = "";
    private static final String NO_SHARES = "0";
    private static final String PAYOUT_FORMAT = "Pays %s per share";
    private static final int OPTION_COUNT = 2;

    @FXML
    private Label winnerLabel;
    @FXML
    private Label payoutPerShareLabel;

    @FXML
    private Label firstOptionNameLabel;
    @FXML
    private Label firstOptionSharesLabel;
    @FXML
    private Label secondOptionNameLabel;
    @FXML
    private Label secondOptionSharesLabel;

    @FXML
    private Label profitLossLabel;

    // Shows how a closed event ended for the selected user.
    public void refresh(String winnerName, double winningSharePayout, List<OptionHoldingDto> holdings,
                        double profitLoss) {
        winnerLabel.setText(winnerName == null ? NO_WINNER : winnerName);
        payoutPerShareLabel.setText(String.format(PAYOUT_FORMAT, DisplayText.amount(winningSharePayout)));
        profitLossLabel.setText(DisplayText.signedAmount(profitLoss));

        showHoldings(holdings);
    }

    private void showHoldings(List<OptionHoldingDto> holdings) {
        if (holdings.size() < OPTION_COUNT) {
            clearHoldings();
            return;
        }
        showHolding(holdings.get(0), firstOptionNameLabel, firstOptionSharesLabel);
        showHolding(holdings.get(1), secondOptionNameLabel, secondOptionSharesLabel);
    }

    private void showHolding(OptionHoldingDto holding, Label nameLabel, Label sharesLabel) {
        nameLabel.setText(holding.getOptionName());
        sharesLabel.setText(String.valueOf(holding.getQuantity()));
    }

    private void clearHoldings() {
        firstOptionNameLabel.setText(EMPTY_TEXT);
        firstOptionSharesLabel.setText(NO_SHARES);
        secondOptionNameLabel.setText(EMPTY_TEXT);
        secondOptionSharesLabel.setText(NO_SHARES);
    }
}
