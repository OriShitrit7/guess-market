package guessmarket.ui.javafx.components.users.dialogs.closeevent;

import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

import java.util.List;

// Collects the winning option when a market maker closes an event.
public class CloseEventDialogController {
    private static final String PROMPT_FORMAT = "Select the winning option for \"%s\" :";
    private static final int FIRST_OPTION_INDEX = 0;
    private static final int SECOND_OPTION_INDEX = 1;

    @FXML
    private DialogPane dialogPane;
    @FXML
    private Label promptLabel;
    @FXML
    private RadioButton firstOptionRadio;
    @FXML
    private RadioButton secondOptionRadio;
    @FXML
    private ButtonType closeEventButtonType;

    private final ToggleGroup winningOption = new ToggleGroup();

    @FXML
    private void initialize() {
        firstOptionRadio.setToggleGroup(winningOption);
        secondOptionRadio.setToggleGroup(winningOption);

        dialogPane.lookupButton(closeEventButtonType).disableProperty()
                .bind(winningOption.selectedToggleProperty().isNull()); // Require a winning option
    }

    // Displays the event name and its options for winner selection.
    public void setEvent(String eventName, List<String> optionNames) {
        promptLabel.setText(String.format(PROMPT_FORMAT, eventName));
        firstOptionRadio.setText(optionNames.get(FIRST_OPTION_INDEX));
        secondOptionRadio.setText(optionNames.get(SECOND_OPTION_INDEX));
    }

    public int getWinningOptionIndex() {
        return winningOption.getSelectedToggle() == firstOptionRadio
                ? FIRST_OPTION_INDEX
                : SECOND_OPTION_INDEX;
    }
}
