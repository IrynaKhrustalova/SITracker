package org.example.sitracker.repository;

import org.example.sitracker.domain.Issue;
import org.example.sitracker.domain.Status;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface IssueRepository {
    Issue save(Issue issue) throws IOException;
    Issue updateStatus(String id, Status newStatus) throws IOException;
    Optional<Issue> findById(String id) throws IOException;
    List<Issue> findByStatus(Status status) throws IOException;
    List<Issue> findAll() throws IOException;
}
