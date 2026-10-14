package guessmarket.client.components.events;

import guessmarket.client.components.DisplayText;
import guessmarket.client.components.StatusStyle;
import guessmarket.dto.CommissionPolicyDto;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

// Controls a selectable event card and presents its main summary details.
public class EventCardController {
    private static final String PERCENT_SIGN = "%";
    private static final String TRADING_SUFFIX = " Trading";
    private static final String ON_PURCHASE_TEXT = "\u00b7 Charged on purchase";
    private static final String ON_CLOSE_TEXT = "\u00b7 Charged when event closes";
    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");
    private static final List<String> METHOD_STYLE_CLASSES = List.of("method-lmsr", "method-order-book");

    @FXML
    private VBox eventCard;
    @FXML
    private Label eventNameLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label tradingMethodLabel;
    @FXML
    private Label commissionDescriptionLabel;
    @FXML
    private Label eventAccountLabel;

    private EventTableRow event;
    private Consumer<EventTableRow> selectionHandler;

    @FXML
    // Enables card selection with either the mouse or keyboard.
    private void initialize() {
        eventCard.setOnMouseClicked(mouseEvent -> select());
        eventCard.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER || keyEvent.getCode() == KeyCode.SPACE) {
                select();
                keyEvent.consume();
            }
        });
    }

    // Populates the card and registers the action used when it is selected.
    public void setEvent(EventTableRow event, Consumer<EventTableRow> selectionHandler) {
        this.event = event;
        this.selectionHandler = selectionHandler;

        eventNameLabel.setText(event.getName());
        statusLabel.setText(event.getStatus());
        tradingMethodLabel.setText(event.getMethodType() + TRADING_SUFFIX);
        commissionDescriptionLabel.setText(event.getCommissionPercent() + PERCENT_SIGN + " "
                + commissionTiming(event.getCommissionPolicy()));
        eventAccountLabel.setText(DisplayText.amount(event.getAccountBalance()));
        eventCard.setAccessibleText(event.getName() + ", " + event.getStatus());

        StatusStyle.apply(statusLabel, event.getStatusType());
        updateMethodStyle(event.isOrderBook());
    }

    public int getEventId() {
        return event.getEventId();
    }

    public void setSelected(boolean selected) {
        eventCard.pseudoClassStateChanged(SELECTED, selected);
    }

    private void select() {
        if (event != null && selectionHandler != null) {
            eventCard.requestFocus();
            selectionHandler.accept(event);
        }
    }

    private String commissionTiming(String policy) {
        return DisplayText.of(CommissionPolicyDto.ON_PURCHASE).equals(policy) ? ON_PURCHASE_TEXT : ON_CLOSE_TEXT;
    }

    private void updateMethodStyle(boolean orderBook) {
        tradingMethodLabel.getStyleClass().removeAll(METHOD_STYLE_CLASSES);
        tradingMethodLabel.getStyleClass().add(orderBook ? "method-order-book" : "method-lmsr");
    }
}
