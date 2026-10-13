package guessmarket.client.components.events;

import com.google.gson.reflect.TypeToken;
import guessmarket.client.components.events.eventholdings.EventHoldingsController;
import guessmarket.client.components.events.lmsrdetails.LmsrDetailsController;
import guessmarket.client.components.events.orderbookdetails.OrderBookDetailsController;
import guessmarket.client.util.Constants;
import guessmarket.client.util.http.HttpClientUtil;
import guessmarket.dto.EventStateDto;
import guessmarket.dto.EventSummaryDto;
import guessmarket.dto.LmsrStateDto;
import guessmarket.dto.OrderBookStateDto;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.controlsfx.control.SegmentedButton;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

// Manages event filtering, card selection and the details shown in the Events tab.
public class EventsController implements Closeable {
    private static final String EVENT_CARD_FXML = "event-card.fxml";
    private static final String EMPTY_TITLE_BEFORE_LOAD = "No events to display";
    private static final String EMPTY_MESSAGE_BEFORE_LOAD = "Load an XML file to get started.";
    private static final String EMPTY_TITLE_AFTER_LOAD = "No matching events";
    private static final String EMPTY_MESSAGE_AFTER_LOAD = "Try changing or clearing the selected filters.";

    @FXML private SegmentedButton methodFilter;
    @FXML private SegmentedButton statusFilter;
    @FXML private SegmentedButton commissionFilter;

    @FXML private FlowPane eventCardsPane;
    @FXML private VBox emptyEventsState;
    @FXML private Label emptyEventsTitleLabel;
    @FXML private Label emptyEventsLabel;

    @FXML private VBox noEventSelectedState;
    @FXML private ScrollPane detailsScroll;
    @FXML private VBox eventDetailsContent;
    @FXML private VBox lmsrDetailsComponent;
    @FXML private LmsrDetailsController lmsrDetailsComponentController;
    @FXML private VBox orderBookDetailsComponent;
    @FXML private OrderBookDetailsController orderBookDetailsComponentController;
    @FXML private EventHoldingsController eventHoldingsComponentController;

    private Timer timer;
    private TimerTask eventsRefresher;

    private final ObservableList<EventTableRow> events = FXCollections.observableArrayList();
    private final FilteredList<EventTableRow> filteredEvents = new FilteredList<>(events, row -> true);
    private final List<EventCardController> displayedCards = new ArrayList<>();

    private Integer selectedEventId;
    private boolean eventsWereLoaded;

    @FXML
    // Configures visibility bindings, event filters and the initial empty state.
    private void initialize() {
        emptyEventsState.managedProperty().bind(emptyEventsState.visibleProperty());
        noEventSelectedState.managedProperty().bind(noEventSelectedState.visibleProperty());
        lmsrDetailsComponent.managedProperty().bind(lmsrDetailsComponent.visibleProperty());
        orderBookDetailsComponent.managedProperty().bind(orderBookDetailsComponent.visibleProperty());
        detailsScroll.viewportBoundsProperty().addListener((_, _, bounds) ->
                eventDetailsContent.setMinHeight(bounds.getHeight()));

        clearFilterToggles();
        initFilterListeners();
        rebuildEventCards();
        clearEventDetails();
    }

    public void startEventsRefresher() {
        eventsRefresher = new EventsRefresher(summaries -> Platform.runLater(() -> updateEvents(summaries)));
        timer = new Timer();
        timer.schedule(eventsRefresher, Constants.REFRESH_RATE, Constants.REFRESH_RATE);
    }

