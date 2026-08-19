package guessmarket.ui.console;

import guessmarket.dto.EventSummaryDto;
import guessmarket.engine.api.MarketManager;
import guessmarket.engine.api.MarketManagerImpl;
import guessmarket.engine.exception.InvalidFileException;
import guessmarket.engine.exception.InvalidRequestException;
import guessmarket.ui.console.exception.InputClosedException;
import guessmarket.ui.console.exception.InvalidInputException;
import guessmarket.ui.console.exception.UnknownEventIdException;

import java.util.List;
import java.util.Optional;

// Controls the console application flow and connects user actions to the market engine.
public class GuessMarketUI {
    private static final int FIRST_MENU_NUMBER = 1;
    private static final int FIRST_OPTION_NUMBER = 1;

    private final MarketManager engine;
    private final ConsolePrinter consolePrinter;
    private final ConsoleInput consoleInput;
    private boolean fileLoaded;

    public GuessMarketUI() {
        engine = new MarketManagerImpl();
        consolePrinter = new ConsolePrinter();
        consoleInput = new ConsoleInput();
        fileLoaded = false;
    }

    // Starts the console application.
    public static void main(String[] args) {
        new GuessMarketUI().run();
    }

    // Repeats the main menu until the user exits or the input stream is closed.
    private void run() {
        MenuCommand command;

        try {
            do {
                consolePrinter.showMenu();
                command = requestMenuCommand();
                executeCommand(command);
            } while (command != MenuCommand.EXIT);
        } catch (InputClosedException e) {
            consolePrinter.printErrorMessage(e.getMessage());
        }
    }

    // Blocks commands that require data, then sends the selected command to its handler.
    private void executeCommand(MenuCommand command) {
        if (command.requiresLoadedFile() && !fileLoaded) {
            consolePrinter.printNoFileLoadedError();
            return;
        }

        try {
            switch (command) {
                case LOAD_SYSTEM_FILE -> loadSystemFile();
                case SHOW_EVENTS -> showEventsSummaries();
                case SHOW_EVENT_TRADING_STATE -> showEventTradingState();
                case PARTICIPATE_IN_EVENT -> participateInEvent();
                case CLOSE_EVENT -> closeEvent();
                case EXIT -> consolePrinter.printGoodbyeMessage();
            }
        } catch (InvalidRequestException e) {
            consolePrinter.printErrorMessage(e.getMessage());
        }
    }

    // Loads a system file without discarding the current state when validation fails.
    private void loadSystemFile() {
        String path = requestFilePath();

        try {
            engine.loadSystemFile(path);
            fileLoaded = true;
            consolePrinter.printLoadSuccessMessage(engine.getEventSummaries().size());
        } catch (InvalidFileException e) {
            consolePrinter.printErrorMessage(e.getMessage());
        }
    }

    private void showEventsSummaries() {
        consolePrinter.printEvents(engine.getEventSummaries());
    }

    // Lets the user choose any loaded event and displays its current state.
    private void showEventTradingState() {
        List<EventSummaryDto> events = engine.getEventSummaries();

        consolePrinter.printEvents(events);
        EventSummaryDto event = requestEvent(events);

        consolePrinter.printEventTradingState(engine.getEventTradingState(event.getEventId()));
    }

    // Guides the user through selecting an active event, option and purchase quantity.
    private void participateInEvent() {
        Optional<EventSummaryDto> chosenEvent = requestActiveEvent();

        if (chosenEvent.isEmpty()) {
            return;
        }

        EventSummaryDto event = chosenEvent.get();
        consolePrinter.printOptionStates(engine.getEventTradingState(event.getEventId()).getOptions());

        int optionIndex = requestOptionIndex(event);
        int quantity = requestQuantity();

        consolePrinter.printPurchaseResult(engine.buyShares(event.getEventId(), optionIndex, quantity));
    }

    // Guides the user through selecting an active event and its winning option.
    private void closeEvent() {
        Optional<EventSummaryDto> chosenEvent = requestActiveEvent();

        if (chosenEvent.isEmpty()) {
            return;
        }

        EventSummaryDto event = chosenEvent.get();
        consolePrinter.printEventTradingState(engine.getEventTradingState(event.getEventId()));
        consolePrinter.printWinningOptionHeader();

        int winningOptionIndex = requestOptionIndex(event);

        consolePrinter.printEventTradingState(engine.closeEvent(event.getEventId(), winningOptionIndex));
    }

    // Displays the events that can still be traded, and reports when none are left.
    private Optional<EventSummaryDto> requestActiveEvent() {
        List<EventSummaryDto> activeEvents = engine.getActiveEventSummaries();

        if (activeEvents.isEmpty()) {
            consolePrinter.printNoActiveEventsMessage();
            return Optional.empty();
        }
        consolePrinter.printEvents(activeEvents);

        return Optional.of(requestEvent(activeEvents));
    }

    // Keeps requesting a command until the user enters a valid menu number.
    private MenuCommand requestMenuCommand() {
        return readUntilValid(() -> {
            consolePrinter.printMenuPrompt();

            return MenuCommand.getCommandFromNumber(
                    consoleInput.readNumberInRange(FIRST_MENU_NUMBER, MenuCommand.values().length));
        });
    }

    // Keeps requesting a path until the user enters a non-empty value.
    private String requestFilePath() {
        return readUntilValid(() -> {
            consolePrinter.printFilePathPrompt();

            return consoleInput.readNonEmptyLine();
        });
    }

    // Accepts only an event ID that appears in the list currently shown to the user.
    private EventSummaryDto requestEvent(List<EventSummaryDto> events) {
        return readUntilValid(() -> {
            consolePrinter.printEventChoicePrompt();
            int eventId = consoleInput.readPositiveNumber();

            return findEventById(events, eventId).orElseThrow(() -> new UnknownEventIdException(eventId));
        });
    }

    private Optional<EventSummaryDto> findEventById(List<EventSummaryDto> events, int eventId) {
        return events.stream()
                .filter(event -> event.getEventId() == eventId)
                .findFirst();
    }

    // Converts the user's one-based option number to the zero-based index used by the engine.
    private int requestOptionIndex(EventSummaryDto event) {
        int optionCount = event.getOptionNames().size();
        consolePrinter.printOptions(event.getOptionNames());

        return readUntilValid(() -> {
            consolePrinter.printOptionChoicePrompt(optionCount);

            return consoleInput.readNumberInRange(FIRST_OPTION_NUMBER, optionCount) - FIRST_OPTION_NUMBER;
        });
    }

    // Keeps requesting a quantity until the user enters a positive whole number.
    private int requestQuantity() {
        return readUntilValid(() -> {
            consolePrinter.printQuantityPrompt();

            return consoleInput.readPositiveNumber();
        });
    }

    // Repeats a console read until it produces a value, reporting every rejected attempt to the user.
    private <T> T readUntilValid(ConsoleInputSupplier<T> reader) {
        while (true) {
            try {
                return reader.get();
            } catch (InvalidInputException e) {
                consolePrinter.printErrorMessage(e.getMessage());
            }
        }
    }
}
