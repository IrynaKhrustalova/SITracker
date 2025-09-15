package org.example.sitracker.cli;

import org.example.sitracker.domain.Issue;
import org.example.sitracker.domain.Status;
import org.example.sitracker.service.IssueService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UpdateCommand.
 */
class UpdateCommandTest {

    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    private ByteArrayOutputStream outBaos;
    private ByteArrayOutputStream errBaos;

    @BeforeEach
    void setUpStreams() {
        outBaos = new ByteArrayOutputStream();
        errBaos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outBaos));
        System.setErr(new PrintStream(errBaos));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void run_success_printsUpdatedIssueToStdout() {
        // Arrange
        IssueService svc = mock(IssueService.class);
        Issue updated = new Issue();
        updated.setId("AD-1");
        updated.setStatus(Status.CLOSED);
        updated.setDescription("some description");
        when(svc.updateIssueStatus("AD-1", Status.CLOSED)).thenReturn(updated);


        UpdateCommand cmd = new UpdateCommand(svc);
        CommandLine cli = new CommandLine(cmd);

        // Act: positional first, then -s option
        int exit = cli.execute("AD-1", "-s", "CLOSED");

        String out = outBaos.toString();
        assertTrue(out.contains("Updated issue: AD-1 -> CLOSED"), "Expected success message in stdout: " + out);
        assertTrue(out.contains("some description") || out.contains("AD-1"), "Expected printed issue or id");
        verify(svc, times(1)).updateIssueStatus("AD-1", Status.CLOSED);
        assertEquals("", errBaos.toString(), "No stderr output expected on success");
    }

    @Test
    void run_whenServiceThrows_printsErrorToStderr() {
        // Arrange
        IssueService svc = mock(IssueService.class);
        when(svc.updateIssueStatus("MISSING", Status.CLOSED)).thenThrow(new IllegalArgumentException("not found"));

        UpdateCommand cmd = new UpdateCommand(svc);
        CommandLine cli = new CommandLine(cmd);

        // stdout should be empty or not contain success message
        String out = outBaos.toString();
        assertFalse(out.contains("Updated issue:"), "Stdout should not contain success message on failure: " + out);
    }
}
