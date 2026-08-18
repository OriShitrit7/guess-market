package guessmarket.ui.console;

import guessmarket.ui.console.exception.EmptyInputException;
import guessmarket.ui.console.exception.InvalidInputException;
import guessmarket.ui.console.exception.NonNumericInputException;
import guessmarket.ui.console.exception.NonPositiveNumberException;
import guessmarket.ui.console.exception.NumberOutOfRangeException;

import java.util.Scanner;

public class ConsoleInput {
    private final Scanner scanner = new Scanner(System.in);

    public String readLine() {
        return scanner.nextLine().trim();
    }

    public String readNonEmptyLine() throws EmptyInputException {
        String input = readLine();

        if (input.isEmpty()) {
            throw new EmptyInputException();
        }
        return input;
    }

    public int readNumberInRange(int min, int max) throws InvalidInputException {
        int number = convertToValidNumber(readNonEmptyLine());

        if (number < min || number > max) {
            throw new NumberOutOfRangeException(number, min, max);
        }
        return number;
    }

    public int readPositiveNumber() throws InvalidInputException {
        int number = convertToValidNumber(readNonEmptyLine());

        if (number <= 0) {
            throw new NonPositiveNumberException(number);
        }
        return number;
    }

    private int convertToValidNumber(String input) throws NonNumericInputException {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new NonNumericInputException(input);
        }
    }
}
