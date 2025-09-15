package org.example.sitracker.service;

import org.example.sitracker.domain.Issue;
import org.example.sitracker.domain.Status;
import org.example.sitracker.repository.IssueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Default implementation of {@link IssueService} backed by an {@link IssueRepository}.
 *
 * <p>This service creates, updates, and queries issues, delegating all persistence
 * operations to the repository. It wraps low-level exceptions into more user-friendly
 * runtime exceptions.
 */
@Service
public class IssueServiceImpl implements IssueService {
    private static final Logger log = LoggerFactory.getLogger(IssueServiceImpl.class);
    private final IssueRepository issueRepository;

    public IssueServiceImpl(IssueRepository issueRepository) {
        this.issueRepository = issueRepository;
    }

    @Override
    public Issue createIssue(String description, String parentId) {
        Issue issue = new Issue();
        issue.setDescription(description);
        issue.setParentId(parentId);
        try{
            issueRepository.save(issue);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save issue: " + e.getMessage(), e);
        }
        return issue;
    }

    @Override
    public Issue updateIssueStatus(String issueId, Status newStatus) {
        try{
            log.info("Update issue service");
            return issueRepository.updateStatus(issueId, newStatus);
//            Optional<Issue> issue = issueRepository.findById(issueId);
//            if (issue.isPresent()) {
//                Issue issueFromStore = issue.get();
//                issueFromStore.setStatus(newStatus);
//                issueFromStore.setUpdatedAt(LocalDateTime.now());
//                return issueFromStore;
//            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Issue not found");
        }
    }

    @Override
    public List<Issue> listIssuesByStatus(Status status) {
        try {
            if (status != null) {
                return issueRepository.findByStatus(status);
            } else {
                throw new IllegalArgumentException("Invalid status");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to list issues: " + e.getMessage(), e);
        }
    }
}
