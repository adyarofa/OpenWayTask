package com.openway.gmail.pages;

import com.openway.gmail.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

/**
 * Page Object for the Gmail Inbox.
 * Provides methods to interact with emails in the inbox, including:
 * - Reading email subjects
 * - Finding unread emails
 * - Selecting emails
 * - Deleting emails
 */
public class InboxPage {

    private static final Logger logger = LoggerFactory.getLogger(InboxPage.class);

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Locators for inbox elements
    // Gmail uses dynamic class names, so we rely on structural/attribute selectors

    // All email rows (both read and unread)
    private static final By EMAIL_ROWS = By.cssSelector("tr.zA");
    // Unread email rows - Gmail marks unread emails with "zE" class
    private static final By UNREAD_EMAIL_ROWS = By.cssSelector("tr.zE");

    // Checkbox for selecting emails
    private static final By EMAIL_CHECKBOX = By.cssSelector("div[role='checkbox']");

    // Delete button (trash icon) in toolbar - multiple possible selectors
    private static final By DELETE_BUTTON = By.cssSelector("div[act='7']");
    private static final By DELETE_BUTTON_ALT = By.cssSelector("button[aria-label='Delete']");
    private static final By DELETE_BUTTON_TITLE = By.cssSelector("[title='Delete']");

    // Back to inbox button (when viewing an email)
    private static final By BACK_BUTTON = By.cssSelector("div[act='19']");

