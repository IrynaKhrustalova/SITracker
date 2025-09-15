package org.example.sitracker.cli;

import org.example.sitracker.domain.Issue;
import org.example.sitracker.service.IssueService;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class CreateCommandTest {

    @Test
    void createCommand_callsService_and_printsResult() throws Exception {
        IssueService svc = mock(IssueService.class);
        Issue stub = new Issue();
        stub.setId("AD-1");
        stub.setDescription("cli test");
        when(svc.createIssue("cli test", null)).thenReturn(stub);

        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        try {
            CreateCommand cmd = new CreateCommand(svc);
            CommandLine cmdLine = new CommandLine(cmd);
            int exitCode = cmdLine.execute("-d", "cli test");
            assertEquals(0, exitCode);

            String output = baos.toString();
            assertTrue(output.contains("Created") || output.contains("AD-1"),
                    "Expected printed output to include Created or AD-1 but was: " + output);

            verify(svc).createIssue("cli test", null);
        } finally {
            System.setOut(originalOut);
        }
    }
}
