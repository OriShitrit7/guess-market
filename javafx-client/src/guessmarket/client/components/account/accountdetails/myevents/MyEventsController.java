package guessmarket.client.components.account.accountdetails.myevents;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

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
    private UserSummaryDto user;
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

    public void update(UserSummaryDto summary, List<EventParticipationDto> participations) {
        user = summary;
        tradeInNewEventButton.setDisable(summary.isBlocked());
        createEventButton.setDisable(summary.isBlocked());

        refreshingTable = true;
        userEvents.setAll(participations.stream().map(UserEventTableRow::new).toList());
        updateUserEventsTableHeight();
        restoreEventSelection(displayedEventId); // Restore the event after its table is rebuilt
        refreshingTable = false;

        refreshDisplayedEvent();
    }

    private void restoreEventSelection(Integer eventId) {
        if (eventId == null) {
            return;
        }
        userEvents.stream().filter(event -> event.getEventId() == eventId).findFirst()
                .ifPresent(event -> userEventsTable.getSelectionModel().select(event));
    }

    // The selected user creates the event and becomes its market maker.
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

    @FXML
    private void tradeInNewEventButtonAction() {
        HttpClientUtil.runAsync(Constants.EVENTS_URL, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> Dialogs.showError(TRADE_IN_NEW_EVENT, "Something went wrong: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try (response) {
                    String body = response.body().string();

                    if (!response.isSuccessful()) {
                        Platform.runLater(() -> Dialogs.showError(TRADE_IN_NEW_EVENT, body.trim()));
                        return;
                    }
                    List<EventSummaryDto> allEvents = EventsRefresher.parseEventSummaries(body);

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

    // An event is available for a first trade only while active and unrelated to the selected user.
    private List<EventSummaryDto> availableEventsFrom(List<EventSummaryDto> allEvents) {
        Set<Integer> relatedEventIds = new HashSet<>();

        userEvents.stream().map(UserEventTableRow::getEventId).forEach(relatedEventIds::add);

        return allEvents.stream()
                .filter(event -> event.getStatus() == EventStatusDto.ACTIVE)
                .filter(event -> !relatedEventIds.contains(event.getEventId()))
                .toList();
    }

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
        noEventSelectedState.setVisible(true);
        lmsrTradeComponent.setVisible(false);
        orderBookComponent.setVisible(false);

        lmsrTradeComponentController.clear();
        orderBookComponentController.clear();
    }

    private boolean isDisplayed(int eventId) {
        return displayedEventId != null && displayedEventId == eventId;
    }

    private void refreshDisplayedEvent() {
        if (displayedEventId == null) {
            return;
        }
        int eventId = displayedEventId;

        requestEventData(Constants.EVENT_STATE_URL, eventId, json -> showEventState(eventId, json));
        requestEventData(Constants.EVENT_POSITION_URL, eventId, json -> showEventPosition(eventId, json));
    }

    private void requestEventData(String url, int eventId, Consumer<String> jsonConsumer) {
        String finalUrl = HttpUrl.parse(url).newBuilder()
                .addQueryParameter(Constants.EVENT_ID_PARAMETER, String.valueOf(eventId))
                .build().toString();

        HttpClientUtil.runAsyncIgnoringErrors(finalUrl, jsonConsumer);
    }

    private void showEventState(int eventId, String json) {
        if (!isDisplayed(eventId)) {
            return;
        }
        if (displayedEventOrderBook) {
            orderBookComponentController.showState(Constants.GSON_INSTANCE.fromJson(json, OrderBookStateDto.class), user, displayedEventMarketMaker);
        } else {
            lmsrTradeComponentController.showState(Constants.GSON_INSTANCE.fromJson(json, LmsrStateDto.class), user, displayedEventMarketMaker);
        }
    }

    private void showEventPosition(int eventId, String json) {
        if (!isDisplayed(eventId)) {
            return;
        }
        UserInvolvementDto position = Constants.GSON_INSTANCE.fromJson(json, UserInvolvementDto.class);

        if (displayedEventOrderBook) {
            orderBookComponentController.showPosition(position);
        } else {
            lmsrTradeComponentController.showPosition(position);
        }
    }
}
