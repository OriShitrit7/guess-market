package guessmarket.engine.xml;

import guessmarket.engine.model.MarketEvent;
import guessmarket.engine.model.UsersRegistry;

import java.util.List;

public class LoadedSystemData {
    private final List<MarketEvent> events;
    private final UsersRegistry usersRegistry;

    public LoadedSystemData(List<MarketEvent> events, UsersRegistry usersRegistry) {
        this.events = events;
        this.usersRegistry = usersRegistry;
    }

    public List<MarketEvent> getEvents() {
        return events;
    }

    public UsersRegistry getUsersRegistry() {
        return usersRegistry;
    }
}
