package org.example.sitracker.repository;

import org.example.sitracker.domain.Issue;
import org.example.sitracker.domain.Status;
import java.util.List;
import java.util.Optional;

public interface IssueRepository {

    void save(Issue issue);

    Optional<Issue> findById(String id);

    void update(Issue issue);

    List<Issue> findByStatus(Status status);

    List<Issue> findAll();
}
