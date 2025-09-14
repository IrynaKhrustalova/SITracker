package org.example.sitracker.service;

import org.example.sitracker.domain.Issue;
import org.example.sitracker.domain.Status;
import java.util.List;

public interface IssueService {

    Issue createIssue(String description, String parentId);

    Issue updateIssueStatus(String issueId, Status newStatus);

    List<Issue> listIssuesByStatus(Status status);
}
