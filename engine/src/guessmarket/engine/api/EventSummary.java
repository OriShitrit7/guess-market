package guessmarket.engine.api;

import guessmarket.engine.model.CommissionPolicy;
import guessmarket.engine.model.EventStatus;

import java.util.List;

public class EventSummary {
    private final int eventId;
    private final String eventName;
    private final String description;
    private final int commissionPercent;
    private final CommissionPolicy commissionPolicy;
    private final List<String> optionNames;
    private final EventStatus status;

    public EventSummary(int eventId, String eventName, String description, int commissionPercent,
                        CommissionPolicy commissionPolicy, List<String> optionNames, EventStatus status) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.description = description;
        this.commissionPercent = commissionPercent;
        this.commissionPolicy = commissionPolicy;
        this.optionNames = List.copyOf(optionNames);
        this.status = status;
    }

    public int getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public String getDescription() {
        return description;
    }

    public int getCommissionPercent() {
        return commissionPercent;
    }

    public CommissionPolicy getCommissionPolicy() {
        return commissionPolicy;
    }

    public List<String> getOptionNames() {
        return optionNames;
    }

    public EventStatus getStatus() {
        return status;
    }
}
