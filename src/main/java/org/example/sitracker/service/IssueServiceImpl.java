package org.example.sitracker.service;

import org.example.sitracker.domain.Issue;
import org.example.sitracker.domain.Status;
import org.example.sitracker.repository.IssueRepository;
import org.example.sitracker.util.IdGenerator;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
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

        return new Issue(id, description, parentId, Status.OPEN, createdAt);
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
        if (status != null) {
            return issueRepository.findByStatus(status);
        } else {
            throw new IllegalArgumentException("Invalid status");
        }
    }
}
