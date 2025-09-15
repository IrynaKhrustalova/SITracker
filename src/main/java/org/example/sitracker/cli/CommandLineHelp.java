package org.example.sitracker.cli;

import picocli.CommandLine;

/**
 * Utility for printing PicoCLI usage/help information for a command object.
 *
 * <p>This class provides a single convenience method that constructs a {@link CommandLine}
 * for the given command object and prints the generated usage message to {@code System.out}
 * (by default). The command object should be a PicoCLI-annotated command (with
 * {@code @Command} and options/parameters).
 *
 * <p>Example:
 * <pre>{@code
 * MyCommand cmd = new MyCommand();
 * CommandLineHelp.printHelp(cmd);
 * }</pre>
 *
 * <p>Note: the method currently writes to {@link System#out}. If you need to redirect output
 * (for testing or logging), consider copying this implementation and calling
 * {@code new CommandLine(cmd).usage(PrintStream)} with your own stream.
 */
public class CommandLineHelp {
    /**
     * Prints the PicoCLI usage message for the given command object to {@code System.out}.
     *
     * @param cmd an instance of a PicoCLI command (annotated with {@code @Command});
     *            must not be {@code null}. The method will build a {@link CommandLine}
     *            for this object and print the usage/help text.
     */
    public static void printHelp(Object cmd) {
        new CommandLine(cmd).usage(System.out);
    }
}
