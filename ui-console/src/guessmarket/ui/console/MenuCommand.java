package guessmarket.ui.console;

import java.util.Arrays;

// Represents a console menu command and whether it requires a loaded system file.
public enum MenuCommand {
    LOAD_SYSTEM_FILE(1, "Load system file", false),
    SHOW_EVENTS(2, "Show events", true),
    SHOW_EVENT_TRADING_STATE(3, "Show event trading state", true),
    PARTICIPATE_IN_EVENT(4, "Participate in an event", true),
    CLOSE_EVENT(5, "Close an event", true),
    EXIT(6, "Exit", false);

    private final int number;
    private final String label;
    private final boolean requiresLoadedFile;

    MenuCommand(int number, String label, boolean requiresLoadedFile) {
        this.number = number;
        this.label = label;
        this.requiresLoadedFile = requiresLoadedFile;
    }

    // Finds the command assigned to a displayed menu number.
    public static MenuCommand getCommandFromNumber(int number) {
        return Arrays.stream(values())
                .filter(command -> command.number == number)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No menu command with number " + number));
    }

    public boolean requiresLoadedFile() {
        return requiresLoadedFile;
    }

    @Override
    public String toString() {
        return number + ". " + label;
    }
}