    private void updateEvents(List<EventSummaryDto> summaries) {
        Integer eventIdToRestore = selectedEventId; // Preserve selection by stable ID
        events.setAll(summaries.stream().map(EventTableRow::new).toList()); // Replace displayed event data
        eventsWereLoaded = true;

        EventTableRow restoredSelection = findVisibleEvent(eventIdToRestore); // Restore only a visible selection
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
        noEventSelectedState.setVisible(false);
        lmsrDetailsComponent.setVisible(!selectedEvent.isOrderBook());
        orderBookDetailsComponent.setVisible(selectedEvent.isOrderBook());

        String finalUrl = HttpUrl.parse(Constants.EVENT_STATE_URL).newBuilder()
                .addQueryParameter(Constants.EVENT_ID_PARAMETER, String.valueOf(selectedEvent.getEventId()))
                .build().toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try (response) {
                    if (!response.isSuccessful()) {
                        return;
                    }
                    Class<? extends EventStateDto> stateClass = selectedEvent.isOrderBook() ? OrderBookStateDto.class : LmsrStateDto.class;
                    EventStateDto state = Constants.GSON_INSTANCE.fromJson(response.body().string(), stateClass);
                    Platform.runLater(() -> showEventState(state));
                }
            }
        });
    }

    private void showEventState(EventStateDto state) {
        if (selectedEventId == null || state.getSummary().getEventId() != selectedEventId) {
            return;
        }
        if (state instanceof OrderBookStateDto orderBookState) {
            orderBookDetailsComponentController.refresh(orderBookState);
        } else if (state instanceof LmsrStateDto lmsrState) {
            lmsrDetailsComponentController.refresh(lmsrState);
        }
        eventHoldingsComponentController.refresh(
                state.getSummary().getOptionNames(), state.getParticipants(), state instanceof OrderBookStateDto);
    }

    private void clearEventDetails() {
        noEventSelectedState.setVisible(true);
        lmsrDetailsComponent.setVisible(false);
        orderBookDetailsComponent.setVisible(false);

        lmsrDetailsComponentController.clear();
        orderBookDetailsComponentController.clear();
        eventHoldingsComponentController.clear();
    }

    private void clearFilterToggles() {
        methodFilter.setToggleGroup(null);
        statusFilter.setToggleGroup(null);
        commissionFilter.setToggleGroup(null);
    }

    // Recreates the event cards and updates the message shown when no card is visible.
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
        emptyEventsTitleLabel.setText(eventsWereLoaded
                ? EMPTY_TITLE_AFTER_LOAD
                : EMPTY_TITLE_BEFORE_LOAD);
        emptyEventsLabel.setText(eventsWereLoaded
                ? EMPTY_MESSAGE_AFTER_LOAD
                : EMPTY_MESSAGE_BEFORE_LOAD);
        emptyEventsState.setVisible(empty);
    }
    
    private void initFilterListeners() {
        List.of(methodFilter, statusFilter, commissionFilter)
                .forEach(filter -> filter.getButtons().forEach(button ->
                        button.selectedProperty().addListener(
                                (observable, oldValue, newValue) -> updateFilter()
                        )
                ));
    }

    // Applies all active filters and clears the selection if its event becomes hidden.
    private void updateFilter() {
        filteredEvents.setPredicate(row -> matchesFilter(methodFilter, row.getMethodType())
                                    && matchesFilter(statusFilter, row.getStatus())
                                    && matchesFilter(commissionFilter, row.getCommissionPolicy())); // Apply every filter group

        EventTableRow selectedEvent = findVisibleEvent(selectedEventId); // Revalidate the current selection
        selectedEventId = selectedEvent == null ? null : selectedEvent.getEventId();
        rebuildEventCards();
        showEventDetails(selectedEvent);
    }

    // A filter matches when no option is selected or one selected value equals the row value.
    private boolean matchesFilter(SegmentedButton filter, String rowValue) {
        boolean noFilterSelected = filter.getButtons().stream().noneMatch(ToggleButton::isSelected);
        boolean filterSelected = filter.getButtons().stream()
                .filter(ToggleButton::isSelected)
                .anyMatch(button -> button.getText().equals(rowValue));
        return noFilterSelected || filterSelected;
    }

    @Override
    public void close() {
        if (eventsRefresher != null && timer != null) {
            eventsRefresher.cancel();
            timer.cancel();
        }
    }
}
