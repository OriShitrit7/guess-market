package guessmarket.ui.javafx.components.events.eventresult;

import guessmarket.dto.OptionStateDto;
import guessmarket.ui.javafx.components.DisplayText;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.List;

// Presents the winning option and final share totals for a closed event.
public class EventResultController {
    private static final String NO_WINNER = "-";
    private static final String EMPTY_TEXT = "";
    private static final String NO_SHARES = "0";
    private static final String PAYOUT_FORMAT = "Pays %s per share";
    private static final int FIRST_OPTION_INDEX = 0;
    private static final int SECOND_OPTION_INDEX = 1;
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

    // Shows how a closed event ended, for the event as a whole.
    public void refresh(String winnerName, double winningSharePayout, List<OptionStateDto> options) {
        winnerLabel.setText(winnerName == null ? NO_WINNER : winnerName);
        payoutPerShareLabel.setText(String.format(PAYOUT_FORMAT, DisplayText.amount(winningSharePayout)));

        if (options.size() < OPTION_COUNT) {
            clearOptions();
            return;
        }
        showOption(options.get(FIRST_OPTION_INDEX), firstOptionNameLabel, firstOptionSharesLabel);
        showOption(options.get(SECOND_OPTION_INDEX), secondOptionNameLabel, secondOptionSharesLabel);
    }

    private void showOption(OptionStateDto option, Label nameLabel, Label sharesLabel) {
        nameLabel.setText(option.getOptionName());
        sharesLabel.setText(String.valueOf(option.getTotalSharesBought()));
    }

    private void clearOptions() {
        firstOptionNameLabel.setText(EMPTY_TEXT);
        firstOptionSharesLabel.setText(NO_SHARES);
        secondOptionNameLabel.setText(EMPTY_TEXT);
        secondOptionSharesLabel.setText(NO_SHARES);
    }
}
