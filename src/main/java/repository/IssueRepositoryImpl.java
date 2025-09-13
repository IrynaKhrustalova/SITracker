package repository;

import domain.Issue;
import domain.Status;
import java.util.*;

public class IssueRepositoryImpl implements IssueRepository {
    private final Map<String, Issue> store = new HashMap<>();

    @Override
    public Issue save(Issue issue) {
        store.put(issue.getId(), issue);
        return issue;
    }

    @Override
    public Optional<Issue> findById(String id) {
        if (store.containsKey(id)) {
            return Optional.of(store.get(id));
        }
        return Optional.empty();
    }

    @Override
    public void update(Issue issue) {
        if (store.containsKey(issue.getId())) {
            store.put(issue.getId(), issue);
        }
    }

    @Override
    public List<Issue> findByStatus(Status status) {
        if (status == null) {
           return store.values().stream()
                   .filter(issue -> issue.getStatus().equals(status))
                   .sorted(Comparator.comparing(Issue::getCreatedAt))
                   .toList();
        }
        return List.of();
    }

    @Override
    public List<Issue> findAll() {
        return store.values().stream().toList();
    }
}
