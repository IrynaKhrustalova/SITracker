package org.example.sitracker;

import org.example.sitracker.cli.CreateCommand;
import org.example.sitracker.cli.ListCommand;
import org.example.sitracker.cli.RootCommand;
import org.example.sitracker.cli.UpdateCommand;
import org.example.sitracker.service.IssueService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

/**
 * CLI entry point that integrates Spring Boot with Picocli commands.
 *
 * <p>This class is automatically run at application startup by Spring Boot
 * (due to implementing {@link CommandLineRunner}). It registers the available
 * subcommands (create, update, list) under a {@code sitracker} root command
 * and delegates execution to Picocli.
 *
 * <p>Example usage from the command line:
 * <pre>
 *   java -jar sitracker.jar create -d "New issue"
 *   java -jar sitracker.jar update AD-1 -s IN_PROGRESS
 *   java -jar sitracker.jar list -s OPEN
 * </pre>
 */
@Component
public class CliRunner implements CommandLineRunner {

    private final IssueService issueService;

    public CliRunner(IssueService issueService) {
        this.issueService = issueService;
    }

    /**
     * Executes the CLI if command-line arguments are provided.
     *
     * @param args raw arguments passed to the JVM
     */
    @Override
    public void run(String... args) {
        if (args == null || args.length == 0) {
            return;
        }

        RootCommand rootCmd = new RootCommand();
        CommandLine root = new CommandLine(rootCmd);

        root.addSubcommand("create", new CreateCommand(issueService));
        root.addSubcommand("update", new UpdateCommand(issueService));
        root.addSubcommand("list",   new ListCommand(issueService));
        root.execute(args);
    }
}
