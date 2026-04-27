package com.openway.gmail.tests;

import com.openway.gmail.base.BaseTest;
import com.openway.gmail.config.TestConfig;
import com.openway.gmail.pages.InboxPage;
import com.openway.gmail.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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

    @Test(priority = 1, description = "Navigate to Gmail and login with valid credentials")
    public void testLoginToGmail() {
        logger.info("=== TEST: Login to Gmail ===");

        String email = TestConfig.getEmail();
        String password = TestConfig.getPassword();

        logger.info("Using test account: {}", email);

        loginPage.navigateToGmail();
        loginPage.enterEmail(email);
        loginPage.enterPassword(password);

        if (loginPage.isAdditionalVerificationRequired()) {
            logger.warn("=== ADDITIONAL VERIFICATION REQUIRED ===");
            Assert.fail("Login requires additional verification. "
                    + "Configure the test account to not require 2FA.");
        }

        boolean inboxLoaded = loginPage.waitForInboxToLoad();
        Assert.assertTrue(inboxLoaded, "Gmail inbox should load after successful login.");

        logger.info("Login successful! Gmail inbox is loaded.");
    }

    @Test(priority = 2, dependsOnMethods = "testLoginToGmail",
            description = "Write the title of the last unread email to the log")
    public void testLogLastUnreadEmailTitle() {
        logger.info("=== TEST: Log Last Unread Email Title ===");

        inboxPage.navigateToInbox();

        String lastUnreadSubject = inboxPage.getLastUnreadEmailSubject();

        Assert.assertNotNull(lastUnreadSubject,
                "There should be at least one unread email in the inbox.");

        logger.info("╔══════════════════════════════════════════════════╗");
        logger.info("║  LAST UNREAD EMAIL TITLE:                       ║");
        logger.info("║  \"{}\"", lastUnreadSubject);
        logger.info("╚══════════════════════════════════════════════════╝");

        Assert.assertFalse(lastUnreadSubject.isEmpty(),
                "The unread email subject should not be empty.");

        logger.info("Test passed: Last unread email title logged successfully.");
    }

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
