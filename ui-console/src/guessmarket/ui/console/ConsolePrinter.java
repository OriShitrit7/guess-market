package guessmarket.ui.console;

import guessmarket.dto.CommissionPolicyDto;
import guessmarket.dto.EventStatusDto;
import guessmarket.dto.EventSummaryDto;
import guessmarket.dto.EventTradingStateDto;
import guessmarket.dto.OptionStateDto;
import guessmarket.dto.PurchaseResultDto;
import guessmarket.dto.TradeDto;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ConsolePrinter {
    private static final int OUTPUT_WIDTH = 80;
    private static final int TABLE_COLUMN_WIDTH = 19;

    private static final String HEADER_SEPARATOR = "=".repeat(OUTPUT_WIDTH);
    private static final String SECTION_SEPARATOR = "-".repeat(OUTPUT_WIDTH);
    private static final String NEW_LINE = System.lineSeparator();
    private static final String CURRENT_MARKET_ROW_FORMAT = "%-" + TABLE_COLUMN_WIDTH + "s %" + TABLE_COLUMN_WIDTH + "s %" + TABLE_COLUMN_WIDTH + "s";
    private static final String TRADE_ROW_FORMAT = "%-" + TABLE_COLUMN_WIDTH + "s %-" + TABLE_COLUMN_WIDTH + "s %" + TABLE_COLUMN_WIDTH + "s %" + TABLE_COLUMN_WIDTH + "s";

    public void showMenu() {
        String menu = String.join(NEW_LINE, "", buildHeader("GUESS MARKET"), "", "MAIN MENU",
                SECTION_SEPARATOR, buildMenuOptions(), HEADER_SEPARATOR, "");
        System.out.print(menu);
    }

    public void printMenuPrompt() {
        System.out.printf("Please choose an option (1-%d): ", MenuCommand.values().length);
    }

    public void printFilePathPrompt() {
        System.out.print("Please enter the full path to the system XML file: ");
    }

    public void printLoadSuccessMessage(int eventCount) {
        System.out.printf(
                "The file is valid and was loaded successfully. %d events are now available.%n", eventCount);
    }

    public void printErrorMessage(String message) {
        System.out.println(message);
    }

    public void printNoFileLoadedError() {
        System.out.println("No system file has been loaded yet. Please use option 1 first.");
    }

    public void printNoActiveEventsMessage() {
        String message = String.join(NEW_LINE, "", HEADER_SEPARATOR, "EVENTS", HEADER_SEPARATOR, "",
                "No active events are currently available.", "", HEADER_SEPARATOR, "");
        System.out.print(message);
    }

    public void printEventChoicePrompt() {
        System.out.print("Please enter the ID of the event you want to choose: ");
    }

    public void printUnknownEventIdError(int eventId) {
        System.out.printf(
                "Invalid input: no event with ID %d appears in the list above. Please try again.%n", eventId);
    }

    public void printOptionChoicePrompt(int optionCount) {
        System.out.printf("Please choose an option (1-%d): ", optionCount);
    }

    public void printQuantityPrompt() {
        System.out.print("Please enter the number of shares to buy: ");
    }

    public void printGoodbyeMessage() {
        System.out.println("Goodbye!");
    }

    public void printEvents(List<EventSummaryDto> events) {
        String eventCards = events.isEmpty() ? "No events are available." : buildEventCards(events);
        String output = String.join(NEW_LINE, "", buildHeader("MARKET BOARD"), "", eventCards,
                HEADER_SEPARATOR, "");
        System.out.print(output);
    }

    public void printEventTradingState(EventTradingStateDto state) {
        System.out.print(buildEventTradingState(state));
    }

    public void printOptionStates(List<OptionStateDto> options) {
        System.out.print(NEW_LINE + buildCurrentMarket(options) + NEW_LINE);
    }

    public void printPurchaseResult(PurchaseResultDto result) {
        System.out.print(buildPurchaseReceipt(result) + buildEventTradingState(result.getStateAfterPurchase()));
    }

    public void printOptions(List<String> optionNames) {
        System.out.print(String.join(NEW_LINE, "", "OPTIONS", SECTION_SEPARATOR,
                buildOptionRows(optionNames), SECTION_SEPARATOR, ""));
    }

    public void printWinningOptionHeader() {
        System.out.print(String.join(NEW_LINE, "", "SELECT THE WINNING OPTION", ""));
    }

    private String buildMenuOptions() {
        List<String> menuOptions = Arrays.stream(MenuCommand.values())
                .map(MenuCommand::toString)
                .collect(Collectors.toList());
        return String.join(NEW_LINE, menuOptions);
    }

    private String buildEventTradingState(EventTradingStateDto state) {
        String sections = String.join(NEW_LINE + NEW_LINE, buildHeader("EVENT TRADING STATE"),
                buildEventSummary(state.getSummary()), buildCurrentMarket(state.getOptions()),
                buildEventAccount(state), buildTradeHistory(state.getTradeHistory()));
        String finalResult = state.getWinningOptionName() == null ? ""
                : NEW_LINE + buildFinalResult(state.getWinningOptionName()) + NEW_LINE;

        return NEW_LINE + sections + NEW_LINE + finalResult + HEADER_SEPARATOR + NEW_LINE;
    }

    private String buildPurchaseReceipt(PurchaseResultDto result) {
        TradeDto trade = result.getTrade();
        EventSummaryDto event = result.getStateAfterPurchase().getSummary();
        double totalPaid = trade.getSharesCost() + trade.getCommissionCost();
        String purchase = String.format("[%s] %d shares purchased", trade.getOptionName(), trade.getQuantity());
        String sharesCost = formatLabelAndValue("Shares cost", formatMoney(trade.getSharesCost()));
        String commissionDetails = String.format("%d%% %s", event.getCommissionPercent(),
                toDisplayText(event.getCommissionPolicy()));
        String commissionValue = String.format("%s (%s)", formatMoney(trade.getCommissionCost()), commissionDetails);
        String commission = formatLabelAndValue("Commission", commissionValue);
        String total = formatLabelAndValue("Total paid", formatMoney(totalPaid));

        return String.join(NEW_LINE, "", buildHeader("PURCHASE COMPLETED"), "", purchase, "", sharesCost,
                commission, total, "", HEADER_SEPARATOR, "");
    }

    private String buildEventCards(List<EventSummaryDto> events) {
        List<String> eventCards = events.stream()
                .map(this::buildEventSummary)
                .collect(Collectors.toList());

        return String.join(NEW_LINE + NEW_LINE, eventCards);
    }

    private String buildEventSummary(EventSummaryDto event) {
        return buildEventDetails(formatEventTitle(event), event);
    }

    private String buildEventDetails(String title, EventSummaryDto event) {
        String eventId = String.format("Event ID   : %d", event.getEventId());
        String commission = String.format("Commission : %d%% (%s)", event.getCommissionPercent(),
                toDisplayText(event.getCommissionPolicy()));

        return String.join(NEW_LINE, title, SECTION_SEPARATOR, event.getDescription(), "",
                "OPTIONS", buildOptionRows(event.getOptionNames()), "", eventId, commission);
    }

    private String formatEventTitle(EventSummaryDto event) {
        return formatTitleRow(event.getEventName(), event);
    }

    private String formatTitleRow(String eventDetails, EventSummaryDto event) {
        String status = "[" + toDisplayText(event.getStatus()) + "]";
        int spaceCount = Math.max(1, OUTPUT_WIDTH - eventDetails.length() - status.length());
        return eventDetails + " ".repeat(spaceCount) + status;
    }

    private String buildOptionRows(List<String> optionNames) {
        List<String> rows = IntStream.range(0, optionNames.size())
                .mapToObj(index -> String.format("  [%d] %s", index + 1, optionNames.get(index)))
                .collect(Collectors.toList());

        return String.join(NEW_LINE, rows);
    }

    private String buildCurrentMarket(List<OptionStateDto> options) {
        return String.join(NEW_LINE, "CURRENT MARKET", SECTION_SEPARATOR,
                formatCurrentMarketRow("Option", "Value", "Shares bought"), SECTION_SEPARATOR,
                buildCurrentMarketRows(options), SECTION_SEPARATOR);
    }

    private String buildCurrentMarketRows(List<OptionStateDto> options) {
        List<String> rows = IntStream.range(0, options.size())
                .mapToObj(index -> { OptionStateDto option = options.get(index);
            String optionName = String.format("[%d] %s", index + 1, option.getOptionName());
            return formatCurrentMarketRow(optionName, formatDecimal(option.getCurrentValue()),
                    String.valueOf(option.getTotalSharesBought()));
        }).collect(Collectors.toList());
        return String.join(NEW_LINE, rows);
    }

    private String formatCurrentMarketRow(String option, String value, String shares) {
        return String.format(CURRENT_MARKET_ROW_FORMAT, option, value, shares);
    }

    private String buildEventAccount(EventTradingStateDto state) {
        return String.join(NEW_LINE, "EVENT ACCOUNT", SECTION_SEPARATOR,
                formatRightAlignedValue("Balance", formatMoney(state.getAccountBalance())),
                formatRightAlignedValue("Commission collected", formatMoney(state.getTotalCommissionCollected())));
    }

    private String buildTradeHistory(List<TradeDto> trades) {
        if (trades.isEmpty()) {
            return String.join(NEW_LINE, "TRADE HISTORY - NEWEST FIRST", SECTION_SEPARATOR,
                    "No trades have been made yet.", SECTION_SEPARATOR);
        }

        return String.join(NEW_LINE, "TRADE HISTORY - NEWEST FIRST", SECTION_SEPARATOR,
                formatTradeRow("No.", "Option", "Shares", "Paid"), SECTION_SEPARATOR,
                buildTradeRows(trades), SECTION_SEPARATOR);
    }

    private String buildTradeRows(List<TradeDto> trades) {
        List<String> rows = IntStream.range(0, trades.size()).mapToObj(index -> {
            TradeDto trade = trades.get(index);
            double totalPaid = trade.getSharesCost() + trade.getCommissionCost();
            String tradeNumber = String.format("%d.", index + 1);

            return formatTradeRow(tradeNumber, trade.getOptionName(), String.valueOf(trade.getQuantity()),
                    formatMoney(totalPaid));
        }).collect(Collectors.toList());
        return String.join(NEW_LINE, rows);
    }

    private String formatTradeRow(String number, String option, String shares, String paid) {
        return String.format(TRADE_ROW_FORMAT, number, option, shares, paid);
    }

    private String buildFinalResult(String winningOptionName) {
        return String.join(NEW_LINE, "FINAL RESULT", SECTION_SEPARATOR,
                formatRightAlignedValue("Winning option", winningOptionName));
    }

    private String buildHeader(String title) {
        int leftPadding = Math.max(0, (OUTPUT_WIDTH - title.length()) / 2);
        return String.join(NEW_LINE, HEADER_SEPARATOR, " ".repeat(leftPadding) + title, HEADER_SEPARATOR);
    }

    private String formatRightAlignedValue(String label, String value) {
        int spaceCount = Math.max(1, OUTPUT_WIDTH - label.length() - value.length());
        return label + " ".repeat(spaceCount) + value;
    }

    private String formatLabelAndValue(String label, String value) {
        return String.format("%-12s: %s", label, value);
    }

    private String formatDecimal(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private String formatMoney(double value) {
        return String.format(Locale.ROOT, "$%.2f", value);
    }

    private String toDisplayText(CommissionPolicyDto policy) {
        return switch (policy) {
            case ON_PURCHASE -> "on-purchase";
            case ON_CLOSE -> "on-close";
        };
    }

    private String toDisplayText(EventStatusDto status) {
        return switch (status) {
            case ACTIVE -> "ACTIVE";
            case CLOSED -> "CLOSED";
        };
    }
}
