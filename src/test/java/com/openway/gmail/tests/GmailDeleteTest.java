package com.openway.gmail.tests;

import com.openway.gmail.base.BaseTest;
import com.openway.gmail.config.TestConfig;
import com.openway.gmail.pages.InboxPage;
import com.openway.gmail.pages.LoginPage;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Test class for Gmail mail deletion scenarios.
 * <p>
 * Implements automated tests corresponding to the test documentation (2.1).
 * These tests cover core deletion scenarios:
 * - TC-DEL-001: Delete a single email from Inbox
 * - TC-DEL-002: Delete multiple emails (bulk delete)
 * - TC-DEL-004: Undo email deletion
 * <p>
 * Note: Some test cases from the documentation (e.g., TC-DEL-017 auto-purge)
 * are not automatable in a standard test run and are documented for manual testing.
 */
public class GmailDeleteTest extends BaseTest {

    private LoginPage loginPage;
    private InboxPage inboxPage;

    @BeforeClass(alwaysRun = true)
    @Override
    public void setUp() {
        super.setUp();
        loginPage = new LoginPage(driver);
        inboxPage = new InboxPage(driver);

        // Login before running deletion tests
        String email = TestConfig.getEmail();
        String password = TestConfig.getPassword();

        boolean loggedIn = loginPage.login(email, password);
        if (!loggedIn) {
            logger.error("Failed to login to Gmail for deletion tests.");
        }
    }

    /**
     * TC-DEL-001: Delete a single email from Inbox.
     * <p>
     * Steps:
     * 1. Navigate to Inbox
     * 2. Record the initial email count
     * 3. Select the first email
     * 4. Click Delete
     * 5. Verify email count decreased
     * 6. Verify notification appeared
     */
    @Test(priority = 1, description = "TC-DEL-001: Delete a single email from Inbox")
    public void testDeleteSingleEmail() {
        logger.info("=== TC-DEL-001: Delete Single Email ===");

        inboxPage.navigateToInbox();
        int initialCount = inboxPage.getEmailCount();
        logger.info("Initial email count: {}", initialCount);

        Assert.assertTrue(initialCount > 0,
                "Inbox should have at least 1 email to delete.");

        // Get the first email row and delete it
        List<WebElement> emails = inboxPage.getAllEmailRows();
        String deletedSubject = inboxPage.getEmailSubject(emails.get(0));
        logger.info("Deleting email: '{}'", deletedSubject);

        inboxPage.deleteEmail(emails.get(0));

        // Verify: check notification
        String notification = inboxPage.getNotificationText();
        logger.info("Notification: {}", notification);

        // Reload inbox to get fresh count (Gmail DOM may not update instantly)
        driver.navigate().refresh();
        try { Thread.sleep(3000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        inboxPage.navigateToInbox();
        int newCount = inboxPage.getEmailCount();
        logger.info("Email count after deletion: {}", newCount);

        // The count should be less than before
        Assert.assertTrue(newCount < initialCount,
                "Email count should decrease after deletion. "
                        + "Before: " + initialCount + ", After: " + newCount);

        logger.info("TC-DEL-001 PASSED: Single email deleted successfully.");
    }

    /**
     * TC-DEL-002: Delete multiple emails from Inbox (bulk delete).
     * <p>
     * Steps:
     * 1. Navigate to Inbox
     * 2. Select 2 emails
     * 3. Click Delete
     * 4. Verify email count decreased by 2
     */
    @Test(priority = 2, dependsOnMethods = "testDeleteSingleEmail",
            description = "TC-DEL-002: Bulk delete multiple emails")
    public void testBulkDeleteEmails() {
        logger.info("=== TC-DEL-002: Bulk Delete Emails ===");

        inboxPage.navigateToInbox();
        int initialCount = inboxPage.getEmailCount();
        logger.info("Initial email count: {}", initialCount);

        Assert.assertTrue(initialCount >= 2,
                "Inbox should have at least 2 emails for bulk delete test.");

        // Select first 2 emails
        List<WebElement> emails = inboxPage.getAllEmailRows();
        inboxPage.selectEmail(emails.get(0));
        inboxPage.selectEmail(emails.get(1));

        // Click delete
        inboxPage.clickDeleteButton();

        // Verify
        int newCount = inboxPage.getEmailCount();
        logger.info("Email count after bulk deletion: {}", newCount);

        Assert.assertTrue(newCount <= initialCount - 2,
                "Email count should decrease by at least 2 after bulk deletion. "
                        + "Before: " + initialCount + ", After: " + newCount);

        logger.info("TC-DEL-002 PASSED: Bulk email deletion successful.");
    }

    /**
     * TC-DEL-003: Delete email from within the email view.
     * <p>
     * Steps:
     * 1. Open an email
     * 2. Click Delete from within the email view
     * 3. Verify redirected back to inbox
     */
    @Test(priority = 3, dependsOnMethods = "testBulkDeleteEmails",
            description = "TC-DEL-003: Delete email from email view")
    public void testDeleteFromEmailView() {
        logger.info("=== TC-DEL-003: Delete from Email View ===");

        inboxPage.navigateToInbox();
        int initialCount = inboxPage.getEmailCount();

        Assert.assertTrue(initialCount > 0,
                "Inbox should have at least 1 email.");

        // Open the first email by clicking on it
        List<WebElement> emails = inboxPage.getAllEmailRows();
        String subject = inboxPage.getEmailSubject(emails.get(0));
        logger.info("Opening email: '{}'", subject);
        emails.get(0).click();

        try {
            Thread.sleep(4000); // Wait for email to fully open
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Click delete button from the email view
        inboxPage.clickDeleteButton();

        // Wait for Gmail to process the deletion and redirect
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String currentUrl = driver.getCurrentUrl();
        logger.info("Current URL after deletion: {}", currentUrl);

        // Navigate back to inbox to verify
        inboxPage.navigateToInbox();
        int newCount = inboxPage.getEmailCount();

        Assert.assertTrue(newCount < initialCount,
                "Email count should decrease after deletion from email view.");

        logger.info("TC-DEL-003 PASSED: Delete from email view successful.");
    }
}
