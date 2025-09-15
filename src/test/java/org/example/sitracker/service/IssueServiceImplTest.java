package org.example.sitracker.service;

import org.example.sitracker.domain.Issue;
import org.example.sitracker.domain.Status;
import org.example.sitracker.repository.IssueRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for IssueServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class IssueServiceImplTest {

    @Mock
    private IssueRepository repo;

    @InjectMocks
    private IssueServiceImpl service;

    @Test
    void createIssue_savesIssueAndReturnsIt_withGeneratedIdFromRepo() throws Exception {
        // arrange
        ArgumentCaptor<Issue> captor = ArgumentCaptor.forClass(Issue.class);
        // simulate repo populating an ID on save
        doAnswer(inv -> {
            Issue i = inv.getArgument(0);
            i.setId("AD-1");
            return i;
        }).when(repo).save(any(Issue.class));

        // act
        Issue created = service.createIssue("Test description", "PARENT-1");

        // assert
        assertNotNull(created, "Created issue must not be null");
        assertEquals("AD-1", created.getId(), "Repository should have populated the ID");
        assertEquals("Test description", created.getDescription());
        assertEquals("PARENT-1", created.getParentId());

        verify(repo, times(1)).save(captor.capture());
        Issue savedArg = captor.getValue();
        assertEquals("Test description", savedArg.getDescription());
        assertEquals("PARENT-1", savedArg.getParentId());
    }

    @Test
    void createIssue_whenRepositoryThrows_wrappedInRuntimeException() throws Exception {
        // arrange
        when(repo.save(any())).thenThrow(new IOException("disk error"));

        // act & assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.createIssue("desc", null));
        assertTrue(ex.getMessage().contains("Failed to save issue"),
                "Exception message should indicate save failure");
        assertNotNull(ex.getCause());
    }

    @Test
    void updateIssueStatus_success_returnsUpdatedIssue() throws Exception {
        // arrange
        Issue returned = new Issue();
        returned.setId("AD-2");
        returned.setStatus(Status.IN_PROGRESS);
        when(repo.updateStatus("AD-2", Status.IN_PROGRESS)).thenReturn(returned);

        // act
        Issue updated = service.updateIssueStatus("AD-2", Status.IN_PROGRESS);

        // assert
        assertNotNull(updated);
        assertEquals("AD-2", updated.getId());
        assertEquals(Status.IN_PROGRESS, updated.getStatus());
        verify(repo).updateStatus("AD-2", Status.IN_PROGRESS);
    }

    @Test
    void listIssuesByStatus_returnsListFromRepository() throws Exception {
        // arrange
        Issue i1 = new Issue(); i1.setId("AD-1"); i1.setStatus(Status.OPEN);
        Issue i2 = new Issue(); i2.setId("AD-2"); i2.setStatus(Status.OPEN);
        when(repo.findByStatus(Status.OPEN)).thenReturn(List.of(i1, i2));

        // act
        List<Issue> results = service.listIssuesByStatus(Status.OPEN);

        // assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("AD-1", results.get(0).getId());
        verify(repo).findByStatus(Status.OPEN);
    }

    @Test
    void listIssuesByStatus_whenRepoThrows_wrappedInRuntimeException() throws Exception {
        when(repo.findByStatus(Status.OPEN)).thenThrow(new RuntimeException("api error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.listIssuesByStatus(Status.OPEN));
        assertTrue(ex.getMessage().contains("Failed to list issues"));
    }
}
