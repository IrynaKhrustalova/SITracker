package org.example.sitracker.cli;

import org.example.sitracker.domain.Issue;
import org.example.sitracker.domain.Status;
import org.example.sitracker.service.IssueService;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "update", description = "Update issue status")
public class UpdateCommand implements Runnable {

    @Parameters(paramLabel = "<issueId>", description = "Issue ID to update (e.g., AD-1)")
    private String issueId;

    @Option(names = {"-s", "--status"}, required = true, description = "Status: ${COMPLETION-CANDIDATES}")
    private Status status;

    private final IssueService issueService;

    public UpdateCommand(IssueService issueService) {
        this.issueService = issueService;
    }

    @Override
    public void run() {
        try {
            Issue updated = issueService.updateIssueStatus(issueId, status);
            System.out.println("Updated issue: " + updated.getId() + " -> " + updated.getStatus());
            System.out.println(updated);
        } catch (Exception e) {
            System.err.println("Failed to update issue: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}
