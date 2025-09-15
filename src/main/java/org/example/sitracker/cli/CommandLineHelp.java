package org.example.sitracker.cli;

import picocli.CommandLine;

public class CommandLineHelp {
    public static void printHelp(Object cmd) {
        new CommandLine(cmd).usage(System.out);
    }
}
