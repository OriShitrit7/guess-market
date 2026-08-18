package guessmarket.ui.console;

import guessmarket.dto.EventSummaryDto;
import guessmarket.engine.api.MarketManager;
import guessmarket.engine.api.MarketManagerImpl;
import guessmarket.engine.exception.InvalidFileException;
import guessmarket.engine.exception.InvalidRequestException;
import guessmarket.ui.console.exception.InvalidInputException;

import java.util.List;
import java.util.Optional;

public class GuessMarketUI {
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
    public static void main(String[] args) {
        new GuessMarketUI().run();
    }

    private void run() {
        MenuCommand command;
        do {
            consolePrinter.showMenu();
            command = requestMenuCommand();
            executeCommand(command);
        } while (command != MenuCommand.EXIT);
    }

    private MenuCommand requestMenuCommand() {
        while (true) {
            consolePrinter.printMenuPrompt();

            try {
                return MenuCommand.getCommandFromNumber(
                        consoleInput.readNumberInRange(1, MenuCommand.values().length));
            } catch (InvalidInputException e) {
                consolePrinter.printErrorMessage(e.getMessage());
            }
        }
    }

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
                case EXIT -> {
                    consolePrinter.printGoodbyeMessage();
                }
            }
        } catch (InvalidRequestException e) {
            consolePrinter.printErrorMessage(e.getMessage());
        }
    }

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

    private String requestFilePath() {
        while (true) {
            consolePrinter.printFilePathPrompt();

            try {
                return consoleInput.readNonEmptyLine();
            } catch (InvalidInputException e) {
                consolePrinter.printErrorMessage(e.getMessage());
            }
        }
    }

    private void showEventsSummaries() {
        consolePrinter.printEvents(engine.getEventSummaries());
    }

    private void showEventTradingState() {
        List<EventSummaryDto> events = engine.getEventSummaries();
        consolePrinter.printEvents(events);
        EventSummaryDto event = requestEvent(events);
        consolePrinter.printEventTradingState(engine.getEventTradingState(event.getEventId()));

    }

    private void participateInEvent() {
        List<EventSummaryDto> activeEvents = engine.getActiveEventSummaries();
        if (activeEvents.isEmpty()) {
            consolePrinter.printNoActiveEventsMessage();
            return;
        }

        consolePrinter.printEvents(activeEvents);

        EventSummaryDto event = requestEvent(activeEvents);
        consolePrinter.printOptionStates(engine.getEventTradingState(event.getEventId()).getOptions());

        int optionIndex = requestOptionIndex(event);
        int quantity = requestQuantity();

        consolePrinter.printPurchaseResult(engine.buyShares(event.getEventId(), optionIndex, quantity));
    }

    private void closeEvent() {
        List<EventSummaryDto> activeEvents = engine.getActiveEventSummaries();

        if (activeEvents.isEmpty()) {
            consolePrinter.printNoActiveEventsMessage();
            return;
        }

        consolePrinter.printEvents(activeEvents);

        EventSummaryDto event = requestEvent(activeEvents);
        consolePrinter.printEventTradingState(engine.getEventTradingState(event.getEventId()));

        consolePrinter.printWinningOptionHeader();

        int winningOptionIndex = requestOptionIndex(event);

        consolePrinter.printEventTradingState(engine.closeEvent(event.getEventId(), winningOptionIndex));
    }


    private EventSummaryDto requestEvent(List<EventSummaryDto> events) {
        while (true) {
            consolePrinter.printEventChoicePrompt();

            try {
                int eventId = consoleInput.readPositiveNumber();
                Optional<EventSummaryDto> event = findEventById(events, eventId);

                if (event.isPresent()) {
                    return event.get();
                }
                consolePrinter.printUnknownEventIdError(eventId);
            } catch (InvalidInputException e) {
                consolePrinter.printErrorMessage(e.getMessage());
            }
        }
    }

    private Optional<EventSummaryDto> findEventById(List<EventSummaryDto> events, int eventId) {
        return events.stream()
                .filter(event -> event.getEventId() == eventId)
                .findFirst();
    }

    private int requestOptionIndex(EventSummaryDto event) {
        consolePrinter.printOptions(event.getOptionNames());

        while (true) {
            consolePrinter.printOptionChoicePrompt(event.getOptionNames().size());

            try {
                return consoleInput.readNumberInRange(1, event.getOptionNames().size()) - 1;
            } catch (InvalidInputException e) {
                consolePrinter.printErrorMessage(e.getMessage());
            }
        }
    }

    private int requestQuantity() {
        while (true) {
            consolePrinter.printQuantityPrompt();

            try {
                return consoleInput.readPositiveNumber();
            } catch (InvalidInputException e) {
                consolePrinter.printErrorMessage(e.getMessage());
            }
        }
    }

}
