package com.openway.gmail.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Configuration class that reads Gmail test credentials and settings.
 * <p>
 * Priority: System Properties > Environment Variables > .env file
 * <p>
 * This ensures the test can work on any computer by simply setting
 * the appropriate environment variables or placing a .env file in
 * src/test/resources/.env
 */
public class TestConfig {

    private static final String ENV_GMAIL_EMAIL = "GMAIL_EMAIL";
    private static final String ENV_GMAIL_PASSWORD = "GMAIL_PASSWORD";
    private static final String PROP_GMAIL_EMAIL = "gmail.email";
    private static final String PROP_GMAIL_PASSWORD = "gmail.password";

    public static final String GMAIL_URL = "https://mail.google.com/mail/";
    public static final int DEFAULT_TIMEOUT_SECONDS = 30;
    public static final int LONG_TIMEOUT_SECONDS = 60;

    private static final Properties envProperties = new Properties();

    // Load .env file on class initialization
    static {
        loadEnvFile();
    }

    /**
     * Load properties from the .env file located in classpath (src/test/resources/.env).
     */
    private static void loadEnvFile() {
        // Try loading from classpath first
        try (InputStream is = TestConfig.class.getClassLoader().getResourceAsStream(".env")) {
            if (is != null) {
                envProperties.load(is);
                System.out.println("[TestConfig] Loaded credentials from .env file (classpath).");
                return;
            }
        } catch (IOException e) {
            // Fall through to file-based loading
        }

        // Try loading from project root
        Path envPath = Paths.get("src/test/resources/.env");
        if (Files.exists(envPath)) {
            try (InputStream is = Files.newInputStream(envPath)) {
                envProperties.load(is);
                System.out.println("[TestConfig] Loaded credentials from " + envPath);
            } catch (IOException e) {
                System.err.println("[TestConfig] Warning: Could not read .env file: " + e.getMessage());
            }
        }
    }

    /**
     * Get a config value with priority: System Property > Env Variable > .env file.
     */
    private static String getConfigValue(String sysPropKey, String envKey) {
        // 1. System property (e.g., -Dgmail.email=...)
        String value = System.getProperty(sysPropKey);
        if (value != null && !value.isEmpty()) return value;

        // 2. Environment variable (e.g., export GMAIL_EMAIL=...)
        value = System.getenv(envKey);
        if (value != null && !value.isEmpty()) return value;

        // 3. .env file
        value = envProperties.getProperty(envKey);
        if (value != null && !value.isEmpty()) return value;

        return null;
    }

    /**
     * Get the Gmail email address for testing.
     *
     * @return the email address
     * @throws IllegalStateException if no email is configured
     */
    public static String getEmail() {
        String email = getConfigValue(PROP_GMAIL_EMAIL, ENV_GMAIL_EMAIL);
        if (email == null || email.isEmpty()) {
            throw new IllegalStateException(
                    "Gmail email not configured. Either:\n"
                            + "  1. Create src/test/resources/.env with GMAIL_EMAIL=your@gmail.com\n"
                            + "  2. Set environment variable: export GMAIL_EMAIL=\"test@gmail.com\"\n"
                            + "  3. Pass system property: -Dgmail.email=\"test@gmail.com\""
            );
        }
        return email;
    }

    /**
     * Get the Gmail password for testing.
     *
     * @return the password
     * @throws IllegalStateException if no password is configured
     */
    public static String getPassword() {
        String password = getConfigValue(PROP_GMAIL_PASSWORD, ENV_GMAIL_PASSWORD);
        if (password == null || password.isEmpty()) {
            throw new IllegalStateException(
                    "Gmail password not configured. Either:\n"
                            + "  1. Create src/test/resources/.env with GMAIL_PASSWORD=your-password\n"
                            + "  2. Set environment variable: export GMAIL_PASSWORD=\"password\"\n"
                            + "  3. Pass system property: -Dgmail.password=\"password\""
            );
        }
        return password;
    }
}
