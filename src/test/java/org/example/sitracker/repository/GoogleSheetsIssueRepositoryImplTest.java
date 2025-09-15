package org.example.sitracker.repository;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.example.sitracker.domain.Issue;
import org.example.sitracker.domain.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GoogleSheetsIssueRepositoryImpl.
 * These tests mock the Google Sheets client classes (Sheets, Spreadsheets, Values and the
 * request objects) so tests run offline.
 */
@ExtendWith(MockitoExtension.class)
class GoogleSheetsIssueRepositoryImplTest {

    @Mock
    Sheets sheets;

    @Mock
    Sheets.Spreadsheets spreadsheets;

    @Mock
    Sheets.Spreadsheets.Values values;

    // request objects for different get/update/append calls
    @Mock
    Sheets.Spreadsheets.Values.Get getHeaderRequest;

    @Mock
    Sheets.Spreadsheets.Values.Get getAllRequest;

    @Mock
    Sheets.Spreadsheets.Values.Update updateRequest;

    @Mock
    Sheets.Spreadsheets.Values.Append appendRequest;

    // repository under test
    GoogleSheetsIssueRepositoryImpl repo;

    final String spreadsheetId = "spreadsheet-123";

    @BeforeEach
    void setUp() {
        // basic wiring of sheets -> spreadsheets -> values
        when(sheets.spreadsheets()).thenReturn(spreadsheets);
        when(spreadsheets.values()).thenReturn(values);

        // default: map get(spreadsheetId, anything) to either header or all depending on range string.
        // We'll override in individual tests where needed.

        // create repo instance
        repo = new GoogleSheetsIssueRepositoryImpl(sheets, spreadsheetId);
    }

    @Test
    void save_when_no_header_createsHeader_and_appendsRow_and_generatesId() throws Exception {
        // Simulate header missing: header get returns null or empty values
        ValueRange emptyHeader = new ValueRange(); // no values -> triggers header creation
        when(values.get(eq(spreadsheetId), eq("Issues!A1:F1"))).thenReturn(getHeaderRequest);
        when(getHeaderRequest.execute()).thenReturn(emptyHeader);

        // Simulate reading all rows for generateNextId -> only header present (so AD-1)
        ValueRange onlyHeader = new ValueRange().setValues(List.of(List.of("ID"))); // one row (header)
        when(values.get(eq(spreadsheetId), eq("Issues!A:F"))).thenReturn(getAllRequest);
        when(getAllRequest.execute()).thenReturn(onlyHeader);

        // prepare append/update request chaining
        when(values.update(eq(spreadsheetId), eq("Issues!A1:F1"), any(ValueRange.class))).thenReturn(updateRequest);
        when(updateRequest.setValueInputOption(anyString())).thenReturn(updateRequest);
        when(updateRequest.execute()).thenReturn(null); // header update not used further

        when(values.append(eq(spreadsheetId), eq("Issues!A:F"), any(ValueRange.class))).thenReturn(appendRequest);
        when(appendRequest.setValueInputOption(anyString())).thenReturn(appendRequest);
        when(appendRequest.execute()).thenReturn(null); // append response not used

        // capture the ValueRange passed to append
        ArgumentCaptor<ValueRange> appendBodyCaptor = ArgumentCaptor.forClass(ValueRange.class);

        // Act: save a new issue (no id)
        Issue in = new Issue();
        in.setDescription("Test issue from unit");
        in.setParentId(null);

        Issue saved = repo.save(in);

        // Assert: ID generated AD-1, status OPEN and timestamps set
        assertNotNull(saved.getId());
        assertTrue(saved.getId().startsWith("AD-"));
        assertEquals("Test issue from unit", saved.getDescription());
        assertEquals(Status.OPEN, saved.getStatus());
        assertNotNull(saved.getCreatedAt());

        // verify header check and update happened
        verify(getHeaderRequest, times(1)).execute();
        verify(updateRequest, times(1)).setValueInputOption("RAW");
        verify(updateRequest, times(1)).execute();

        // verify append was called with expected sheet and range
        verify(values).append(eq(spreadsheetId), eq("Issues!A:F"), appendBodyCaptor.capture());
        ValueRange appendBody = appendBodyCaptor.getValue();
        assertNotNull(appendBody);
        List<List<Object>> rows = appendBody.getValues();
        assertNotNull(rows);
        assertEquals(1, rows.size());
        List<Object> row = rows.get(0);

        assertEquals(saved.getId(), row.get(0));
        assertEquals(saved.getDescription(), row.get(1));
        assertEquals("", row.get(2));
        assertEquals(saved.getStatus().name(), row.get(3));
        assertNotNull(row.get(4)); // createdAt string
    }

