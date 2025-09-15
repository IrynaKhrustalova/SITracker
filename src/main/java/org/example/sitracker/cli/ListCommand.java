package org.example.sitracker.cli;

import org.example.sitracker.domain.Issue;
import org.example.sitracker.domain.Status;
import org.example.sitracker.service.IssueService;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import java.util.List;

/**
 * PicoCLI command that lists issues filtered by {@link Status}.
 *
 * <p>Usage example:
 * <pre>{@code
 * sitracker list -s OPEN
 * }</pre>
 *
 * <p>The command delegates to {@link IssueService#listIssuesByStatus(Status)} and prints each
 * matching {@link Issue} to {@code System.out}. If no issues are found, a friendly message is printed.
 */
@Command(name = "list", description = "List issues by status")
public class ListCommand implements Runnable {

    /**
     * Status to filter by. PicoCLI will offer completion candidates from {@link Status}.
     * Required CLI option: {@code -s} or {@code --status}.
     */
    @Option(names = {"-s", "--status"}, required = true, description = "Status: ${COMPLETION-CANDIDATES}")
    private Status status;

    /**
     * Service used to retrieve issues. Must not be {@code null}.
     */
    private final IssueService issueService;

    /**
     * Constructs the command with the provided {@link IssueService}.
     *
     * @param issueService service used to list issues; must not be {@code null}
     * @throws NullPointerException if {@code issueService} is {@code null}
     */
    public ListCommand(IssueService issueService) {
        this.issueService = issueService;
    }

    /**
     * Executes the command: lists issues by {@link #status} and prints results to {@code System.out}.
     * Errors are printed to {@code System.err} with a stacktrace for debugging.
     */
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
