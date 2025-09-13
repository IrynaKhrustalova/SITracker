package service;

import domain.Issue;
import domain.Status;
import java.util.List;

public interface IssueService {

    Issue createIssue(String description, String parentId);

    Issue updateIssueStatus(String issueId, Status newStatus);

    List<Issue> listIssuesByStatus(Status status);
}
