package service;

import domain.Issue;
import domain.Status;
import repository.IssueRepository;
import util.IdGenerator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class IssueServiceImpl implements IssueService {
    private final IssueRepository issueRepository;
    private final IdGenerator idGenerator;

    public IssueServiceImpl(IssueRepository issueRepository, IdGenerator idGenerator) {
        this.issueRepository = issueRepository;
        this.idGenerator = idGenerator;
    }

    @Override
    public Issue createIssue(String description, String parentId) {
        String id = idGenerator.nextId();
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();
        return new Issue(id, description, parentId, Status.OPEN, createdAt, updatedAt);
    }

    @Override
    public Issue updateIssueStatus(String issueId, Status newStatus) {
        Optional<Issue> issue = issueRepository.findById(issueId);
        if (issue.isPresent()) {
            Issue issueFromStore = issue.get();
            issueFromStore.setStatus(newStatus);
            issueFromStore.setUpdatedAt(LocalDateTime.now());
            return issueFromStore;
        } else {
            throw new IllegalArgumentException("Issue not found");
        }
    }

    @Override
    public List<Issue> listIssuesByStatus(Status status) {
        if (status == null) {
            return issueRepository.findByStatus(status);
        } else {
            throw new IllegalArgumentException("Invalid status");
        }
    }
}
