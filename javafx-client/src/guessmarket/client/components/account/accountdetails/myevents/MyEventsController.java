package guessmarket.client.components.account.accountdetails.myevents;

import com.google.gson.reflect.TypeToken;
import guessmarket.client.components.events.EventsRefresher;
import guessmarket.client.util.Constants;
import guessmarket.client.util.http.HttpClientUtil;
import guessmarket.dto.EventParticipationDto;
import guessmarket.dto.EventStatusDto;
import guessmarket.dto.EventSummaryDto;
import guessmarket.dto.LmsrStateDto;
import guessmarket.dto.NewEventDto;
import guessmarket.dto.OrderBookStateDto;
import guessmarket.dto.TradingMethodTypeDto;
import guessmarket.dto.UserInvolvementDto;
import guessmarket.dto.UserSummaryDto;
import guessmarket.dto.VersionedDto;
import guessmarket.client.components.account.accountdetails.myevents.lmsrtrade.LmsrTradeController;
import guessmarket.client.components.account.accountdetails.myevents.orderbooktrade.OrderBookTradeController;
import guessmarket.client.components.dialogs.Dialogs;
import guessmarket.client.components.account.dialogs.createevent.CreateEventDialogController;
import guessmarket.client.components.account.dialogs.tradeinevent.TradeInEventDialogController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

// Shows the logged-in user's events and the trading panel of the selected one, and opens the
// Create Event and Trade in New Event dialogs.
public class MyEventsController {
    private static final String TRADE_IN_EVENT_DIALOG = "/guessmarket/client/components/account/dialogs/tradeinevent/trade-in-event-dialog.fxml";
    private static final String TRADE_IN_NEW_EVENT = "Trade in New Event";
    private static final String CREATE_EVENT_DIALOG = "/guessmarket/client/components/account/dialogs/createevent/create-event-dialog.fxml";
    private static final String CREATE_EVENT = "Create Event";
    private static final String CREATE_FAILED = "Event Not Created";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    private static final String NO_EVENTS = "You are not taking part in any event yet";
    private static final int MAX_VISIBLE_USER_EVENT_ROWS = 3;
    private static final double USER_EVENT_ROW_HEIGHT = 44.0;
    private static final double USER_EVENT_HEADER_HEIGHT = 44.0;
    private static final double USER_EVENT_TABLE_BORDER_HEIGHT = 2.0;

    // The event the details panel is showing, which is not always a row in the events table.
    private Integer displayedEventId;
    private boolean displayedEventOrderBook;
    private boolean displayedEventMarketMaker;
    // Versions of the displayed event's state and of the user's position in it, as last received.
    private int displayedStateVersion = -1;
    private int displayedPositionVersion = -1;
    private UserSummaryDto user;
    // Set while the table is rebuilt, so that the selection listener does not clear the displayed event.
    private boolean refreshingTable;

    @FXML
    private Button createEventButton;
    @FXML
    private Button tradeInNewEventButton;

    @FXML
    private TableView<UserEventTableRow> userEventsTable;
    @FXML
    private TableColumn<UserEventTableRow, String> userEventNameColumn;
    @FXML
    private TableColumn<UserEventTableRow, String> userEventStatusColumn;
    @FXML
    private TableColumn<UserEventTableRow, String> userEventMethodColumn;
    @FXML
    private TableColumn<UserEventTableRow, String> userEventRoleColumn;

    @FXML
    private TitledPane eventDetailsPane;
    @FXML
    private VBox noEventSelectedState;
    @FXML
    private VBox lmsrTradeComponent;
    @FXML
    private LmsrTradeController lmsrTradeComponentController;
    @FXML
    private VBox orderBookComponent;
    @FXML
    private OrderBookTradeController orderBookComponentController;

    private final ObservableList<UserEventTableRow> userEvents = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        userEventsTable.setPlaceholder(new Label(NO_EVENTS));

        noEventSelectedState.managedProperty().bind(noEventSelectedState.visibleProperty());
        lmsrTradeComponent.managedProperty().bind(lmsrTradeComponent.visibleProperty());
        orderBookComponent.managedProperty().bind(orderBookComponent.visibleProperty());

