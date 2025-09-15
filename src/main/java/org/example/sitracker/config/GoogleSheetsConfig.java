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

@Configuration
public class GoogleSheetsConfig {

    @Value("${sitracker.google.credentials:#{null}}")
    private String credentialsPath;

    @Value("${sitracker.spreadsheet.id}")
    private String spreadsheetId;

    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

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

    @Bean
    public String spreadsheetId() {
        return spreadsheetId;
    }

    private InputStream resolveCredentialsStream() throws Exception {
        String path = credentialsPath != null ? credentialsPath : System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (path != null && !path.isBlank()) {
            return new FileInputStream(path);
        }
        throw new IllegalStateException("Google credentials not found. Set sitracker.google.credentials or env GOOGLE_APPLICATION_CREDENTIALS");
    }
}
