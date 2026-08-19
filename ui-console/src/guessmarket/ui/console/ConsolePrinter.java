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
import java.util.stream.IntStream;

// Formats and prints all text displayed by the console application.
public class ConsolePrinter {
    // Fixed widths keep headers, table columns and labelled values aligned throughout the UI.
    private static final int OUTPUT_WIDTH = 80;
    private static final int TABLE_COLUMN_WIDTH = 17;
    private static final int VALUE_ROW_WIDTH = 40;
    private static final int LABEL_WIDTH = 12;

    private static final String HEADER_SEPARATOR = "=".repeat(OUTPUT_WIDTH);
    private static final String SECTION_SEPARATOR = "-".repeat(OUTPUT_WIDTH);
    private static final String ERROR_TITLE = "ERROR";
    private static final String NEW_LINE = System.lineSeparator();

    private static final String COLUMN_SEPARATOR = " ";
    private static final String LEFT_COLUMN = "%-" + TABLE_COLUMN_WIDTH + "s";
    private static final String RIGHT_COLUMN = "%" + TABLE_COLUMN_WIDTH + "s";
    private static final String CURRENT_MARKET_ROW_FORMAT =
            String.join(COLUMN_SEPARATOR, LEFT_COLUMN, RIGHT_COLUMN, RIGHT_COLUMN);
    private static final String TRADE_ROW_FORMAT =
            String.join(COLUMN_SEPARATOR, LEFT_COLUMN, LEFT_COLUMN, RIGHT_COLUMN, RIGHT_COLUMN);
    private static final String LABEL_AND_VALUE_FORMAT = "%-" + LABEL_WIDTH + "s: %s";

    // Prints the main menu and its available commands.
    public void showMenu() {
        String menu = String.join(NEW_LINE, "", buildHeader("GUESS MARKET"), "", "MAIN MENU",
                SECTION_SEPARATOR, buildMenuOptions(), HEADER_SEPARATOR, "");
        System.out.print(menu);
    }

    // Prompts the user to select a menu command.
    public void printMenuPrompt() {
        System.out.printf("Please choose an option (1-%d): ", MenuCommand.values().length);
    }

    // Prompts the user for the full path of a system file.
    public void printFilePathPrompt() {
        System.out.print("Please enter the full path to the system XML file: ");
    }

    // Confirms a successful file load and displays the number of available events.
    public void printLoadSuccessMessage(int eventCount) {
        System.out.printf(
                "The file is valid and was loaded successfully. %d events are now available.%n", eventCount);
    }

    // Prints an error message supplied by the input or engine layers.
    public void printErrorMessage(String message) {
        System.out.println(buildErrorBlock(message));
    }

    // Explains that the requested command requires a loaded system file.
    public void printNoFileLoadedError() {
        System.out.println(buildErrorBlock("No system file has been loaded yet. Please use option 1 first."));
    }

    // Reports that no active event is available for the requested operation.
    public void printNoActiveEventsMessage() {
        String message = String.join(NEW_LINE, "", HEADER_SEPARATOR, "EVENTS", HEADER_SEPARATOR, "",
                "No active events are currently available.", "", HEADER_SEPARATOR, "");
        System.out.print(message);
    }

    // Prompts the user to select an event by its ID.
    public void printEventChoicePrompt() {
        System.out.print("Please enter the ID of the event you want to choose: ");
    }

    // Prompts the user to select an option from the displayed range.
    public void printOptionChoicePrompt(int optionCount) {
        System.out.printf("Please choose an option (1-%d): ", optionCount);
    }

    // Prompts the user for the number of shares to purchase.
    public void printQuantityPrompt() {
        System.out.print("Please enter the number of shares to buy: ");
    }

    // Prints the message shown when the application exits.
    public void printGoodbyeMessage() {
        System.out.println("Goodbye!");
    }

    // Prints summary cards for the supplied events.
    public void printEvents(List<EventSummaryDto> events) {
        String eventCards = events.isEmpty() ? "No events are available." : buildEventCards(events);
        String output = String.join(NEW_LINE, "", buildHeader("EVENTS OVERVIEW"), "", eventCards,
                HEADER_SEPARATOR, "");
        System.out.print(output);
    }

    // Prints the complete trading, account and history state of an event.
    public void printEventTradingState(EventTradingStateDto state) {
        System.out.print(buildEventTradingState(state));
    }

    // Prints the current values and purchased shares of the supplied options.
    public void printOptionStates(List<OptionStateDto> options) {
        System.out.print(NEW_LINE + buildCurrentMarket(options) + NEW_LINE);
    }

    // Prints a purchase receipt followed by the updated event state.
    public void printPurchaseResult(PurchaseResultDto result) {
        System.out.print(buildPurchaseReceipt(result) + buildEventTradingState(result.getStateAfterPurchase()));
    }

    // Prints a numbered option list for user selection.
    public void printOptions(List<String> optionNames) {
        System.out.print(String.join(NEW_LINE, "", "OPTIONS", SECTION_SEPARATOR,
                buildOptionRows(optionNames), SECTION_SEPARATOR, ""));
    }

