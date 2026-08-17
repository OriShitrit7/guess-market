package guessmarket.ui.console;

import guessmarket.engine.api.EventSummary;
import guessmarket.engine.api.MarketManager;
import guessmarket.engine.api.MarketManagerImpl;
import guessmarket.engine.model.CommissionPolicy;
import guessmarket.engine.model.EventStatus;

import java.util.List;

public class GuessMarketUI {

    public static void main(String[] args) {
        MarketManager engine = new MarketManagerImpl();

        showEvents(engine.getEventSummaries());
    }

    private static void showEvents(List<EventSummary> summaries) {
        if (summaries.isEmpty()) {
            System.out.println("There are no events in the system.");
            return;
        }

        for (int i = 0; i < summaries.size(); i++) {
            showEvent(i + 1, summaries.get(i));
        }
    }

    private static void showEvent(int number, EventSummary summary) {
        System.out.println(number + ". " + summary.getEventName());
        System.out.println("   Description: " + summary.getDescription());
        System.out.println("   Commission:  " + summary.getCommissionPercent() + "% ("
                + describePolicy(summary.getCommissionPolicy()) + ")");
        System.out.println("   Options:     " + describeOptions(summary.getOptionNames()));
        System.out.println("   Status:      " + describeStatus(summary.getStatus()));
        System.out.println();
    }

    private static String describeOptions(List<String> optionNames) {
        StringBuilder options = new StringBuilder();

        for (int i = 0; i < optionNames.size(); i++) {
            if (i > 0) {
                options.append("   ");
            }
            options.append(i + 1).append(") ").append(optionNames.get(i));
        }
        return options.toString();
    }

    private static String describePolicy(CommissionPolicy policy) {
        return policy == CommissionPolicy.ON_PURCHASE ? "collected on purchase" : "collected on close";
    }

    private static String describeStatus(EventStatus status) {
        return status == EventStatus.ACTIVE ? "Active" : "Closed";
    }
}
