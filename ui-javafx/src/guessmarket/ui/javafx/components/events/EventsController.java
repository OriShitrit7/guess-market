package guessmarket.ui.javafx.components.events;

import guessmarket.dto.EventSummaryDto;
import guessmarket.ui.javafx.components.events.lmsrdetails.LmsrDetailsController;
import guessmarket.ui.javafx.components.events.orderbookdetails.OrderBookDetailsController;
import guessmarket.ui.javafx.components.main.MainController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.SegmentedButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EventsController {
    private static final String EVENT_CARD_FXML = "event-card.fxml";
    private static final String EMPTY_BEFORE_LOAD = "Load a file to view events";
    private static final String EMPTY_AFTER_LOAD = "No events match the selected filters";

    private MainController mainController;

    @FXML
    private SegmentedButton methodFilter;
    @FXML
    private SegmentedButton statusFilter;
    @FXML
    private SegmentedButton commissionFilter;

    @FXML
    private FlowPane eventCardsPane;
    @FXML
    private Label emptyEventsLabel;

    @FXML
    private Label noEventSelectedLabel;
    @FXML
    private VBox lmsrDetailsComponent;
    @FXML
    private LmsrDetailsController lmsrDetailsComponentController;
    @FXML
    private VBox orderBookDetailsComponent;
    @FXML
    private OrderBookDetailsController orderBookDetailsComponentController;

    private final ObservableList<EventTableRow> events = FXCollections.observableArrayList();
    private final FilteredList<EventTableRow> filteredEvents = new FilteredList<>(events, row -> true);
    private final List<EventCardController> displayedCards = new ArrayList<>();

    private Integer selectedEventId;
    private boolean eventsWereLoaded;

    @FXML
    private void initialize() {
        emptyEventsLabel.managedProperty().bind(emptyEventsLabel.visibleProperty());
        noEventSelectedLabel.managedProperty().bind(noEventSelectedLabel.visibleProperty());
        lmsrDetailsComponent.managedProperty().bind(lmsrDetailsComponent.visibleProperty());
        orderBookDetailsComponent.managedProperty().bind(orderBookDetailsComponent.visibleProperty());

        clearFilterToggles();
        initFilterListeners();
        rebuildEventCards();
        clearEventDetails();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void refreshEvents() {
        Integer eventIdToRestore = selectedEventId;

        List<EventSummaryDto> summaries = mainController.getMarketManager().getEventSummaries();
        events.setAll(summaries.stream().map(EventTableRow::new).toList());
        eventsWereLoaded = true;

        EventTableRow restoredSelection = findVisibleEvent(eventIdToRestore);
        selectedEventId = restoredSelection == null ? null : restoredSelection.getEventId();
        rebuildEventCards();
        showEventDetails(restoredSelection);
    }

    private EventTableRow findVisibleEvent(Integer eventId) {
        if (eventId == null) {
            return null;
        }
        return filteredEvents.stream()
                .filter(event -> event.getEventId() == eventId)
                .findFirst()
                .orElse(null);
    }

    private void selectEvent(EventTableRow selectedEvent) {
        selectedEventId = selectedEvent.getEventId();
        displayedCards.forEach(card -> card.setSelected(card.getEventId() == selectedEventId));
        showEventDetails(selectedEvent);
    }

    // Shows the panel that matches the selected event's trading method, and hides the other one.
    private void showEventDetails(EventTableRow selectedEvent) {
        if (selectedEvent == null) {
            clearEventDetails();
            return;
        }
        noEventSelectedLabel.setVisible(false);
        lmsrDetailsComponent.setVisible(!selectedEvent.isOrderBook());
        orderBookDetailsComponent.setVisible(selectedEvent.isOrderBook());

        if (selectedEvent.isOrderBook()) {
            orderBookDetailsComponentController.refresh(
                    mainController.getMarketManager().getOrderBookState(selectedEvent.getEventId()));
            return;
        }
        lmsrDetailsComponentController.refresh(
                mainController.getMarketManager().getEventTradingState(selectedEvent.getEventId()));
    }

    private void clearEventDetails() {
        noEventSelectedLabel.setVisible(true);
        lmsrDetailsComponent.setVisible(false);
        orderBookDetailsComponent.setVisible(false);

        lmsrDetailsComponentController.clear();
        orderBookDetailsComponentController.clear();
    }

    private void clearFilterToggles() {
        methodFilter.setToggleGroup(null);
        statusFilter.setToggleGroup(null);
        commissionFilter.setToggleGroup(null);
    }

    private void rebuildEventCards() {
        eventCardsPane.getChildren().clear();
        displayedCards.clear();

        for (EventTableRow event : filteredEvents) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(EVENT_CARD_FXML));
                Node eventCard = loader.load();
                EventCardController controller = loader.getController();

                controller.setEvent(event, this::selectEvent);
                controller.setSelected(selectedEventId != null && event.getEventId() == selectedEventId);
                displayedCards.add(controller);
                eventCardsPane.getChildren().add(eventCard);
            } catch (IOException exception) {
                throw new IllegalStateException("Could not load the event card", exception);
            }
        }

        boolean empty = filteredEvents.isEmpty();
        emptyEventsLabel.setText(eventsWereLoaded ? EMPTY_AFTER_LOAD : EMPTY_BEFORE_LOAD);
        emptyEventsLabel.setVisible(empty);
    }
    
    private void initFilterListeners() {
        List.of(methodFilter, statusFilter, commissionFilter)
                .forEach(filter -> filter.getButtons().forEach(button ->
                        button.selectedProperty().addListener(
                                (observable, oldValue, newValue) -> updateFilter()
                        )
                ));
    }

    private void updateFilter() {
        filteredEvents.setPredicate(row -> matchesFilter(methodFilter, row.getMethodType())
                                    && matchesFilter(statusFilter, row.getStatus())
                                    && matchesFilter(commissionFilter, row.getCommissionPolicy()));

        EventTableRow selectedEvent = findVisibleEvent(selectedEventId);
        selectedEventId = selectedEvent == null ? null : selectedEvent.getEventId();
        rebuildEventCards();
        showEventDetails(selectedEvent);
    }

    private boolean matchesFilter(SegmentedButton filter, String rowValue) {
        boolean noFilterSelected = filter.getButtons().stream().noneMatch(ToggleButton::isSelected);
        boolean filterSelected = filter.getButtons().stream()
                .filter(ToggleButton::isSelected)
                .anyMatch(button -> button.getText().equals(rowValue));
        return noFilterSelected || filterSelected;
    }
}
