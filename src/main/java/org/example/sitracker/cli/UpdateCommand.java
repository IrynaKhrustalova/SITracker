package org.example.sitracker.cli;

import org.example.sitracker.domain.Issue;
import org.example.sitracker.domain.Status;
import org.example.sitracker.service.IssueService;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * PicoCLI command that updates the status of an existing {@link Issue}.
 *
 * <p>Usage example:
 * <pre>{@code
 * sitracker update AD-1 -s DONE
 * }</pre>
 *
 * <p>The command delegates to {@link IssueService#updateIssueStatus(String, Status)} and prints
 * the updated issue id and new status to {@code System.out}. Any exceptions are printed to
 * {@code System.err} together with a stacktrace.
 */
@Command(name = "update", description = "Update issue status")
public class UpdateCommand implements Runnable {

    /**
     * The ID of the issue to update (e.g., AD-1).
     * This is a required positional parameter.
     */
    @Parameters(paramLabel = "<issueId>", description = "Issue ID to update (e.g., AD-1)")
    private String issueId;

    /**
     * New status for the issue. PicoCLI will offer completion candidates from {@link Status}.
     * Required CLI option: {@code -s} or {@code --status}.
     */
    @Option(names = {"-s", "--status"}, required = true, description = "Status: ${COMPLETION-CANDIDATES}")
    private Status status;

    /**
     * Service responsible for updating issue status. Must not be {@code null}.
     */
    private final IssueService issueService;

    /**
     * Constructs the command with the provided {@link IssueService}.
     *
     * @param issueService service used to update issues; must not be {@code null}
     * @throws NullPointerException if {@code issueService} is {@code null}
     */
    public UpdateCommand(IssueService issueService) {
        this.issueService = issueService;
    }

    /**
     * Executes the command: updates the status of the issue identified by {@link #issueId}.
     * Prints the updated issue id and status to {@code System.out}. Errors are logged to
     * {@code System.err} with a stacktrace.
     */
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
