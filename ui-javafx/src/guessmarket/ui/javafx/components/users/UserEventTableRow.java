package guessmarket.ui.javafx.components.users;

import guessmarket.dto.EventParticipationDto;
import guessmarket.dto.EventSummaryDto;
import guessmarket.ui.javafx.components.DisplayText;
import javafx.beans.property.SimpleStringProperty;

public class UserEventTableRow {
    private static final String MARKET_MAKER = "Market Maker";
    private static final String PARTICIPANT = "Participant";
    private static final String ROLE_SEPARATOR = ", ";

    private final int eventId;
    private final boolean marketMaker;

    private final SimpleStringProperty name;
    private final SimpleStringProperty status;
    private final SimpleStringProperty methodType;
    private final SimpleStringProperty role;

    public UserEventTableRow(EventParticipationDto participation) {
        EventSummaryDto event = participation.getEvent();

        this.eventId = event.getEventId();
        this.marketMaker = participation.isMarketMaker();
        this.name = new SimpleStringProperty(event.getEventName());
        this.status = new SimpleStringProperty(DisplayText.of(event.getStatus()));
        this.methodType = new SimpleStringProperty(DisplayText.of(event.getMethodType()));
        this.role = new SimpleStringProperty(toDisplayText(participation));
    }

    public int getEventId() {
        return eventId;
    }

    public boolean isMarketMaker() {
        return marketMaker;
    }

    public String getName() {
        return name.get();
    }

    public String getStatus() {
        return status.get();
    }

    public String getMethodType() {
        return methodType.get();
    }

    public String getRole() {
        return role.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public SimpleStringProperty statusProperty() {
        return status;
    }

    public SimpleStringProperty methodTypeProperty() {
        return methodType;
    }

    public SimpleStringProperty roleProperty() {
        return role;
    }

    private static String toDisplayText(EventParticipationDto participation) {
        if (participation.isMarketMaker() && participation.isParticipant()) {
            return MARKET_MAKER + ROLE_SEPARATOR + PARTICIPANT;
        }
        return participation.isMarketMaker() ? MARKET_MAKER : PARTICIPANT;
    }
}
