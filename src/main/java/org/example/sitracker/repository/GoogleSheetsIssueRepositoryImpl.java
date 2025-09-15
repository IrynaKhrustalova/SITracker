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

@Repository
public class GoogleSheetsIssueRepositoryImpl implements IssueRepository {
    private final Sheets sheets;
    private final String spreadsheetId;
    private final String sheetName = "Issues";
    private final DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final List<String> HEADER = List.of("ID", "Description", "Parent ID", "Status", "Created at", "Updated at");

    public GoogleSheetsIssueRepositoryImpl(Sheets sheets, String spreadsheetId) {
        this.sheets = sheets;
        this.spreadsheetId = spreadsheetId;
    }

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

    @Override
    public Optional<Issue> findById(String id) throws IOException {
        return findAll().stream().filter(i -> i.getId().equals(id)).findFirst();
    }

    // ------------- helpers --------------

    private List<List<Object>> readRawRows() throws IOException {
        ValueRange resp = sheets.spreadsheets().values().get(spreadsheetId, sheetName + "!A:F").execute();
        return resp.getValues();
    }

    private void ensureHeaderExists() throws IOException {
        ValueRange resp = sheets.spreadsheets().values().get(spreadsheetId, sheetName + "!A1:F1").execute();
        List<List<Object>> rows = resp.getValues();
        boolean ok = rows != null && !rows.isEmpty() && !rows.get(0).isEmpty() && "ID".equalsIgnoreCase(rows.get(0).get(0).toString());
        if (!ok) {
            ValueRange headerBody = new ValueRange().setValues(List.of(new ArrayList<>(HEADER)));
            sheets.spreadsheets().values().update(spreadsheetId, sheetName + "!A1:F1", headerBody).setValueInputOption("RAW").execute();
        }
    }

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

    private String getCell(List<Object> row, int idx) {
        if (row == null || row.size() <= idx) return "";
        Object v = row.get(idx);
        return v == null ? "" : v.toString();
    }

    private LocalDateTime parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try { return LocalDateTime.parse(s, dtf); } catch (Exception ex) { return null; }
    }

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
