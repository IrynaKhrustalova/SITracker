package repository;

import domain.Issue;
import domain.Status;
import java.util.List;
import java.util.Optional;

public interface IssueRepository {

    Issue save(Issue issue);

    Optional<Issue> findById(String id);

    void update(Issue issue);

    List<Issue> findByStatus(Status status);

    List<Issue> findAll();
}
