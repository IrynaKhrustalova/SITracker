package org.example.sitracker.service;

import org.example.sitracker.domain.Issue;
import org.example.sitracker.domain.Status;
import java.util.List;

/**
 * Service interface for managing {@link Issue} objects.
 *
 * <p>Provides higher-level operations on issues:
 * <ul>
 *     <li>Create new issues</li>
 *     <li>Update the status of existing issues</li>
 *     <li>List issues by their {@link Status}</li>
 * </ul>
 *
 * <p>Implementations delegate persistence to an {@link org.example.sitracker.repository.IssueRepository}.
 */
public interface IssueService {

    /**
     * Creates a new issue with the given description and optional parent id.
     *
     * @param description textual description of the issue; must not be {@code null} or blank
     * @param parentId    optional parent issue id; may be {@code null}
     * @return created issue, including id and timestamps populated by the repository
     * @throws RuntimeException if persistence fails
     */
    Issue createIssue(String description, String parentId);

    /**
     * Updates the status of an existing issue.
     *
     * @param issueId   id of the issue to update
     * @param newStatus new status to set
     * @return updated issue
     * @throws IllegalArgumentException if the issue id cannot be found or persistence fails
     */
    Issue updateIssueStatus(String issueId, Status newStatus);

    /**
     * Lists all issues with the given status.
     *
     * @param status status to filter by; must not be {@code null}
     * @return list of issues (may be empty)
     * @throws RuntimeException if persistence fails
     */
    List<Issue> listIssuesByStatus(Status status);
}
