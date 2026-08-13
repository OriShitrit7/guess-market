package guessmarket.engine.api;

import guessmarket.engine.model.MarketEvent;
import guessmarket.engine.model.MarketOption;
import guessmarket.engine.model.Trade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MarketManagerImpl implements MarketManager {

    private Map<Integer, MarketEvent> events = new LinkedHashMap<>();

    @Override
    public List<EventSummary> getEventSummaries() {
        List<EventSummary> summaries = new ArrayList<>();

        for (MarketEvent event : events.values()) {
            summaries.add(createEventSummary(event));
        }
        return summaries;
    }

    @Override
    public EventTradingState getEventTradingState(int eventId) {
        return createEventTradingState(getEvent(eventId));
    }

    @Override
    public void buyShares(int eventId, int optionIndex) {

    }

    private MarketEvent getEvent(int id) {
        return events.get(id);
    }

    private EventSummary createEventSummary(MarketEvent event) {
        List<String> optionNames = new ArrayList<>();

        for (MarketOption option : event.getOptions()) {
            optionNames.add(option.getName());
        }

        return new EventSummary(event.getEventId(), event.getEventName(), event.getDescription(),
                event.getCommissionConfig().getCommissionPercent(), event.getCommissionConfig().getCommissionPolicy(),
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
            infos.add(new TradeInfo(trade.getOption().getName(), trade.getQuantity(),
                    trade.getSharesCost(), trade.getCommissionCost()));
        }
        Collections.reverse(infos);
        return infos;
    }
}
