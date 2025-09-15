package org.example.sitracker.cli;

import picocli.CommandLine.Command;

/**
 * Root (top-level) PicoCLI command for the SiTracker application.
 *
 * <p>This command only serves as an entry point that prints help when no subcommand
 * is provided. It is configured with {@code mixinStandardHelpOptions=true} so PicoCLI
 * automatically adds standard options such as {@code -h/--help} and {@code -V/--version}.
 *
 * <p>Example (CLI):
 * <pre>{@code
 * sitracker             # prints help / usage for the top-level command
 * sitracker --help
 * }</pre>
 */
@Command(
    name = "sitracker",
    mixinStandardHelpOptions = true,
    description = "SiTracker CLI - manage issues (create, update, list)"
)
public class RootCommand implements Runnable {
    /**
     * When the root command is invoked without subcommands, print the usage/help text.
     */
    @Override
    public void run() {
        CommandLineHelp.printHelp(this);
    }
}
