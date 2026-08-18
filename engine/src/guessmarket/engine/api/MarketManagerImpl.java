package guessmarket.engine.api;

import guessmarket.engine.exception.InvalidFileException;
import guessmarket.engine.model.*;
import guessmarket.engine.trading.LmsrTradingMethod;
import guessmarket.engine.xml.SystemFileLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MarketManagerImpl implements MarketManager {

    private Map<Integer, MarketEvent> events = new LinkedHashMap<>();
    private final SystemFileLoader loader = new SystemFileLoader();

    public MarketManagerImpl() {
        seedHardcodedEvents();
    }

    // TEMPORARY scaffolding so command 2 can be run before the XML loader exists.
    // Delete this method and the constructor once loading from file works.
    private void seedHardcodedEvents() {
        MarketEvent first = new MarketEvent(1, "Mujtaba is Dead",
                "This event gambles if Mujtaba is a live or not. it will be determined if he will be shown in public until 31.8.26",
                List.of(new MarketOption("Hell Yea !"), new MarketOption("No way !")),
                new CommissionConfig(5, CommissionPolicy.ON_PURCHASE),
                new LmsrTradingMethod(100));

        MarketEvent second = new MarketEvent(2, "World Cap Winner",
                "Who do you think will win the world cap ?",
                List.of(new MarketOption("Argentina"), new MarketOption("Spain")),
                new CommissionConfig(15, CommissionPolicy.ON_CLOSE),
                new LmsrTradingMethod(50));

        events.put(first.getEventId(), first);
        events.put(second.getEventId(), second);
    }
    //////////////////

    private MarketEvent getEventById(int id) {
        return events.get(id);
    }

    @Override
    public void loadSystemFile(String path) throws InvalidFileException {
        List<MarketEvent> loadedEvents = loader.load(path);

        processInitialSubsidies(loadedEvents);
        events = buildEventMap(loadedEvents);
    }

    @Override
    public List<EventSummary> getEventSummaries() {
        return events.values().stream()
                .map(this::createEventSummary)
                .toList();
    }
    // MarketManagerImpl
    @Override
    public List<EventSummary> getActiveEventSummaries() {
        return events.values().stream()
                .filter(event -> event.getStatus() == EventStatus.ACTIVE)
                .map(this::createEventSummary)
                .toList();
    }

    @Override
    public EventTradingState getEventTradingState(int eventId) {
        return createEventTradingState(getEventById(eventId));
    }

    @Override
    public TradeInfo buyShares(int eventId, int optionIndex, int quantity) {
        Trade trade = getEventById(eventId).buyShares(optionIndex, quantity);

        return createTradeInfo(trade);
    }

    @Override
    public void closeEvent(int eventId, int winningOptionIndex) {
        getEventById(eventId).close(winningOptionIndex);
    }

    private EventSummary createEventSummary(MarketEvent event) {
        List<String> optionNames = new ArrayList<>();

        for (MarketOption option : event.getOptions()) {
            optionNames.add(option.getName());
        }

        return new EventSummary(event.getEventId(), event.getEventName(), event.getDescription(),
                event.getCommissionPercentage(), event.getCommissionPolicy(),
                optionNames, event.getStatus());
    }

    private EventTradingState createEventTradingState(MarketEvent event) {
        MarketOption winner = event.getWinningOption();

        return new EventTradingState(createEventSummary(event), createOptionInfos(event),
                event.getAccount().getBalance(), event.getAccount().getTotalCommissionCollected(), createTradeInfos(event),
                winner == null ? null : winner.getName());
    }

    private List<OptionTradingInfo> createOptionInfos(MarketEvent event) {
        List<OptionTradingInfo> infos = new ArrayList<>();

        List<MarketOption> options = event.getOptions();
        double[] values = event.getOptionsValues();

        for (int i = 0; i < options.size(); i++) {
            infos.add(new OptionTradingInfo(options.get(i).getName(), values[i],
                    options.get(i).getTotalSharesBought()));
        }
        return infos;
    }

    private List<TradeInfo> createTradeInfos(MarketEvent event) {
        List<TradeInfo> infos = new ArrayList<>();

        for (Trade trade : event.getAccount().getTradeHistory()) {
            infos.add(createTradeInfo(trade));
        }
        Collections.reverse(infos);
        return infos;
    }

    private TradeInfo createTradeInfo(Trade trade) {
        return new TradeInfo(trade.getOption().getName(), trade.getQuantity(),
                trade.getSharesCost(), trade.getCommissionCost());
    }

    private void processInitialSubsidies(List<MarketEvent> loadedEvents) {
        loadedEvents.forEach(MarketEvent::processInitialSubsidy);
    }

    private Map<Integer, MarketEvent> buildEventMap(List<MarketEvent> loadedEvents) {
        Map<Integer, MarketEvent> eventMap = new LinkedHashMap<>();

        for (MarketEvent event : loadedEvents) {
            eventMap.put(event.getEventId(), event);
        }
        return eventMap;
    }
}
