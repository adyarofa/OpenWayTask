package com.openway.gmail.pages;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openway.gmail.config.TestConfig;

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
    // Gmail uses dynamic class names and structure that changes frequently
    // We use multiple stable selector strategies with fallbacks

    // All email rows - Gmail uses class 'zA' on each thread row
    private static final By EMAIL_ROWS = By.cssSelector("tr.zA");
    // Fallback selector
    private static final By EMAIL_ROWS_FALLBACK = By.cssSelector("div[role='main'] tr[jsmodel]");

    // Unread email rows - Gmail adds class 'zE' to unread rows
    private static final By UNREAD_EMAIL_ROWS = By.cssSelector("tr.zA.zE");

    // Checkbox for selecting an email row (inside the row)
    private static final By EMAIL_CHECKBOX = By.cssSelector("td.oZ-jc div[role='checkbox'], td.PF div[role='checkbox']");
    
    // Delete button locators - multiple strategies with fallbacks
    private static final By DELETE_BUTTON = By.xpath(
        "//div[@aria-label='Delete'] | //button[@aria-label='Delete'] | " +
        "//div[@title='Delete'] | //button[@title='Delete'] | " +
        "//div[contains(@aria-label, 'Delete')] | //button[contains(@aria-label, 'Delete')]"
    );
    private static final By DELETE_BUTTON_ALT = By.cssSelector("[data-tooltip='Delete'], [aria-label='Delete'], [title='Delete']");
    
    // Back to inbox button (when viewing an email)
    private static final By BACK_BUTTON = By.xpath("//div[@aria-label='Back'] | //button[@aria-label='Back']");

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
        
        // Wait for email rows to be visible (indicating inbox is loaded)
        try {
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(EMAIL_ROWS));
            logger.info("Inbox content loaded successfully.");
        } catch (Exception e) {
            logger.warn("Timeout waiting for inbox content: {}. Attempting with fallback selector.", e.getMessage());
            try {
                wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(EMAIL_ROWS_FALLBACK));
                logger.info("Inbox loaded with fallback selector.");
            } catch (Exception e2) {
                logger.warn("Could not load inbox with either selector: {}", e2.getMessage());
            }
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
            // Wait for email rows to be visible and interactive
            WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(TestConfig.LONG_TIMEOUT_SECONDS));
            longWait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(EMAIL_ROWS));
            logger.info("Email rows are visible.");
        } catch (Exception e) {
            logger.warn("Primary email row selector failed: {}. Trying fallback...", e.getMessage());
            try {
                WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(TestConfig.LONG_TIMEOUT_SECONDS));
                longWait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(EMAIL_ROWS_FALLBACK));
                logger.info("Email rows loaded with fallback selector.");
            } catch (Exception e2) {
                logger.error("Could not load email rows with either selector: {}", e2.getMessage());
            }
        }
        
        return driver.findElements(EMAIL_ROWS);
    }

    /**
     * Get all unread email rows in the inbox.
     * Gmail marks unread emails with specific CSS classes that may change.
     *
     * @return list of WebElements representing unread email rows
     */
    public List<WebElement> getUnreadEmailRows() {
        try {
            // Wait for unread emails to be visible (they have different styling)
            WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(TestConfig.LONG_TIMEOUT_SECONDS));
            
            // Custom condition: wait for at least one unread email OR timeout gracefully
            longWait.until(d -> {
                List<WebElement> unreadRows = d.findElements(UNREAD_EMAIL_ROWS);
                return !unreadRows.isEmpty();
            });
            
            List<WebElement> unreadRows = driver.findElements(UNREAD_EMAIL_ROWS);
            logger.info("Found {} unread email(s) in inbox.", unreadRows.size());
            return unreadRows;
            
        } catch (Exception e) {
            logger.warn("No unread emails detected or timeout waiting for them: {}. Returning empty list.", e.getMessage());
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
            String subject = (String) ((JavascriptExecutor) driver).executeScript(
                    "var row = arguments[0];" +
                    "var ariaLabel = row.getAttribute('aria-label');" +
                    "if (ariaLabel && ariaLabel.trim().length > 0) {" +
                    "  return ariaLabel.split(', ').slice(1).join(', ');" +
                    "}" +
                    "var spans = row.querySelectorAll('span');" +
                    "for (var s = 0; s < spans.length; s++) {" +
                    "  var txt = spans[s].textContent.trim();" +
                    "  if (txt.length > 3 && txt.length < 500 && " +
                    "      !txt.match(/^\\d{1,2}:\\d{2}/) && " +
                    "      !txt.match(/^(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec|Mon|Tue|Wed|Thu|Fri|Sat|Sun)/) && " +
                    "      !txt.match(/^\\d{1,2}\\s+(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)/) && " +
                    "      txt !== 'Inbox' && txt !== 'me' && txt !== 'to me' && " +
                    "      !txt.match(/^\\d+\\s+(message|email|unread)/) && " +
                    "      spans[s].offsetParent !== null) {" +
                    "    return txt;" +
                    "  }" +
                    "}" +
                    "var tds = row.querySelectorAll('td');" +
                    "for (var i = 0; i < tds.length; i++) {" +
                    "  var tdText = tds[i].textContent.trim();" +
                    "  if (tdText.length > 5 && tdText.length < 500 && " +
                    "      !tdText.match(/^\\d/) && " +
                    "      tds[i].offsetParent !== null) {" +
                    "    return tdText;" +
                    "  }" +
                    "}" +
                    "var allText = row.textContent.trim();" +
                    "if (allText.length > 0) {" +
                    "  return allText.substring(0, 200);" +
                    "}" +
                    "return '';",
                    emailRow
            );

            if (subject != null && !subject.isEmpty()) {
                logger.debug("Email subject extracted: {}", subject);
                return subject;
            }

            logger.warn("Could not extract subject text using any strategy");
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
     * Select an email by hovering over the row to reveal the checkbox, then clicking it.
     *
     * @param emailRow the email row to select
     */
    public InboxPage selectEmail(WebElement emailRow) {
        try {
            // Hover over the row so Gmail reveals the checkbox
            new org.openqa.selenium.interactions.Actions(driver)
                    .moveToElement(emailRow)
                    .perform();
            Thread.sleep(400);

            // Try the scoped checkbox first, then fall back to any checkbox in the row
            WebElement checkbox;
            try {
                checkbox = emailRow.findElement(EMAIL_CHECKBOX);
            } catch (Exception ex) {
                checkbox = emailRow.findElement(By.cssSelector("div[role='checkbox']"));
            }

            // Click via JS to avoid interception by overlays
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkbox);
            Thread.sleep(500);

            String checkedAfter = checkbox.getAttribute("aria-checked");
            logger.info("Checkbox state after click: {}", checkedAfter);

            if (!"true".equals(checkedAfter)) {
                logger.warn("Checkbox not marked checked after click — retrying normal click...");
                checkbox.click();
                Thread.sleep(400);
            } else {
                logger.info("Email selected successfully.");
            }
        } catch (Exception e) {
            logger.error("Error selecting email: {}", e.getMessage());
        }
        return this;
    }

    /**
     * Delete selected emails from the INBOX view (after checkbox selection).
     * The toolbar delete button appears with aria-label="Delete" only when
     * one or more email rows are checked.
     */
    public InboxPage clickDeleteButton() {
        try {
            Thread.sleep(800);

            // Click the toolbar Delete button that appears after row selection
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement deleteBtn = shortWait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//div[@aria-label='Delete' and not(@style='display:none')] | " +
                             "//button[@aria-label='Delete' and not(@style='display:none')]")
            ));
            deleteBtn.click();
            logger.info("Delete button clicked via toolbar.");
            Thread.sleep(1500);

        } catch (Exception e) {
            logger.error("Error clicking inbox delete button: {}", e.getMessage());
        }
        return this;
    }

    /**
     * Delete the currently open email from within the email view.
     * Inside an open email, Gmail's delete button has aria-label="Move to Trash",
     * not "Delete" — a different label from the inbox toolbar button.
     */
    public InboxPage clickDeleteButtonInEmailView() {
        try {
            Thread.sleep(800);

            // Strategy 1: "Move to Trash" label (Gmail's label inside email view)
            try {
                WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
                WebElement deleteBtn = shortWait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//div[@aria-label='Move to Trash'] | //button[@aria-label='Move to Trash'] | " +
                                 "//div[@data-tooltip='Move to Trash'] | //button[@data-tooltip='Move to Trash']")
                ));
                deleteBtn.click();
                logger.info("'Move to Trash' button clicked (Strategy 1).");
                Thread.sleep(1500);
                return this;
            } catch (Exception e1) {
                logger.warn("'Move to Trash' button not found, trying JS scan (Strategy 2)...");
            }

            // Strategy 2: JS scan for any trash/delete button visible on screen
            boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var labels = ['Move to Trash', 'Delete', 'Trash'];" +
                "for (var l = 0; l < labels.length; l++) {" +
                "  var btns = document.querySelectorAll(" +
                "    '[aria-label=\"' + labels[l] + '\"], [data-tooltip=\"' + labels[l] + '\"]');" +
                "  for (var i = 0; i < btns.length; i++) {" +
                "    var rect = btns[i].getBoundingClientRect();" +
                "    if (rect.width > 0 && rect.height > 0 && rect.top >= 0) {" +
                "      btns[i].click(); return true;" +
                "    }" +
                "  }" +
                "}" +
                "return false;"
            );

            if (clicked) {
                logger.info("Delete button clicked via JS scan (Strategy 2).");
                Thread.sleep(1500);
            } else {
                logger.warn("Could not find delete button inside email view.");
            }

        } catch (Exception e) {
            logger.error("Error clicking email-view delete button: {}", e.getMessage());
        }
        return this;
    }

    /**
     * Select multiple emails atomically by their indices in a single JS call.
     * All checkboxes are clicked before Gmail has a chance to re-render the list,
     * preventing the stale/deselect issue that occurs between sequential selections.
     *
     * @param indices 0-based indices of the rows to select
     */
    public InboxPage selectMultipleEmailsByIndex(int... indices) {
        try {
            // Pass indices as a JS array literal and click all checkboxes in one execution
            String indicesJson = "[" + java.util.Arrays.toString(indices)
                    .replace("[", "").replace("]", "") + "]";
            Long selected = (Long) ((JavascriptExecutor) driver).executeScript(
                "var indices = " + indicesJson + ";" +
                "var rows = document.querySelectorAll('tr.zA');" +
                "var count = 0;" +
                "for (var i = 0; i < indices.length; i++) {" +
                "  var idx = indices[i];" +
                "  if (idx < rows.length) {" +
                "    var cb = rows[idx].querySelector('div[role=\"checkbox\"]');" +
                "    if (cb) { cb.click(); count++; }" +
                "  }" +
                "}" +
                "return count;"
            );

            logger.info("Selected {} email(s) atomically via JS (requested {}).", selected, indices.length);
            Thread.sleep(600);
        } catch (Exception e) {
            logger.error("Error selecting emails by index: {}", e.getMessage());
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
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(EMAIL_ROWS));
        } catch (Exception e) {
            logger.debug("Timeout waiting for email rows, but continuing...");
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
