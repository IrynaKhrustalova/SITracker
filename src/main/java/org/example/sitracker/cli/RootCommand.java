package org.example.sitracker.cli;

import picocli.CommandLine.Command;

@Command(
    name = "sitracker",
    mixinStandardHelpOptions = true,
    description = "SiTracker CLI - manage issues (create, update, list)"
)
public class RootCommand implements Runnable {
    @Override
    public void run() {
        CommandLineHelp.printHelp(this);
    }
}
