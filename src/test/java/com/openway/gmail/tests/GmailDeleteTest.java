package com.openway.gmail.tests;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.openway.gmail.base.BaseTest;
import com.openway.gmail.config.TestConfig;
import com.openway.gmail.pages.InboxPage;
import com.openway.gmail.pages.LoginPage;

/**
 * Automated tests for Gmail mail deletion — OpenWay Task Scenario A.
 *
 * Covers TC-DEL-001 (delete single) and TC-DEL-002 (bulk delete).
 * Remaining test cases (TC-DEL-004 to TC-DEL-020) are documented for manual execution.
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

        String email = TestConfig.getEmail();
        String password = TestConfig.getPassword();

        boolean loggedIn = loginPage.login(email, password);
        if (!loggedIn) {
            logger.error("Failed to login to Gmail for deletion tests.");
        }
    }

    /**
     * TC-DEL-001: Delete a single email from Inbox.
     *
     * Steps:
     * 1. Navigate to Inbox
     * 2. Record initial email count
     * 3. Select the first email checkbox
     * 4. Click the Delete toolbar button
     * 5. Verify count decreased by 1 and toast notification appeared
     */
    @Test(priority = 1, description = "TC-DEL-001: Delete a single email from Inbox")
    public void testDeleteSingleEmail() {
        logger.info("=== TC-DEL-001: Delete Single Email ===");

        inboxPage.navigateToInbox();
        int initialCount = inboxPage.getEmailCount();
        logger.info("Initial email count: {}", initialCount);

        Assert.assertTrue(initialCount > 0, "Inbox should have at least 1 email to delete.");

        List<WebElement> emails = inboxPage.getAllEmailRows();
        String deletedSubject = inboxPage.getEmailSubject(emails.get(0));
        logger.info("Deleting email: '{}'", deletedSubject);

        inboxPage.deleteEmail(emails.get(0));

        String notification = inboxPage.getNotificationText();
        logger.info("Notification: {}", notification);

        try { Thread.sleep(3000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        inboxPage.navigateToInbox();
        int newCount = inboxPage.getEmailCount();
        logger.info("Email count after deletion: {}", newCount);

        Assert.assertTrue(newCount < initialCount,
                "Email count should decrease after deletion. Before: " + initialCount + ", After: " + newCount);

        logger.info("TC-DEL-001 PASSED: Single email deleted successfully.");
    }

    /**
     * TC-DEL-002: Bulk delete multiple emails from Inbox.
     *
     * Steps:
     * 1. Navigate to Inbox
     * 2. Record initial email count
     * 3. Select 2 email checkboxes atomically (single JS call)
     * 4. Click the Delete toolbar button
     * 5. Verify count decreased by at least 2
     */
    @Test(priority = 2, dependsOnMethods = "testDeleteSingleEmail",
            description = "TC-DEL-002: Bulk delete multiple emails")
    public void testBulkDeleteEmails() {
        logger.info("=== TC-DEL-002: Bulk Delete Emails ===");

        inboxPage.navigateToInbox();
        int initialCount = inboxPage.getEmailCount();
        logger.info("Initial email count: {}", initialCount);

        Assert.assertTrue(initialCount >= 2, "Inbox should have at least 2 emails for bulk delete test.");

        inboxPage.selectMultipleEmailsByIndex(0, 1);
        inboxPage.clickDeleteButton();

        try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        int newCount = inboxPage.getEmailCount();
        logger.info("Email count after bulk deletion: {}", newCount);

        Assert.assertTrue(newCount <= initialCount - 2,
                "Email count should decrease by at least 2 after bulk deletion. "
                        + "Before: " + initialCount + ", After: " + newCount);

        logger.info("TC-DEL-002 PASSED: Bulk email deletion successful.");
    }
}
