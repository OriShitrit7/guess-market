package guessmarket.ui.console;

import guessmarket.ui.console.exception.EmptyInputException;
import guessmarket.ui.console.exception.InputClosedException;
import guessmarket.ui.console.exception.InvalidInputException;
import guessmarket.ui.console.exception.NonNumericInputException;
import guessmarket.ui.console.exception.NonPositiveNumberException;
import guessmarket.ui.console.exception.NumberOutOfRangeException;

import java.util.Scanner;

// Reads console input and validates values before they are used by the UI.
public class ConsoleInput {
    private final Scanner scanner = new Scanner(System.in);

    // Reads a line and rejects input that contains no value.
    public String readNonEmptyLine() throws EmptyInputException {
        String input = readLine();

        if (input.isEmpty()) {
            throw new EmptyInputException();
        }
        return input;
    }

    // Reads a whole number that must be within the given inclusive range.
    public int readNumberInRange(int min, int max) throws InvalidInputException {
        int number = convertToValidNumber(readNonEmptyLine());

        if (number < min || number > max) {
            throw new NumberOutOfRangeException(number, min, max);
        }
        return number;
    }

    // Reads a whole number that must be greater than zero.
    public int readPositiveNumber() throws InvalidInputException {
        int number = convertToValidNumber(readNonEmptyLine());

        if (number <= 0) {
            throw new NonPositiveNumberException(number);
        }
        return number;
    }

    // Reads one line and removes surrounding whitespace.
    // A closed stream cannot be answered by the user, so it ends the application instead of looping.
    private String readLine() {
        if (!scanner.hasNextLine()) {
            throw new InputClosedException();
        }
        return scanner.nextLine().trim();
    }

    // Converts text to a whole number and translates parsing failures into an input error.
    private int convertToValidNumber(String input) throws NonNumericInputException {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new NonNumericInputException(input);
        }
    }
}
