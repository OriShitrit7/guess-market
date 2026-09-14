package guessmarket.ui.javafx.components.users.dialogs.createevent;

import guessmarket.dto.CommissionPolicyDto;
import guessmarket.dto.NewEventDto;
import guessmarket.dto.TradingMethodTypeDto;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableStringValue;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

// Collects and validates the values needed to create a new market event.
public class CreateEventDialogController {
    private static final int MIN_COMMISSION = 0;
    private static final int MAX_COMMISSION = 90;
    private static final int INITIAL_COMMISSION = 5;
    private static final int MIN_B = 1;
    private static final int MAX_B = 10000;
    private static final int INITIAL_B = 100;
    private static final int MIN_BASE_VALUE = 1;
    private static final int MAX_BASE_VALUE = 1000;
    private static final int MIN_INITIAL_INVESTMENT = 0;
    private static final int MAX_INITIAL_INVESTMENT = 1000000;
    private static final int INITIAL_INVESTMENT_STEP = 100;

    @FXML
    private DialogPane dialogPane;
    @FXML
    private ButtonType createEventButtonType;

    @FXML
    private TextField eventNameField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextField firstOptionField;
    @FXML
    private TextField secondOptionField;

    @FXML
    private Spinner<Integer> commissionSpinner;
    @FXML
    private RadioButton onPurchaseRadio;
    @FXML
    private RadioButton onCloseRadio;

    @FXML
    private RadioButton lmsrRadio;
    @FXML
    private RadioButton orderBookRadio;
    @FXML
    private HBox lmsrFields;
    @FXML
    private VBox orderBookFields;
    @FXML
    private Spinner<Integer> bSpinner;
    @FXML
    private Spinner<Integer> baseValueSpinner;
    @FXML
    private Spinner<Integer> initialSpinner;
    @FXML
    private CheckBox mintCheckBox;

    private final ToggleGroup commissionPolicy = new ToggleGroup();
    private final ToggleGroup tradingMethod = new ToggleGroup();

    @FXML
    // Configures the choices, method-specific fields, spinners and form validation.
    private void initialize() {
        onPurchaseRadio.setToggleGroup(commissionPolicy);
        onCloseRadio.setToggleGroup(commissionPolicy);
        lmsrRadio.setToggleGroup(tradingMethod);
        orderBookRadio.setToggleGroup(tradingMethod);

        keepOneSelected(commissionPolicy);
        keepOneSelected(tradingMethod);

        lmsrFields.managedProperty().bind(lmsrFields.visibleProperty());
        orderBookFields.managedProperty().bind(orderBookFields.visibleProperty());

        initSpinners();
        showMethodFields();
        tradingMethod.selectedToggleProperty().addListener(
                (observable, oldValue, newValue) -> showMethodFields());

        dialogPane.lookupButton(createEventButtonType).disableProperty().bind(incomplete());
    }

    // The engine validates again; this only keeps the user from submitting an obviously empty form.
    private BooleanBinding incomplete() {
        return blank(eventNameField.textProperty())
                .or(blank(descriptionArea.textProperty()))
                .or(blank(firstOptionField.textProperty()))
                .or(blank(secondOptionField.textProperty()));
    }

    // Builds an event request from the values currently entered in the form.
    public NewEventDto getRequest() {
        boolean lmsr = lmsrRadio.isSelected(); // Select the matching method-specific values

        return new NewEventDto(eventNameField.getText(), descriptionArea.getText(),
                List.of(firstOptionField.getText(), secondOptionField.getText()),
                commissionSpinner.getValue(),
                onPurchaseRadio.isSelected() ? CommissionPolicyDto.ON_PURCHASE : CommissionPolicyDto.ON_CLOSE,
                lmsr ? TradingMethodTypeDto.LMSR : TradingMethodTypeDto.ORDER_BOOK,
                bSpinner.getValue(), baseValueSpinner.getValue(), initialSpinner.getValue(),
                mintCheckBox.isSelected());
    }

    // Each trading method is defined by different values, so only the chosen one is asked for.
    private void showMethodFields() {
        boolean lmsr = lmsrRadio.isSelected();

        lmsrFields.setVisible(lmsr);
        orderBookFields.setVisible(!lmsr);
        Platform.runLater(this::resizeDialogToContent);
    }

    private void resizeDialogToContent() {
        dialogPane.applyCss();
        dialogPane.layout();

        if (dialogPane.getScene() != null
                && dialogPane.getScene().getWindow() instanceof Stage) {
            ((Stage) dialogPane.getScene().getWindow()).sizeToScene();
        }
    }

    // Configures the allowed ranges and editable behavior of every numeric field.
    private void initSpinners() {
        commissionSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                MIN_COMMISSION, MAX_COMMISSION, INITIAL_COMMISSION));
        bSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                MIN_B, MAX_B, INITIAL_B));
        baseValueSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                MIN_BASE_VALUE, MAX_BASE_VALUE, MIN_BASE_VALUE));
        initialSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                MIN_INITIAL_INVESTMENT, MAX_INITIAL_INVESTMENT, MIN_INITIAL_INVESTMENT,
                INITIAL_INVESTMENT_STEP));

        commissionSpinner.setEditable(true);
        bSpinner.setEditable(true);
        baseValueSpinner.setEditable(true);
        initialSpinner.setEditable(true);

        commitSpinnerTextOnFocusLoss(List.of(commissionSpinner, bSpinner, baseValueSpinner, initialSpinner));
    }

    // A toggle group allows clearing the selection; here one toggle must always stay chosen.
    private void keepOneSelected(ToggleGroup group) {
        group.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                group.selectToggle(oldValue);
            }
        });
    }

    // Typed spinner text is not committed until the editor loses focus, so commit it here.
    private void commitSpinnerTextOnFocusLoss(List<Spinner<Integer>> spinners) {
        for (Spinner<Integer> spinner : spinners) {
            spinner.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    spinner.increment(0);
                }
            });
        }
    }

    private BooleanBinding blank(ObservableStringValue text) {
        return Bindings.createBooleanBinding(() -> text.get() == null || text.get().isBlank(), text);
    }
}
