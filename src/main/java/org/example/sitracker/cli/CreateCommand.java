package org.example.sitracker.cli;

import org.example.sitracker.domain.Issue;
import org.example.sitracker.service.IssueService;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "create", description = "Create a new issue")
public class CreateCommand implements Runnable {

    @Option(names = {"-d", "--description"}, required = true, description = "Issue description")
    private String description;

    @Option(names = {"-p", "--parentId"}, description = "Parent issue ID (optional)")
    private String parentId;

    private final IssueService issueService;

    public CreateCommand(IssueService issueService) {
        this.issueService = issueService;
    }

    @Override
    public void run() {
        try {
            Issue created = issueService.createIssue(description, parentId);
            System.out.println("Created issue: " + created.getId());
            System.out.println(created);
        } catch (Exception e) {
            System.err.println("Failed to create issue: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}
