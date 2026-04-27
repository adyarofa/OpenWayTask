package com.openway.gmail.pages;

import com.openway.gmail.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Page Object for the Gmail Login flow.
 * Handles Google's multi-step login process including:
 * - Email entry
 * - Password entry
 * - Detection of additional verification prompts (OTP, phone, etc.)
 */
public class LoginPage {

    private static final Logger logger = LoggerFactory.getLogger(LoginPage.class);

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Locators for the login flow
    private static final By EMAIL_INPUT = By.cssSelector("input[type='email']");
    private static final By EMAIL_NEXT_BUTTON = By.id("identifierNext");
    private static final By PASSWORD_INPUT = By.cssSelector("input[type='password']");
    private static final By PASSWORD_NEXT_BUTTON = By.id("passwordNext");

    // Possible additional verification locators
    private static final By VERIFICATION_CHALLENGE = By.cssSelector("#challengePickerList");
    private static final By OTP_INPUT = By.cssSelector("input[type='tel']");
    private static final By RECOVERY_EMAIL_OPTION = By.xpath("//div[contains(text(),'Confirm your recovery email')]");

    // Error message locators (wrong password, account not found, etc.)
    private static final By LOGIN_ERROR = By.cssSelector("div[class*='o6cuMc'], div[class*='dEOOab']");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(TestConfig.DEFAULT_TIMEOUT_SECONDS));
    }

    /**
     * Navigate to Gmail login page.
     */
    public LoginPage navigateToGmail() {
        logger.info("Navigating to Gmail: {}", TestConfig.GMAIL_URL);
        driver.get(TestConfig.GMAIL_URL);
        return this;
    }

    /**
     * Enter the email address and click Next.
     *
     * @param email the Gmail address
     */
    public LoginPage enterEmail(String email) {
        logger.info("Entering email address: {}", email);

        WebElement emailInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT)
        );
        emailInput.clear();
        emailInput.sendKeys(email);

        // Click the "Next" button
        WebElement nextButton = wait.until(
                ExpectedConditions.elementToBeClickable(EMAIL_NEXT_BUTTON)
        );
        nextButton.click();

        logger.info("Email entered and Next clicked.");
        return this;
    }

    /**
     * Enter the password and click Next.
     * Waits for the password field to become visible after the email step.
     *
     * @param password the account password
     */
    public LoginPage enterPassword(String password) {
        logger.info("Entering password...");

        // Wait for the password input to appear (Google animates between steps)
        WebElement passwordInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(PASSWORD_INPUT)
        );

        // Delay to ensure the field is interactive after Google's animation
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        passwordInput.clear();
        passwordInput.sendKeys(password);

        // Click the "Next" button
        WebElement nextButton = wait.until(
                ExpectedConditions.elementToBeClickable(PASSWORD_NEXT_BUTTON)
        );
        nextButton.click();

        logger.info("Password entered and Next clicked.");

        // Wait for the page to transition after submitting password
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return this;
    }

    /**
     * Check if Google is showing an additional verification challenge.
     * This handles the case where Google asks for OTP, phone verification, etc.
     * <p>
     * IMPORTANT: The normal password page URL contains "/challenge/pwd" which is NOT
     * an additional verification. We only flag true verification challenges like
     * OTP, phone, or recovery email prompts.
     *
     * @return true if additional verification is detected
     */
    public boolean isAdditionalVerificationRequired() {
        try {
            // Allow page to settle after password submission
            Thread.sleep(3000);

            String currentUrl = driver.getCurrentUrl();
            logger.info("Post-login URL: {}", currentUrl);

            // If we're already at the inbox, no verification needed
            if (currentUrl.contains("mail.google.com/mail/u/")
                    && !currentUrl.contains("accounts.google.com")) {
                logger.info("Already at inbox - no verification needed.");
                return false;
            }

            // Check for challenge picker or OTP prompt elements
            boolean challengePickerPresent = !driver.findElements(VERIFICATION_CHALLENGE).isEmpty();

            // Only flag OTP if we're NOT on the password page
            boolean onPasswordPage = currentUrl.contains("challenge/pwd");
            boolean otpPresent = !onPasswordPage && !driver.findElements(OTP_INPUT).isEmpty();

            // Check URL for REAL verification challenges (not the password page)
            boolean verificationUrl = (currentUrl.contains("/challenge/") && !onPasswordPage)
                    || currentUrl.contains("speedbump")
                    || currentUrl.contains("challenge/sk")     // Security key
                    || currentUrl.contains("challenge/dp")     // Device prompt
                    || currentUrl.contains("challenge/ipp");   // Phone verification

            if (challengePickerPresent || otpPresent || verificationUrl) {
                logger.warn("!!! ADDITIONAL VERIFICATION DETECTED !!!");
                logger.warn("Current URL: {}", currentUrl);
                logger.warn("Challenge picker: {}, OTP: {}, Verification URL: {}",
                        challengePickerPresent, otpPresent, verificationUrl);
                logger.warn("This may require manual intervention (OTP code, phone verification, etc.)");
                logger.warn("Consider using Google App Passwords or disabling 2-step verification for the test account.");
                return true;
            }

            // Check for login errors (wrong password, etc.)
            var errorElements = driver.findElements(LOGIN_ERROR);
            for (var el : errorElements) {
                String text = el.getText().trim();
                if (!text.isEmpty()) {
                    logger.error("Login error detected: '{}'", text);
                }
            }

        } catch (Exception e) {
            logger.debug("Exception during verification check: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Wait for Gmail inbox to load after successful login.
     * Checks multiple possible indicators that we've reached the inbox.
     *
     * @return true if inbox loaded successfully
     */
    public boolean waitForInboxToLoad() {
        logger.info("Waiting for Gmail inbox to load...");
        try {
            WebDriverWait longWait = new WebDriverWait(
                    driver, Duration.ofSeconds(TestConfig.LONG_TIMEOUT_SECONDS)
            );

            // Wait for the URL to indicate we're in Gmail (not on login pages)
            longWait.until(d -> {
                String url = d.getCurrentUrl();
                return (url.contains("mail.google.com/mail/u/") && !url.contains("accounts.google.com"))
                        || url.contains("#inbox")
                        || url.contains("#label")
                        || url.contains("#all");
            });

            // Additional wait for the page content to stabilize
            Thread.sleep(3000);

            logger.info("Gmail inbox loaded. Current URL: {}", driver.getCurrentUrl());
            return true;
        } catch (Exception e) {
            logger.error("Failed to load Gmail inbox: {}", e.getMessage());
            logger.error("Current URL: {}", driver.getCurrentUrl());
            return false;
        }
    }

    /**
     * Perform the complete login flow: navigate -> email -> password -> wait for inbox.
     *
     * @param email    Gmail address
     * @param password Gmail password
     * @return true if login was successful and inbox is loaded
     */
    public boolean login(String email, String password) {
        navigateToGmail();
        enterEmail(email);
        enterPassword(password);

        // Check for additional verification
        if (isAdditionalVerificationRequired()) {
            logger.error("Login requires additional verification. "
                    + "Please ensure the test account does not have 2FA enabled, "
                    + "or use an App Password.");
            return false;
        }

        return waitForInboxToLoad();
    }
}
