package com.openway.gmail.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class TestConfig {

    private static final String ENV_GMAIL_EMAIL = "GMAIL_EMAIL";
    private static final String ENV_GMAIL_PASSWORD = "GMAIL_PASSWORD";
    private static final String PROP_GMAIL_EMAIL = "gmail.email";
    private static final String PROP_GMAIL_PASSWORD = "gmail.password";

    public static final String GMAIL_URL = "https://mail.google.com/mail/";
    public static final int DEFAULT_TIMEOUT_SECONDS = 30;
    public static final int LONG_TIMEOUT_SECONDS = 60;

    private static final Properties envProperties = new Properties();

    static {
        loadEnvFile();
    }

    private static void loadEnvFile() {
        try (InputStream is = TestConfig.class.getClassLoader().getResourceAsStream(".env")) {
            if (is != null) {
                envProperties.load(is);
                System.out.println("[TestConfig] Loaded credentials from .env file (classpath).");
                return;
            }
        } catch (IOException e) {
            System.err.println("[TestConfig] Error reading .env file from classpath: " + e.getMessage());
        }

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

    private static String getConfigValue(String sysPropKey, String envKey) {
        String value = System.getProperty(sysPropKey);
        if (value != null && !value.isEmpty()) return value;

        value = System.getenv(envKey);
        if (value != null && !value.isEmpty()) return value;

        value = envProperties.getProperty(envKey);
        if (value != null && !value.isEmpty()) return value;

        return null;
    }

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
