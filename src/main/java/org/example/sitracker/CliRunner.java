package org.example.sitracker;

import org.example.sitracker.cli.CreateCommand;
import org.example.sitracker.cli.ListCommand;
import org.example.sitracker.cli.RootCommand;
import org.example.sitracker.cli.UpdateCommand;
import org.example.sitracker.service.IssueService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Component
public class CliRunner implements CommandLineRunner {

    private final IssueService issueService;

    public CliRunner(IssueService issueService) {
        this.issueService = issueService;
    }

    @Override
    public void run(String... args) throws Exception {
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
