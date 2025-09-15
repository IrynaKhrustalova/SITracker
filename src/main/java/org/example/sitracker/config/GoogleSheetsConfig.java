package org.example.sitracker.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Spring configuration that creates Google Sheets related beans.
 *
 * <p>It exposes two beans:
 * <ul>
 *     <li>{@link Sheets} — an authenticated client for the Google Sheets API</li>
 *     <li>{@link String} (named {@code spreadsheetId}) — the spreadsheet id used by the app</li>
 * </ul>
 *
 * <p>How credentials are resolved:
 * <ol>
 *     <li>If the Spring property {@code sitracker.google.credentials} is set, its value is treated
 *     as a filesystem path to the service account JSON and will be used.</li>
 *     <li>Otherwise, the {@code GOOGLE_APPLICATION_CREDENTIALS} environment variable is consulted.</li>
 *     <li>If neither is present the configuration fails with an {@link IllegalStateException}.</li>
 * </ol>
 *
 * <p>In Docker / runtime, prefer mounting the credentials file into the container and set the
 * environment variable or the Spring property (see README examples).
 */
@Configuration
public class GoogleSheetsConfig {

    /**
     * Optional path to the service account credentials JSON. Read from
     * {@code sitracker.google.credentials} Spring property if present.
     */
    @Value("${sitracker.google.credentials:#{null}}")
    private String credentialsPath;

    /**
     * Spreadsheet id (required). Read from {@code sitracker.spreadsheet.id}.
     */
    @Value("${sitracker.spreadsheet.id}")
    private String spreadsheetId;

    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Creates and returns a configured {@link Sheets} client using a service account.
     *
     * @return configured Sheets client
     * @throws Exception if credentials cannot be read or the Sheets client cannot be created
     */
    @Bean
    public Sheets sheetsService() throws Exception {
        InputStream credentialsStream = resolveCredentialsStream();
        ServiceAccountCredentials creds = (ServiceAccountCredentials) ServiceAccountCredentials.fromStream(credentialsStream)
                .createScoped(List.of("https://www.googleapis.com/auth/spreadsheets"));

        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                new HttpCredentialsAdapter(creds)
        ).setApplicationName("SiTracker").build();
    }

    /**
     * Exposes the spreadsheet id as a Spring bean so it can be injected where needed.
     *
     * @return the spreadsheet id string (never null/blank)
     */
    @Bean
    public String spreadsheetId() {
        return spreadsheetId;
    }

    /**
     * Resolves the credentials JSON input stream.
     *
     * <p>Resolution order:
     * <ol>
     *     <li>Use {@code credentialsPath} (value of {@code sitracker.google.credentials}) if provided</li>
     *     <li>Otherwise use the {@code GOOGLE_APPLICATION_CREDENTIALS} environment variable</li>
     * </ol>
     *
     * @return an {@link InputStream} to the credentials JSON; caller is responsible for closing it
     * @throws Exception if no credentials path is available or the file cannot be opened
     */
    private InputStream resolveCredentialsStream() throws Exception {
        String path = credentialsPath != null ? credentialsPath : System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (path != null && !path.isBlank()) {
            return new FileInputStream(path);
        }
        throw new IllegalStateException("Google credentials not found. Set sitracker.google.credentials or env GOOGLE_APPLICATION_CREDENTIALS");
    }
}