    // Prints the heading used before choosing the winning option.
    public void printWinningOptionHeader() {
        System.out.print(String.join(NEW_LINE, "", "SELECT THE WINNING OPTION", ""));
    }

    // Frames every error the same way, so it stands out between two menu screens.
    private String buildErrorBlock(String message) {
        return String.join(NEW_LINE, "", ERROR_TITLE, SECTION_SEPARATOR, message, SECTION_SEPARATOR);
    }

    private String buildMenuOptions() {
        List<String> menuOptions = Arrays.stream(MenuCommand.values())
                .map(MenuCommand::toString)
                .toList();

        return String.join(NEW_LINE, menuOptions);
    }

    // Combines all event state sections and includes the final result only after closing.
    private String buildEventTradingState(EventTradingStateDto state) {
        String sections = String.join(NEW_LINE + NEW_LINE, buildHeader("EVENT TRADING STATE"),
                buildEventSummary(state.getSummary()), buildEventAccount(state),
                buildCurrentMarket(state.getOptions()), buildTradeHistory(state.getTradeHistory()));
        String finalResult = state.getWinningOptionName() == null ? ""
                : NEW_LINE + buildFinalResult(state.getWinningOptionName()) + NEW_LINE;

        return NEW_LINE + sections + NEW_LINE + finalResult + HEADER_SEPARATOR + NEW_LINE;
    }

    // Builds a receipt that separates share cost, commission and total payment.
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
                .toList();

        return String.join(NEW_LINE + NEW_LINE, eventCards);
    }

    private String buildEventSummary(EventSummaryDto event) {
        String eventId = String.format("Event ID   : %d", event.getEventId());
        String commission = String.format("Commission : %d%% (%s)", event.getCommissionPercent(),
                toDisplayText(event.getCommissionPolicy()));

        return String.join(NEW_LINE, formatTitleRow(event), SECTION_SEPARATOR,
                wrapToWidth(event.getDescription()), "", "OPTIONS", buildOptionRows(event.getOptionNames()),
                "", eventId, commission);
    }

    // Breaks a long text into lines that fit the display width, without splitting words.
    private String wrapToWidth(String text) {
        StringBuilder wrapped = new StringBuilder();
        int lineLength = 0;

        for (String word : text.trim().split("\\s+")) {
            if (lineLength > 0 && lineLength + 1 + word.length() > OUTPUT_WIDTH) {
                wrapped.append(NEW_LINE);
                lineLength = 0;
            } else if (lineLength > 0) {
                wrapped.append(" ");
                lineLength++;
            }
            wrapped.append(word);
            lineLength += word.length();
        }
        return wrapped.toString();
    }

    // Places the event status at the right edge while keeping the event name on the left.
    private String formatTitleRow(EventSummaryDto event) {
        String eventName = event.getEventName();
        String status = "[" + toDisplayText(event.getStatus()) + "]";
        int spaceCount = Math.max(1, OUTPUT_WIDTH - eventName.length() - status.length());

        return eventName + " ".repeat(spaceCount) + status;
    }

    private String buildOptionRows(List<String> optionNames) {
        List<String> rows = IntStream.range(0, optionNames.size())
                .mapToObj(index -> String.format("  [%d] %s", index + 1, optionNames.get(index)))
                .toList();

        return String.join(NEW_LINE, rows);
    }

    private String buildCurrentMarket(List<OptionStateDto> options) {
        return String.join(NEW_LINE, "CURRENT MARKET", SECTION_SEPARATOR,
                formatCurrentMarketRow("Option", "Value", "Shares bought"), SECTION_SEPARATOR,
                buildCurrentMarketRows(options), SECTION_SEPARATOR);
    }

    private String buildCurrentMarketRows(List<OptionStateDto> options) {
        List<String> rows = IntStream.range(0, options.size())
                .mapToObj(index -> {
                    OptionStateDto option = options.get(index);
                    String optionName = String.format("[%d] %s", index + 1, option.getOptionName());

                    return formatCurrentMarketRow(optionName, formatDecimal(option.getCurrentValue()),
                            String.valueOf(option.getTotalSharesBought()));
                })
                .toList();

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

    // Builds the history in the order received from the engine, which is newest first.
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
        List<String> rows = IntStream.range(0, trades.size())
                .mapToObj(index -> {
                    TradeDto trade = trades.get(index);
                    double totalPaid = trade.getSharesCost() + trade.getCommissionCost();
                    String tradeNumber = String.format("%d.", index + 1);

                    return formatTradeRow(tradeNumber, trade.getOptionName(),
                            String.valueOf(trade.getQuantity()), formatMoney(totalPaid));
                })
                .toList();

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

    // Aligns a value to a fixed column while keeping its label on the left.
    private String formatRightAlignedValue(String label, String value) {
        int spaceCount = Math.max(1, VALUE_ROW_WIDTH - label.length() - value.length());

        return label + " ".repeat(spaceCount) + value;
    }

    private String formatLabelAndValue(String label, String value) {
        return String.format(LABEL_AND_VALUE_FORMAT, label, value);
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