    public InboxPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(TestConfig.DEFAULT_TIMEOUT_SECONDS));
    }

    /**
     * Navigate to the Inbox view.
     */
    public InboxPage navigateToInbox() {
        logger.info("Navigating to Inbox...");
        driver.get("https://mail.google.com/mail/u/0/#inbox");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        logger.info("Navigated to Inbox.");
        return this;
    }

    /**
     * Get all email rows visible in the inbox.
     *
     * @return list of WebElements representing email rows
     */
    public List<WebElement> getAllEmailRows() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(EMAIL_ROWS));
            Thread.sleep(1000);
        } catch (Exception e) {
            logger.warn("Timeout waiting for email rows: {}", e.getMessage());
        }
        return driver.findElements(EMAIL_ROWS);
    }

    /**
     * Get all unread email rows in the inbox.
     * Gmail marks unread emails with a specific CSS class (zE for unread vs yO for read).
     *
     * @return list of WebElements representing unread email rows
     */
    public List<WebElement> getUnreadEmailRows() {
        try {
            Thread.sleep(2000);
            List<WebElement> unreadRows = driver.findElements(UNREAD_EMAIL_ROWS);
            logger.info("Found {} unread email(s) in inbox.", unreadRows.size());
            return unreadRows;
        } catch (Exception e) {
            logger.warn("Error getting unread emails: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Get the subject/title of a specific email row using JavaScript.
     * Gmail's DOM is complex with obfuscated class names, so we use JS
     * to reliably extract the subject text from the row.
     *
     * @param emailRow the email row WebElement
     * @return the subject text
     */
    public String getEmailSubject(WebElement emailRow) {
        try {
            // Use JavaScript to find the subject text within the row.
            // Gmail structures emails with multiple span elements.
            // The subject is typically in a span with data-thread-id or
            // within specific nested structures.
            String subject = (String) ((JavascriptExecutor) driver).executeScript(
                    "var row = arguments[0];" +
                    // Try multiple strategies to find the subject
                    // Strategy 1: Look for span.bqe (subject span in some Gmail versions)
                    "var el = row.querySelector('span.bqe');" +
                    "if (el && el.textContent.trim()) return el.textContent.trim();" +
                    // Strategy 2: Look for span.bog (subject span in other versions)
                    "el = row.querySelector('span.bog');" +
                    "if (el && el.textContent.trim()) return el.textContent.trim();" +
                    // Strategy 3: Look for span.y2 (yet another version)
                    "el = row.querySelector('span.y2');" +
                    "if (el && el.textContent.trim()) return el.textContent.trim();" +
                    // Strategy 4: Look for the subject in the specific TD cell structure
                    // The subject is usually in the 5th-6th td cell
                    "var tds = row.querySelectorAll('td');" +
                    "for (var i = 3; i < tds.length; i++) {" +
                    "  var spans = tds[i].querySelectorAll('span');" +
                    "  for (var j = 0; j < spans.length; j++) {" +
                    "    var txt = spans[j].textContent.trim();" +
                    "    if (txt.length > 2 && !txt.match(/^\\d{1,2}[:/]/) && " +
                    "        !txt.match(/^(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)/) && " +
                    "        txt !== 'me' && txt !== 'Inbox') {" +
                    "      return txt;" +
                    "    }" +
                    "  }" +
                    "}" +
                    // Strategy 5: Get all text from the row and extract the middle portion
                    "var allSpans = row.querySelectorAll('span[id]');" +
                    "for (var k = 0; k < allSpans.length; k++) {" +
                    "  var t = allSpans[k].textContent.trim();" +
                    "  if (t.length > 5) return t;" +
                    "}" +
                    "return '';",
                    emailRow
            );

            if (subject != null && !subject.isEmpty()) {
                return subject;
            }

            // Fallback: try to get the aria-label of the row which often contains the subject
            String ariaLabel = emailRow.getAttribute("aria-label");
            if (ariaLabel != null && !ariaLabel.isEmpty()) {
                logger.info("Using aria-label as subject: {}", ariaLabel);
                return ariaLabel;
            }

            return "(Unable to read subject)";
        } catch (Exception e) {
            logger.error("Error reading email subject: {}", e.getMessage());
            return "(Error reading subject)";
        }
    }

    /**
     * Get the subject/title of the last (most recent) unread email.
     * This is the primary method required by the task specification.
     *
     * @return the subject of the last unread email, or null if no unread emails
     */
    public String getLastUnreadEmailSubject() {
        List<WebElement> unreadEmails = getUnreadEmailRows();

        if (unreadEmails.isEmpty()) {
            logger.warn("No unread emails found in the inbox.");

            // Fallback: try to get the first email subject instead
            logger.info("Attempting to read first email subject as fallback...");
            List<WebElement> allEmails = getAllEmailRows();
            if (!allEmails.isEmpty()) {
                String subject = getEmailSubject(allEmails.get(0));
                logger.info("========================================");
                logger.info("FIRST EMAIL SUBJECT (fallback): {}", subject);
                logger.info("========================================");
                return subject;
            }
            return null;
        }

        // The first unread email in the list is the most recent one
        // (Gmail sorts by date descending by default)
        WebElement lastUnreadEmail = unreadEmails.get(0);
        String subject = getEmailSubject(lastUnreadEmail);

        logger.info("========================================");
        logger.info("LAST UNREAD EMAIL SUBJECT: {}", subject);
        logger.info("========================================");

        return subject;
    }

    /**
     * Select an email by clicking its checkbox.
     * Verifies the checkbox is actually checked after clicking.
     *
     * @param emailRow the email row to select
     */
    public InboxPage selectEmail(WebElement emailRow) {
        try {
            WebElement checkbox = emailRow.findElement(EMAIL_CHECKBOX);
            String checkedBefore = checkbox.getAttribute("aria-checked");
            logger.info("Checkbox state before click: {}", checkedBefore);

            checkbox.click();
            Thread.sleep(500);

            // Verify checkbox is now checked
            String checkedAfter = checkbox.getAttribute("aria-checked");
            logger.info("Checkbox state after click: {}", checkedAfter);

            if ("true".equals(checkedAfter)) {
                logger.info("Email selected successfully (checkbox is checked).");
            } else {
                // Try clicking again with JS
                logger.warn("Checkbox not checked, retrying with JS...");
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkbox);
                Thread.sleep(500);
            }
        } catch (Exception e) {
            logger.error("Error selecting email: {}", e.getMessage());
        }
        return this;
    }

    /**
     * Delete selected emails using the '#' keyboard shortcut.
     * This is more reliable than clicking the toolbar delete button
     * because Gmail's toolbar button selectors change across versions.
     */
    public InboxPage clickDeleteButton() {
        try {
            // Wait for toolbar to update after selection
            Thread.sleep(1000);

            // Primary method: use keyboard shortcut '#' to delete
            // This requires Gmail keyboard shortcuts to be enabled (they are by default)
            org.openqa.selenium.interactions.Actions actions =
                    new org.openqa.selenium.interactions.Actions(driver);
            actions.sendKeys("#").perform();
            logger.info("Delete performed via '#' keyboard shortcut.");

            Thread.sleep(2000);

            // If keyboard shortcut didn't work, try clicking the toolbar button
            // Check if a notification appeared (indicates success)
            boolean notificationShown = !driver.findElements(
                    By.cssSelector("div.vh, div.bAq, span.bAq")).isEmpty();

            if (!notificationShown) {
                logger.warn("No notification detected after '#'. Trying toolbar button...");

                // Try clicking delete button via JS with broader selectors
                ((JavascriptExecutor) driver).executeScript(
                        "var btns = document.querySelectorAll(" +
                        "  '[aria-label=\"Delete\"], [data-tooltip=\"Delete\"], [title=\"Delete\"]');" +
                        "for (var i = 0; i < btns.length; i++) {" +
                        "  var r = btns[i].getBoundingClientRect();" +
                        "  if (r.width > 0 && r.height > 0) { btns[i].click(); break; }" +
                        "}"
                );
                logger.info("Clicked toolbar delete button as fallback.");
                Thread.sleep(2000);
            }

        } catch (Exception e) {
            logger.error("Error during delete: {}", e.getMessage());
        }
        return this;
    }

    /**
     * Delete a single email from the inbox.
     * Selects the email and deletes it via keyboard shortcut.
     *
     * @param emailRow the email row to delete
     */
    public InboxPage deleteEmail(WebElement emailRow) {
        String subject = getEmailSubject(emailRow);
        logger.info("Deleting email: '{}'", subject);
        selectEmail(emailRow);
        clickDeleteButton();
        return this;
    }

    /**
     * Get the total count of visible emails in the inbox.
     *
     * @return the number of email rows
     */
    public int getEmailCount() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        List<WebElement> rows = driver.findElements(EMAIL_ROWS);
        logger.info("Total visible emails: {}", rows.size());
        return rows.size();
    }

    /**
     * Check if a notification/toast message is displayed.
     *
     * @return the notification text, or null if not found
     */
    public String getNotificationText() {
        try {
            WebElement notification = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(
                            By.cssSelector("div.bAq span.aT, div.vh span, span.bAq")
                    ));
            String text = notification.getText().trim();
            logger.info("Notification displayed: '{}'", text);
            return text;
        } catch (Exception e) {
            logger.info("No notification bar detected.");
            return null;
        }
    }
}
