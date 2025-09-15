package org.example.sitracker;

import org.example.sitracker.domain.Issue;
import org.example.sitracker.domain.Status;
import org.example.sitracker.service.IssueService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * Unit tests for CliRunner.
 * These tests do not start Spring; they instantiate the runner directly
 * and verify Picocli invokes the correct service methods.
 */
@ExtendWith(MockitoExtension.class)
class CliRunnerTest {

    @Mock
    IssueService issueService;

    @InjectMocks
    CliRunner cliRunner;

    @Test
    void run_withNoArgs_doesNothing() {
        // act
        cliRunner.run(); // no args

        // assert: no interactions with service
        verifyNoInteractions(issueService);
    }

    @Test
    void run_createCommand_invokesCreateIssue() {
        // arrange: stub service to return an Issue when createIssue is called (optional)
        Issue stub = new Issue();
        stub.setId("AD-1");
        when(issueService.createIssue("from-cli", null)).thenReturn(stub);

        // act: simulate "create -d from-cli"
        cliRunner.run("create", "-d", "from-cli");

        // assert: createIssue called once with description and null parentId
        verify(issueService, times(1)).createIssue("from-cli", null);
        verifyNoMoreInteractions(issueService);
    }

    @Test
    void run_updateCommand_invokesUpdateIssueStatus() {
        // arrange
        Issue updated = new Issue();
        updated.setId("AD-2");
        updated.setStatus(Status.CLOSED);
        when(issueService.updateIssueStatus("AD-2", Status.CLOSED)).thenReturn(updated);

        // act: simulate `update AD-2 -s DONE`
        cliRunner.run("update", "AD-2", "-s", "CLOSED");

        // assert
        verify(issueService, times(1)).updateIssueStatus("AD-2", Status.CLOSED);
        verifyNoMoreInteractions(issueService);
    }

    @Test
    void run_listCommand_invokesListIssuesByStatus() {
        // arrange
        when(issueService.listIssuesByStatus(Status.OPEN)).thenReturn(java.util.List.of());

        // act: simulate `list -s OPEN`
        cliRunner.run("list", "-s", "OPEN");

        // assert
        verify(issueService, times(1)).listIssuesByStatus(Status.OPEN);
        verifyNoMoreInteractions(issueService);
    }
}
