package org.example.sitracker.cli;

import org.example.sitracker.domain.Issue;
import org.example.sitracker.domain.Status;
import org.example.sitracker.service.IssueService;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;

@Command(name = "list", description = "List issues by status")
public class ListCommand implements Runnable {

    @Option(names = {"-s", "--status"}, required = true, description = "Status: ${COMPLETION-CANDIDATES}")
    private Status status;

    private final IssueService issueService;

    public ListCommand(IssueService issueService) {
        this.issueService = issueService;
    }

    @Override
    public void run() {
        try {
            List<Issue> issues = issueService.listIssuesByStatus(status);
            if (issues.isEmpty()) {
                System.out.println("No issues with status " + status);
            } else {
                issues.forEach(System.out::println);
            }
        } catch (Exception e) {
            System.err.println("Failed to list issues: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}
