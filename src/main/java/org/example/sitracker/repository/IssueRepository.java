package org.example.sitracker.repository;

import org.example.sitracker.domain.Issue;
import org.example.sitracker.domain.Status;
import java.io.IOException;
import java.util.List;

/**
 * Repository abstraction for persisting and querying {@link Issue} entities.
 *
 * <p>Provides methods to save new issues, update their status, and query by id or status.
 * Implementations may back onto different storage systems (e.g., Google Sheets).
 */
public interface IssueRepository {
    /**
     * Saves a new issue or appends it to storage.
     *
     * @param issue issue to save
     * @return saved issue with id and timestamps populated
     * @throws IOException if persistence fails
     */
    Issue save(Issue issue) throws IOException;

    /**
     * Updates the status of an existing issue.
     *
     * @param id        issue id
     * @param newStatus new status
     * @return updated issue
     * @throws IOException if persistence fails
     */
    Issue updateStatus(String id, Status newStatus) throws IOException;

    /**
     * Finds all issues with the given status.
     *
     * @param status status to match
     * @return list of issues (may be empty)
     * @throws IOException if persistence fails
     */
    List<Issue> findByStatus(Status status) throws IOException;

    /**
     * Returns all issues from storage.
     *
     * @return list of all issues (may be empty)
     * @throws IOException if persistence fails
     */
    List<Issue> findAll() throws IOException;
}
