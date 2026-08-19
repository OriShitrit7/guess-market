package guessmarket.ui.console;

import guessmarket.ui.console.exception.InvalidInputException;

// Supplies one value read from the console, and reports invalid input instead of returning a value.
@FunctionalInterface
interface ConsoleInputSupplier<T> {
    T get() throws InvalidInputException;
}
