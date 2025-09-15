package org.example.sitracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the SiTracker application.
 *
 * <p>This class bootstraps Spring Boot, which initializes the application
 * context, configures beans (such as the Google Sheets client), and then
 * invokes {@link CliRunner} if command-line arguments are present.
 *
 * <p>Run this class to start the CLI application:
 * <pre>
 *   mvn spring-boot:run -Dspring-boot.run.arguments="create -d 'Test issue'"
 * </pre>
 */
@SpringBootApplication
public class SiTrackerApplication {

    /**
     * Starts the Spring Boot application and delegates to {@link CliRunner}.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(SiTrackerApplication.class, args);
    }
}