        initUserEventsTable();
        initSelectionListeners();
        createEventButton.setDisable(true);
        tradeInNewEventButton.setDisable(true);
        clearEventDetails();
    }

    private void initUserEventsTable() {
        userEventNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        userEventStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        userEventMethodColumn.setCellValueFactory(new PropertyValueFactory<>("methodType"));
        userEventRoleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        userEventsTable.setFixedCellSize(USER_EVENT_ROW_HEIGHT);
        userEventsTable.setItems(userEvents);
        updateUserEventsTableHeight();
    }

    private void initSelectionListeners() {
        userEventsTable.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
            if (!refreshingTable) {
                showEventDetails(newValue);
            }
        });
    }

    // Refreshes the events table and the displayed event with the data of the latest pull.
    public void update(UserSummaryDto summary, List<EventParticipationDto> participations) {
        user = summary;
        tradeInNewEventButton.setDisable(summary.isBlocked());
        createEventButton.setDisable(summary.isBlocked());

        refreshingTable = true;
        userEvents.setAll(participations.stream().map(UserEventTableRow::new).toList());
        updateUserEventsTableHeight();
        restoreEventSelection(displayedEventId); // Restore the event after its table is rebuilt
        refreshingTable = false;

        // The panel shows the user's balance and block state with the event, so it is drawn again on the next pull
        displayedStateVersion = -1;
    }

    private void restoreEventSelection(Integer eventId) {
        if (eventId == null) {
            return;
        }
        userEvents.stream().filter(event -> event.getEventId() == eventId).findFirst()
                .ifPresent(event -> userEventsTable.getSelectionModel().select(event));
    }

    // The logged-in user creates the event and becomes its market maker.
    @FXML
    private void createEventButtonAction() {
        Dialogs.<CreateEventDialogController>show(getClass(), CREATE_EVENT_DIALOG, CREATE_EVENT,
                        _ -> { })
                .ifPresent(dialog -> createEvent(dialog.getRequest()));
    }

    private void createEvent(NewEventDto newEvent) {
        RequestBody body = RequestBody.create(Constants.GSON_INSTANCE.toJson(newEvent), JSON_MEDIA_TYPE);
        Request request = new Request.Builder().url(Constants.CREATE_EVENT_URL).post(body).build();

        HttpClientUtil.runAsyncOrShowError(request, CREATE_FAILED);
    }

    // Fetches the current event list, and lets the user pick an active event they have not traded in yet.
    @FXML
    private void tradeInNewEventButtonAction() {
        HttpClientUtil.runAsync(Constants.EVENTS_URL, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> Dialogs.showError(TRADE_IN_NEW_EVENT, Constants.SERVER_UNREACHABLE));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try (response) {
                    String body = response.body().string();

                    if (!response.isSuccessful()) {
                        Platform.runLater(() -> Dialogs.showError(TRADE_IN_NEW_EVENT, body.trim()));
                        return;
                    }
                    List<EventSummaryDto> allEvents = EventsRefresher.parseEventSummaries(body).getData();

                    Platform.runLater(() -> showTradeInEventDialog(availableEventsFrom(allEvents)));
                }
            }
        });
    }

    private void showTradeInEventDialog(List<EventSummaryDto> availableEvents) {
        Dialogs.<TradeInEventDialogController>show(getClass(), TRADE_IN_EVENT_DIALOG, TRADE_IN_NEW_EVENT, dialog -> dialog.setEvents(availableEvents))
                .ifPresent(dialog -> showNewEventDetails(dialog.getSelectedEvent()));
    }

    // Sizes the table for up to three event rows and avoids unused empty space.
    private void updateUserEventsTableHeight() {
        int visibleRows = Math.max(1, Math.min(MAX_VISIBLE_USER_EVENT_ROWS, userEvents.size()));
        double tableHeight = USER_EVENT_HEADER_HEIGHT
                + visibleRows * USER_EVENT_ROW_HEIGHT
                + USER_EVENT_TABLE_BORDER_HEIGHT;

        userEventsTable.setPrefHeight(tableHeight);
    }

    // An event is available for a first trade only while active and unrelated to the logged-in user.
    private List<EventSummaryDto> availableEventsFrom(List<EventSummaryDto> allEvents) {
        Set<Integer> relatedEventIds = new HashSet<>();

        userEvents.stream().map(UserEventTableRow::getEventId).forEach(relatedEventIds::add);

        return allEvents.stream()
                .filter(event -> event.getStatus() == EventStatusDto.ACTIVE)
                .filter(event -> !relatedEventIds.contains(event.getEventId()))
                .toList();
    }

    // An event picked in Trade in New Event is not in the table yet, so the table selection is cleared.
    private void showNewEventDetails(EventSummaryDto selectedEvent) {
        userEventsTable.getSelectionModel().clearSelection();
        showEventDetails(selectedEvent.getEventId(), selectedEvent.getMethodType() == TradingMethodTypeDto.ORDER_BOOK, false);
    }

    // Shows the panel that matches the selected event's trading method, and hides the other one.
    private void showEventDetails(UserEventTableRow selectedEvent) {
        if (selectedEvent == null) {
            clearEventDetails();
            return;
        }
        showEventDetails(selectedEvent.getEventId(), selectedEvent.isOrderBook(), selectedEvent.isMarketMaker());
    }

    // Switching to another event clears the panel first, so the previous event's data is not shown
    // while the new data loads.
    private void showEventDetails(int eventId, boolean orderBook, boolean marketMaker) {
        if (!isDisplayed(eventId)) {
            clearEventDetails();
        }
        displayedEventId = eventId;
        displayedEventOrderBook = orderBook;
        displayedEventMarketMaker = marketMaker;

        noEventSelectedState.setVisible(false);
        eventDetailsPane.setExpanded(true);
        lmsrTradeComponent.setVisible(!orderBook);
        orderBookComponent.setVisible(orderBook);

        refreshDisplayedEvent();
    }

    private void clearEventDetails() {
        displayedEventId = null;
        displayedStateVersion = -1;
        displayedPositionVersion = -1;
        noEventSelectedState.setVisible(true);
        lmsrTradeComponent.setVisible(false);
        orderBookComponent.setVisible(false);

        lmsrTradeComponentController.clear();
        orderBookComponentController.clear();
    }

    private boolean isDisplayed(int eventId) {
        return displayedEventId != null && displayedEventId == eventId;
    }

    // The event's state and the user's position come from two separate requests, and each one
    // updates its own part of the panel. The server answers only when the event has changed.
    public void refreshDisplayedEvent() {
        if (displayedEventId == null) {
            return;
        }
        int eventId = displayedEventId;

        requestEventData(Constants.EVENT_STATE_URL, eventId, displayedStateVersion, json -> showEventState(eventId, json));
        requestEventData(Constants.EVENT_POSITION_URL, eventId, displayedPositionVersion,
                json -> showEventPosition(eventId, json));
    }

    private void requestEventData(String url, int eventId, int version, Consumer<String> jsonConsumer) {
        String finalUrl = HttpUrl.parse(url).newBuilder()
                .addQueryParameter(Constants.EVENT_ID_PARAMETER, String.valueOf(eventId))
                .addQueryParameter(Constants.VERSION_PARAMETER, String.valueOf(version))
                .build().toString();

        HttpClientUtil.runAsyncIgnoringErrors(finalUrl, jsonConsumer);
    }

    // A response may arrive after another event was selected; such a response is ignored.
    private void showEventState(int eventId, String json) {
        if (!isDisplayed(eventId)) {
            return;
        }
        Class<?> stateClass = displayedEventOrderBook ? OrderBookStateDto.class : LmsrStateDto.class;
        Type type = TypeToken.getParameterized(VersionedDto.class, stateClass).getType();
        VersionedDto<?> versioned = Constants.GSON_INSTANCE.fromJson(json, type);

        displayedStateVersion = versioned.getVersion();
        if (versioned.getData() instanceof OrderBookStateDto orderBookState) {
            orderBookComponentController.showState(orderBookState, user, displayedEventMarketMaker);
        } else if (versioned.getData() instanceof LmsrStateDto lmsrState) {
            lmsrTradeComponentController.showState(lmsrState, user, displayedEventMarketMaker);
        }
    }

    private void showEventPosition(int eventId, String json) {
        if (!isDisplayed(eventId)) {
            return;
        }
        Type type = new TypeToken<VersionedDto<UserInvolvementDto>>() {}.getType();
        VersionedDto<UserInvolvementDto> versioned = Constants.GSON_INSTANCE.fromJson(json, type);
        UserInvolvementDto position = versioned.getData();

        displayedPositionVersion = versioned.getVersion();

        if (displayedEventOrderBook) {
            orderBookComponentController.showPosition(position);
        } else {
            lmsrTradeComponentController.showPosition(position);
        }
    }
}
