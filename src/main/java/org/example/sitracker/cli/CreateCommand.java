package org.example.sitracker.cli;

import org.example.sitracker.domain.Issue;
import org.example.sitracker.service.IssueService;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * PicoCLI command that creates a new {@link Issue}.
 *
 * <p>Usage example (CLI):
 * <pre>{@code
 * sitracker create -d "My issue description" [-p parentId]
 * }</pre>
 *
 * <p>The command delegates creation to an {@link IssueService} and prints the created
 * issue id and the issue object to {@code System.out}. Any exception thrown during
 * creation is printed to {@code System.err} together with a stacktrace.
 */
@Command(name = "create", description = "Create a new issue")
public class CreateCommand implements Runnable {

    /**
     * Issue description provided by the user.
     * Required CLI option: {@code -d} or {@code --description}.
     */
    @Option(names = {"-d", "--description"}, required = true, description = "Issue description")
    private String description;

    /**
     * Optional parent issue id. If set, the created issue will reference this parent id.
     * CLI option: {@code -p} or {@code --parentId}.
     */
    @Option(names = {"-p", "--parentId"}, description = "Parent issue ID (optional)")
    private String parentId;

    /**
     * Service responsible for issue creation and persistence.
     * Must not be {@code null}.
     */
    private final IssueService issueService;

    /**
     * Constructs the command with the provided {@link IssueService}.
     *
     * @param issueService service used to create issues; must not be {@code null}
     * @throws NullPointerException if {@code issueService} is {@code null}
     */
    public CreateCommand(IssueService issueService) {
        this.issueService = issueService;
    }

    /**
     * Executes the command: creates a new issue using {@link #description} and {@link #parentId},
     * then prints the created issue id and the issue object to {@code System.out}.
     *
     * <p>Any exceptions are reported to {@code System.err} and include a stacktrace for debugging.
     */
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
