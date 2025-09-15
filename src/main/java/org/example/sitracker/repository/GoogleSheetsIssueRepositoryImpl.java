package org.example.sitracker.repository;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.example.sitracker.domain.Issue;
import org.example.sitracker.domain.Status;
import org.springframework.stereotype.Repository;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of {@link IssueRepository} backed by a Google Sheets document.
 *
 * <p>The repository persists {@link Issue} rows into a sheet named {@code Issues}. Each row
 * contains columns in the order defined by {@link #HEADER}:
 * <pre>
 * ID | Description | Parent ID | Status | Created at | Updated at
 * </pre>
 *
 * <p>Concurrency: all public write operations are synchronized to avoid concurrent updates
 * to the same sheet from within this JVM instance.
 */
@Repository
public class GoogleSheetsIssueRepositoryImpl implements IssueRepository {
    private final Sheets sheets;
    private final String spreadsheetId;
    private final String sheetName = "Issues";
    private final DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final List<String> HEADER = List.of("ID", "Description", "Parent ID", "Status", "Created at", "Updated at");

    /**
     * Constructs a new repository bound to the given Sheets client and spreadsheet id.
     *
     * @param sheets        authenticated Google Sheets client
     * @param spreadsheetId id of the spreadsheet where issues are stored
     */
    public GoogleSheetsIssueRepositoryImpl(Sheets sheets, String spreadsheetId) {
        this.sheets = sheets;
        this.spreadsheetId = spreadsheetId;
    }

    /**
     * Saves a new {@link Issue} into the sheet.
     * <ul>
     *     <li>If the issue has no id, a new one is generated with prefix {@code AD-}.</li>
     *     <li>If {@code createdAt} is null, it is set to {@link LocalDateTime#now()}.</li>
     *     <li>If {@code status} is null, it defaults to {@link Status#OPEN}.</li>
     * </ul>
     *
     * @param issue issue to persist
     * @return the saved issue (with id and timestamps populated if needed)
     * @throws IOException if the Sheets API call fails
     */
    @Override
    public synchronized Issue save(Issue issue) throws IOException {
        ensureHeaderExists();

        if (issue.getId() == null || issue.getId().isBlank()) {
            issue.setId(generateNextId());
        }
        LocalDateTime now = LocalDateTime.now();
        if (issue.getCreatedAt() == null) issue.setCreatedAt(now);
        if (issue.getStatus() == null) issue.setStatus(Status.OPEN);

        List<Object> row = List.of(
                issue.getId(),
                issue.getDescription(),
                issue.getParentId() == null ? "" : issue.getParentId(),
                issue.getStatus().name(),
                issue.getCreatedAt().format(dtf)
        );

        ValueRange body = new ValueRange().setValues(List.of(row));
        sheets.spreadsheets().values()
                .append(spreadsheetId, sheetName + "!A:F", body)
                .setValueInputOption("USER_ENTERED")
                .execute();
        return issue;
    }

    /**
     * Updates the status of an existing issue in the sheet.
     *
     * @param id        issue id to update
     * @param newStatus new status
     * @return updated issue object
     * @throws IOException             if Sheets API call fails
     * @throws NoSuchElementException  if the issue id cannot be found
     */
    @Override
    public synchronized Issue updateStatus(String id, Status newStatus) throws IOException {
        List<List<Object>> rows = readRawRows();
        if (rows == null || rows.size() <= 1) throw new NoSuchElementException("No issues present");

        int foundRow = -1;
        for (int i = 1; i < rows.size(); i++) {
            List<Object> r = rows.get(i);
            if (!r.isEmpty()) {
                String curId = r.get(0).toString();
                if (id.equals(curId)) {
                    foundRow = i + 1;
                    break;
                }
            }
        }
        if (foundRow == -1) throw new NoSuchElementException("Issue not found: " + id);

        Issue issue = parseRowToIssue(rows.get(foundRow - 1));
        issue.setStatus(newStatus);
        issue.setUpdatedAt(LocalDateTime.now());

        List<Object> updatedRow = List.of(
                issue.getId(),
                issue.getDescription(),
                issue.getParentId() == null ? "" : issue.getParentId(),
                issue.getStatus().name(),
                issue.getCreatedAt() == null ? "" : issue.getCreatedAt().format(dtf),
                issue.getUpdatedAt().format(dtf)
        );

        ValueRange body = new ValueRange().setValues(List.of(updatedRow));
        String range = String.format("%s!A%d:F%d", sheetName, foundRow, foundRow);
        sheets.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("USER_ENTERED")
                .execute();

        return issue;
    }

    @Override
    public List<Issue> findByStatus(Status status) throws IOException {
        List<Issue> all = findAll();
        return all.stream().filter(i -> i.getStatus() == status).collect(Collectors.toList());
    }

    @Override
    public List<Issue> findAll() throws IOException {
        List<List<Object>> rows = readRawRows();
        if (rows == null || rows.size() <= 1) return Collections.emptyList();
        return rows.stream()
                .skip(1)
                .map(this::parseRowToIssue)
                .collect(Collectors.toList());
    }

    // ------------- helpers --------------

    /**
     * Reads all raw rows from the sheet (range A:F).
     *
     * @return list of rows; may be {@code null} if sheet is empty
     * @throws IOException if Sheets API call fails
     */
    private List<List<Object>> readRawRows() throws IOException {
        ValueRange resp = sheets.spreadsheets().values().get(spreadsheetId, sheetName + "!A:F").execute();
        return resp.getValues();
    }

    /**
     * Ensures the header row (A1:F1) exists and matches {@link #HEADER}.
     *
     * @throws IOException if Sheets API call fails
     */
    private void ensureHeaderExists() throws IOException {
        ValueRange resp = sheets.spreadsheets().values().get(spreadsheetId, sheetName + "!A1:F1").execute();
        List<List<Object>> rows = resp.getValues();
        boolean ok = rows != null && !rows.isEmpty() && !rows.get(0).isEmpty() && "ID".equalsIgnoreCase(rows.get(0).get(0).toString());
        if (!ok) {
            ValueRange headerBody = new ValueRange().setValues(List.of(new ArrayList<>(HEADER)));
            sheets.spreadsheets().values().update(spreadsheetId, sheetName + "!A1:F1", headerBody).setValueInputOption("RAW").execute();
        }
    }

    /**
     * Converts a raw row of sheet values into an {@link Issue}.
     *
     * @param row list of cell values (0–5 columns)
     * @return parsed issue
     */
    private Issue parseRowToIssue(List<Object> row) {
        String id = getCell(row, 0);
        String desc = getCell(row, 1);
        String parent = getCell(row, 2);
        String statusStr = getCell(row, 3);
        String createdStr = getCell(row, 4);
        String updatedStr = getCell(row, 5);

        Status status = Status.OPEN;
        try { if (statusStr != null && !statusStr.isBlank()) status = Status.valueOf(statusStr.trim()); } catch (Exception ignored) {}

        LocalDateTime created = parseDate(createdStr);
        LocalDateTime updated = parseDate(updatedStr);

        Issue issue = new Issue();
        issue.setId(id);
        issue.setDescription(desc);
        issue.setParentId(parent == null || parent.isBlank() ? null : parent);
        issue.setStatus(status);
        issue.setCreatedAt(created);
        issue.setUpdatedAt(updated);
        return issue;
    }

    /**
     * Safely extracts a string cell value from a row.
     *
     * @param row row list
     * @param idx cell index
     * @return cell value as string, or empty string if not present
     */
    private String getCell(List<Object> row, int idx) {
        if (row == null || row.size() <= idx) return "";
        Object v = row.get(idx);
        return v == null ? "" : v.toString();
    }

    /**
     * Parses a date string into {@link LocalDateTime} using {@link #dtf}.
     *
     * @param s date string, may be {@code null} or blank
     * @return parsed {@link LocalDateTime}, or {@code null} if parse fails
     */
    private LocalDateTime parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try { return LocalDateTime.parse(s, dtf); } catch (Exception ex) { return null; }
    }

    /**
     * Generates the next sequential issue id with prefix {@code AD-}.
     * Scans the sheet for the maximum numeric suffix and increments it.
     *
     * @return next id string (e.g., {@code AD-1}, {@code AD-2}, …)
     * @throws IOException if Sheets API call fails
     */
    private String generateNextId() throws IOException {
        List<List<Object>> rows = readRawRows();
        int max = 0;
        if (rows != null) {
            for (int i = 1; i < rows.size(); i++) {
                List<Object> row = rows.get(i);
                if (!row.isEmpty()) {
                    String id = row.get(0).toString();
                    if (id != null && id.contains("-")) {
                        String[] parts = id.split("-");
                        try { int n = Integer.parseInt(parts[parts.length - 1]); max = Math.max(max, n); } catch (NumberFormatException ignore) {}
                    }
                }
            }
        }
        return "AD-" + (max + 1);
    }
}
