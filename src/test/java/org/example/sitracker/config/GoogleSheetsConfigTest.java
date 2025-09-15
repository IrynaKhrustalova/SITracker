package org.example.sitracker.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GoogleSheetsConfig using reflection to set private fields
 * and to invoke the private resolveCredentialsStream() helper.
 *
 * These tests avoid creating the real Sheets client and therefore do not
 * require real Google credentials or network access.
 */
class GoogleSheetsConfigTest {

    /**
     * Helper: set a private field on the target object via reflection.
     */
    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    /**
     * Test that spreadsheetId() returns the value stored in the private field.
     */
    @Test
    void spreadsheetIdBean_returnsInjectedValue() throws Exception {
        GoogleSheetsConfig cfg = new GoogleSheetsConfig();

        // set private field 'spreadsheetId'
        setPrivateField(cfg, "spreadsheetId", "SHEET-123");

        String result = cfg.spreadsheetId();
        assertEquals("SHEET-123", result);
    }

    /**
     * When credentialsPath is set to a real file, resolveCredentialsStream() should
     * return an InputStream that reads the file contents.
     */
    @Test
    void resolveCredentialsStream_usesCredentialsPath_whenProvided(@TempDir Path tmp) throws Exception {
        // create a temporary credentials file
        Path creds = tmp.resolve("creds.json");
        String sample = "{\"type\":\"service_account\",\"client_email\":\"x@example.com\"}";
        Files.writeString(creds, sample);

        GoogleSheetsConfig cfg = new GoogleSheetsConfig();
        // set credentialsPath private field to the temp file path
        setPrivateField(cfg, "credentialsPath", creds.toString());

        // invoke private method resolveCredentialsStream()
        Method m = GoogleSheetsConfig.class.getDeclaredMethod("resolveCredentialsStream");
        m.setAccessible(true);
        try (InputStream in = (InputStream) m.invoke(cfg)) {
            assertNotNull(in, "InputStream should not be null");
            byte[] read = in.readAllBytes();
            String got = new String(read);
            assertTrue(got.contains("\"type\":\"service_account\""));
        }
    }

    /**
     * If neither credentialsPath nor GOOGLE_APPLICATION_CREDENTIALS env var is set,
     * resolveCredentialsStream() should throw IllegalStateException.
     */
    @Test
    void resolveCredentialsStream_throws_whenNoCredentialsPresent() throws Exception {
        GoogleSheetsConfig cfg = new GoogleSheetsConfig();
        // ensure credentialsPath is null
        setPrivateField(cfg, "credentialsPath", null);

        Method m = GoogleSheetsConfig.class.getDeclaredMethod("resolveCredentialsStream");
        m.setAccessible(true);

        Exception ex = assertThrows(Exception.class, () -> m.invoke(cfg));
        // InvocationTargetException is thrown by reflection; unwrap to check cause
        Throwable cause = ex.getCause();
        assertNotNull(cause);
        assertTrue(cause instanceof IllegalStateException);
        assertTrue(cause.getMessage().contains("Google credentials not found"));
    }
}
