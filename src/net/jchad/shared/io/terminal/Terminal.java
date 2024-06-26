package net.jchad.shared.io.terminal;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * This class represents a terminal interface which is used to output and read data.
 */
public abstract class Terminal {
    /**
     * This method returns a {@link Terminal} instance, based on the terminal capability.
     *
     * @return a compatible {@link Terminal} instance.
     */
    public static Terminal get() {
        try {
            return new AdvancedTerminal();
        } catch (IOException e) {
            return new SimpleTerminal();
        }
    }

    /**
     * Output a message to the terminal.
     *
     * @param message the message that should be displayed
     */
    public abstract void outputMessage(String message);

    /**
     * This method initializes the input reader for this terminal.
     * Read inputs get returned via a {@link Consumer<String>}.
     *
     * @param inputHandler the method responsible for handling the read input.
     * @param exitHandler this method will be called if the user interrupts the CLI (using CTRL + C)
     */
    public abstract void initInputReading(Consumer<String> inputHandler, Runnable exitHandler);

    /**
     * This method only read a single user input.
     *
     * @return the read {@link String}
     */
    public abstract String read() throws UserExitedException;

    /**
     * Close a terminal instance.
     * This method closes any {@link java.util.stream.Stream}s or other resources used by the terminal.
     */
    public abstract void close();
}
