package org.example.sitracker.cli;

import org.example.sitracker.domain.Issue;
import org.example.sitracker.domain.Status;
import org.example.sitracker.service.IssueService;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ListCommandTest {

    @Test
    void listCommand_printsIssues() {
        IssueService svc = mock(IssueService.class);
        Issue i1 = new Issue(); i1.setId("AD-1"); i1.setDescription("one"); i1.setStatus(Status.OPEN);
        Issue i2 = new Issue(); i2.setId("AD-2"); i2.setDescription("two"); i2.setStatus(Status.OPEN);
        when(svc.listIssuesByStatus(Status.OPEN)).thenReturn(List.of(i1, i2));

        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        try {
            ListCommand cmd = new ListCommand(svc);
            CommandLine cmdLine = new CommandLine(cmd);
            int exitCode = cmdLine.execute("-s", "OPEN");
            assertEquals(0, exitCode);

            String out = baos.toString();
            assertTrue(out.contains("AD-1") && out.contains("AD-2"));
            verify(svc).listIssuesByStatus(Status.OPEN);
        } finally {
            System.setOut(originalOut);
        }
    }
}