    @Test
    void save_with_existing_rows_generatesNextIdCorrectly() throws Exception {
        // header exists
        ValueRange header = new ValueRange().setValues(List.of(List.of("ID")));
        when(values.get(eq(spreadsheetId), eq("Issues!A1:F1"))).thenReturn(getHeaderRequest);
        when(getHeaderRequest.execute()).thenReturn(header);

        // existing rows: header + AD-1 and AD-5
        List<List<Object>> existing = new ArrayList<>();
        existing.add(List.of("ID")); // header
        existing.add(List.of("AD-1", "one", "", "OPEN", "2025-01-01T00:00:00"));
        existing.add(List.of("AD-5", "two", "", "OPEN", "2025-01-02T00:00:00"));
        ValueRange all = new ValueRange().setValues(existing);
        when(values.get(eq(spreadsheetId), eq("Issues!A:F"))).thenReturn(getAllRequest);
        when(getAllRequest.execute()).thenReturn(all);

        when(values.append(eq(spreadsheetId), eq("Issues!A:F"), any(ValueRange.class))).thenReturn(appendRequest);
        when(appendRequest.setValueInputOption(anyString())).thenReturn(appendRequest);
        when(appendRequest.execute()).thenReturn(null);

        // Act
        Issue i = new Issue();
        i.setDescription("New issue");
        Issue saved = repo.save(i);

        // Next id should be AD-6 (max was 5)
        assertEquals("AD-6", saved.getId());
    }

    @Test
    void updateStatus_updatesCorrectRowAndUsesCorrectRange() throws Exception {
        // Prepare readRawRows return: header + one row AD-2
        List<List<Object>> rows = new ArrayList<>();
        rows.add(List.of("ID"));
        rows.add(List.of("AD-2", "desc", "", "OPEN", "2025-01-01T00:00:00")); // row index 1 -> sheet row 2
        ValueRange all = new ValueRange().setValues(rows);
        when(values.get(eq(spreadsheetId), eq("Issues!A:F"))).thenReturn(getAllRequest);
        when(getAllRequest.execute()).thenReturn(all);

        // update request behavior
        when(values.update(eq(spreadsheetId), eq("Issues!A2:F2"), any(ValueRange.class))).thenReturn(updateRequest);
        when(updateRequest.setValueInputOption(anyString())).thenReturn(updateRequest);
        when(updateRequest.execute()).thenReturn(null);

        // Act
        Issue updated = repo.updateStatus("AD-2", Status.CLOSED);

        // Assert
        assertNotNull(updated);
        assertEquals("AD-2", updated.getId());
        assertEquals(Status.CLOSED, updated.getStatus());
        // verify update called for the correct sheet range
        verify(values).update(eq(spreadsheetId), eq("Issues!A2:F2"), any(ValueRange.class));
    }

    @Test
    void updateStatus_whenNotFound_throwsNoSuchElement() throws Exception {
        // only header => no rows
        List<List<Object>> rows = new ArrayList<>();
        rows.add(List.of("ID"));
        ValueRange all = new ValueRange().setValues(rows);
        when(values.get(eq(spreadsheetId), eq("Issues!A:F"))).thenReturn(getAllRequest);
        when(getAllRequest.execute()).thenReturn(all);

        assertThrows(NoSuchElementException.class, () -> repo.updateStatus("MISSING", Status.CLOSED));
    }

    @Test
    void findAll_parsesRowsIntoIssues() throws Exception {
        // header + two rows
        List<List<Object>> rows = new ArrayList<>();
        rows.add(List.of("ID"));
        rows.add(List.of("AD-1", "desc1", "", "OPEN", "2025-01-01T10:00:00", "2025-01-01T11:00:00"));
        rows.add(List.of("AD-2", "desc2", "AD-1", "IN_PROGRESS", "2025-01-02T10:00:00", "2025-01-02T11:00:00"));
        ValueRange all = new ValueRange().setValues(rows);
        when(values.get(eq(spreadsheetId), eq("Issues!A:F"))).thenReturn(getAllRequest);
        when(getAllRequest.execute()).thenReturn(all);

        List<Issue> allIssues = repo.findAll();
        assertEquals(2, allIssues.size());
        Issue a = allIssues.get(0);
        assertEquals("AD-1", a.getId());
        assertEquals("desc1", a.getDescription());
        assertEquals(Status.OPEN, a.getStatus());
        Issue b = allIssues.get(1);
        assertEquals("AD-2", b.getId());
        assertEquals("AD-1", b.getParentId());
        assertEquals(Status.IN_PROGRESS, b.getStatus());
    }
}
