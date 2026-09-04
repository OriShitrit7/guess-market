package guessmarket.ui.javafx.components.events;

import guessmarket.ui.javafx.components.main.MainController;
import javafx.fxml.FXML;
import org.controlsfx.control.SegmentedButton;

public class EventsController {
    private MainController mainController;

    @FXML private SegmentedButton methodFilter;
    @FXML private SegmentedButton statusFilter;
    @FXML private SegmentedButton commissionFilter;

    @FXML
    private void initialize() {
        methodFilter.setToggleGroup(null);
        statusFilter.setToggleGroup(null);
        commissionFilter.setToggleGroup(null);
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }


}
