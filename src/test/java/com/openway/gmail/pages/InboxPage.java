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

public class InboxPage {

    private static final Logger logger = LoggerFactory.getLogger(InboxPage.class);

    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final By EMAIL_ROWS = By.cssSelector("tr.zA");
    private static final By EMAIL_ROWS_FALLBACK = By.cssSelector("div[role='main'] tr[jsmodel]");
    private static final By UNREAD_EMAIL_ROWS = By.cssSelector("tr.zA.zE");
    private static final By EMAIL_CHECKBOX = By.cssSelector("td.oZ-jc div[role='checkbox'], td.PF div[role='checkbox']");

    public InboxPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(TestConfig.DEFAULT_TIMEOUT_SECONDS));
    }

    public InboxPage navigateToInbox() {
        logger.info("Navigating to Inbox...");
        driver.get("https://mail.google.com/mail/u/0/#inbox");
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

    public List<WebElement> getAllEmailRows() {
        try {
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

    public List<WebElement> getUnreadEmailRows() {
        try {
            WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(TestConfig.LONG_TIMEOUT_SECONDS));
            longWait.until(d -> !d.findElements(UNREAD_EMAIL_ROWS).isEmpty());
            List<WebElement> unreadRows = driver.findElements(UNREAD_EMAIL_ROWS);
            logger.info("Found {} unread email(s) in inbox.", unreadRows.size());
            return unreadRows;
        } catch (Exception e) {
            logger.warn("No unread emails detected or timeout: {}. Returning empty list.", e.getMessage());
            return List.of();
        }
    }

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
                    "if (allText.length > 0) { return allText.substring(0, 200); }" +
                    "return '';",
                    emailRow
            );
            if (subject != null && !subject.isEmpty()) {
                logger.debug("Email subject extracted: {}", subject);
                return subject;
            }
            logger.warn("Could not extract subject text.");
            return "(Unable to read subject)";
        } catch (Exception e) {
            logger.error("Error reading email subject: {}", e.getMessage());
            return "(Error reading subject)";
        }
    }

    public String getLastUnreadEmailSubject() {
        List<WebElement> unreadEmails = getUnreadEmailRows();
        if (unreadEmails.isEmpty()) {
            logger.warn("No unread emails found in the inbox.");
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
        String subject = getEmailSubject(unreadEmails.get(0));
        logger.info("========================================");
        logger.info("LAST UNREAD EMAIL SUBJECT: {}", subject);
        logger.info("========================================");
        return subject;
    }

    public InboxPage selectEmail(WebElement emailRow) {
        try {
            new org.openqa.selenium.interactions.Actions(driver)
                    .moveToElement(emailRow)
                    .perform();
            Thread.sleep(400);

            WebElement checkbox;
            try {
                checkbox = emailRow.findElement(EMAIL_CHECKBOX);
            } catch (Exception ex) {
                checkbox = emailRow.findElement(By.cssSelector("div[role='checkbox']"));
            }

            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkbox);
            Thread.sleep(500);

            String checkedAfter = checkbox.getAttribute("aria-checked");
            logger.info("Checkbox state after click: {}", checkedAfter);

            if (!"true".equals(checkedAfter)) {
                logger.warn("Checkbox not marked checked — retrying normal click...");
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

    public InboxPage clickDeleteButton() {
        try {
            Thread.sleep(800);
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

    public InboxPage clickDeleteButtonInEmailView() {
        try {
            Thread.sleep(800);
            try {
                WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
                WebElement deleteBtn = shortWait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//div[@aria-label='Move to Trash'] | //button[@aria-label='Move to Trash'] | " +
                                 "//div[@data-tooltip='Move to Trash'] | //button[@data-tooltip='Move to Trash']")
                ));
                deleteBtn.click();
                logger.info("'Move to Trash' button clicked.");
                Thread.sleep(1500);
                return this;
            } catch (Exception e1) {
                logger.warn("'Move to Trash' button not found, trying JS scan...");
            }

            boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var labels = ['Move to Trash', 'Delete', 'Trash'];" +
                "for (var l = 0; l < labels.length; l++) {" +
                "  var btns = document.querySelectorAll('[aria-label=\"' + labels[l] + '\"], [data-tooltip=\"' + labels[l] + '\"]');" +
                "  for (var i = 0; i < btns.length; i++) {" +
                "    var rect = btns[i].getBoundingClientRect();" +
                "    if (rect.width > 0 && rect.height > 0 && rect.top >= 0) { btns[i].click(); return true; }" +
                "  }" +
                "}" +
                "return false;"
            );

            if (clicked) {
                logger.info("Delete button clicked via JS scan.");
                Thread.sleep(1500);
            } else {
                logger.warn("Could not find delete button inside email view.");
            }
        } catch (Exception e) {
            logger.error("Error clicking email-view delete button: {}", e.getMessage());
        }
        return this;
    }

    public InboxPage selectMultipleEmailsByIndex(int... indices) {
        try {
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

    public InboxPage deleteEmail(WebElement emailRow) {
        String subject = getEmailSubject(emailRow);
        logger.info("Deleting email: '{}'", subject);
        selectEmail(emailRow);
        clickDeleteButton();
        return this;
    }

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
