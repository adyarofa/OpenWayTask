package com.openway.gmail.tests;

import com.openway.gmail.base.BaseTest;
import com.openway.gmail.config.TestConfig;
import com.openway.gmail.pages.InboxPage;
import com.openway.gmail.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for Gmail Login and reading the last unread email.
 * <p>
 * This test performs the following scenario (as per task 2.2):
 * 1. Opens Google Chrome in a new window
 * 2. Navigates to https://mail.google.com/mail/
 * 3. Enters a login and password
 * 4. Writes the title of the last unread email in the mailbox to the log
 * <p>
 * Prerequisites:
 * - A test Gmail account must be registered
 * - The account must contain at least 5 unread emails
 * - Credentials must be set via environment variables:
 *   GMAIL_EMAIL and GMAIL_PASSWORD
 * - If the account has 2FA, use an App Password
 * <p>
 * Note on Google Verification:
 * Google may ask for additional verification (OTP, phone, etc.) especially
 * when logging in from a new device/location. To mitigate this:
 * - Use a dedicated test account with 2FA disabled
 * - Or generate an App Password (Settings > Security > App Passwords)
 * - Pre-authorize the test machine by logging in manually once
 * - Consider using Google's less secure app access (if available)
 */
public class GmailLoginTest extends BaseTest {

    private LoginPage loginPage;
    private InboxPage inboxPage;

    @BeforeClass(alwaysRun = true)
    @Override
    public void setUp() {
        super.setUp();
        loginPage = new LoginPage(driver);
        inboxPage = new InboxPage(driver);
    }

    /**
     * Test Case: Login to Gmail with valid credentials.
     * Corresponds to the prerequisite step of the task.
     */
    @Test(priority = 1, description = "Navigate to Gmail and login with valid credentials")
    public void testLoginToGmail() {
        logger.info("=== TEST: Login to Gmail ===");

        String email = TestConfig.getEmail();
        String password = TestConfig.getPassword();

        logger.info("Using test account: {}", email);

        // Step 1: Navigate to Gmail
        loginPage.navigateToGmail();
        logger.info("Step 1: Navigated to Gmail login page.");

        // Step 2: Enter email
        loginPage.enterEmail(email);
        logger.info("Step 2: Email entered.");

        // Step 3: Enter password
        loginPage.enterPassword(password);
        logger.info("Step 3: Password entered.");

        // Step 4: Check for verification challenges
        if (loginPage.isAdditionalVerificationRequired()) {
            logger.warn("=== ADDITIONAL VERIFICATION REQUIRED ===");
            logger.warn("Google is requesting additional verification.");
            logger.warn("This test may need manual intervention.");
            logger.warn("Consider: ");
            logger.warn("  1. Disabling 2-Step Verification on the test account");
            logger.warn("  2. Using an App Password");
            logger.warn("  3. Pre-authorizing this machine by logging in manually");
            Assert.fail("Login requires additional verification. "
                    + "Configure the test account to not require 2FA.");
        }

        // Step 5: Verify inbox loaded
        boolean inboxLoaded = loginPage.waitForInboxToLoad();
        Assert.assertTrue(inboxLoaded, "Gmail inbox should load after successful login.");

        logger.info("Login successful! Gmail inbox is loaded.");
    }

    /**
     * Test Case: Read and log the title of the last unread email.
     * This is the primary requirement of task 2.2.
     */
    @Test(priority = 2, dependsOnMethods = "testLoginToGmail",
            description = "Write the title of the last unread email to the log")
    public void testLogLastUnreadEmailTitle() {
        logger.info("=== TEST: Log Last Unread Email Title ===");

        // Navigate to inbox to ensure we're on the right page
        inboxPage.navigateToInbox();

        // Get the subject of the last unread email
        String lastUnreadSubject = inboxPage.getLastUnreadEmailSubject();

        // Assert that at least one unread email exists
        Assert.assertNotNull(lastUnreadSubject,
                "There should be at least one unread email in the inbox. "
                        + "Ensure the test account has at least 5 unread emails.");

        // Log the result prominently
        logger.info("╔══════════════════════════════════════════════════╗");
        logger.info("║  LAST UNREAD EMAIL TITLE:                       ║");
        logger.info("║  \"{}\"", lastUnreadSubject);
        logger.info("╚══════════════════════════════════════════════════╝");

        // Verify the subject is not empty
        Assert.assertFalse(lastUnreadSubject.isEmpty(),
                "The unread email subject should not be empty.");

        logger.info("Test passed: Last unread email title logged successfully.");
    }

    /**
     * Test Case: Verify inbox has at least 5 unread emails.
     * This validates the test setup requirement.
     */
    @Test(priority = 3, dependsOnMethods = "testLoginToGmail",
            description = "Verify the test account has at least 5 unread emails")
    public void testInboxHasMinimumUnreadEmails() {
        logger.info("=== TEST: Verify Minimum Unread Emails ===");

        int unreadCount = inboxPage.getUnreadEmailRows().size();
        logger.info("Number of unread emails found: {}", unreadCount);

        Assert.assertTrue(unreadCount >= 5,
                "Test account should have at least 5 unread emails. Found: " + unreadCount);

        logger.info("Test passed: Found {} unread emails (minimum 5 required).", unreadCount);
    }
}
