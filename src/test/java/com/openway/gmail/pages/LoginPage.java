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

public class LoginPage {

    private static final Logger logger = LoggerFactory.getLogger(LoginPage.class);

    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final By EMAIL_INPUT = By.cssSelector("input[type='email']");
    private static final By EMAIL_NEXT_BUTTON = By.id("identifierNext");
    private static final By PASSWORD_INPUT = By.cssSelector("input[type='password']");
    private static final By PASSWORD_NEXT_BUTTON = By.id("passwordNext");

    private static final By VERIFICATION_CHALLENGE = By.cssSelector("#challengePickerList");
    private static final By OTP_INPUT = By.cssSelector("input[type='tel']");
    private static final By RECOVERY_EMAIL_OPTION = By.xpath("//div[contains(text(),'Confirm your recovery email')]");

    private static final By LOGIN_ERROR = By.cssSelector("div[class*='o6cuMc'], div[class*='dEOOab']");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(TestConfig.DEFAULT_TIMEOUT_SECONDS));
    }

    public LoginPage navigateToGmail() {
        logger.info("Navigating to Gmail: {}", TestConfig.GMAIL_URL);
        driver.get(TestConfig.GMAIL_URL);
        return this;
    }

    public LoginPage enterEmail(String email) {
        logger.info("Entering email address: {}", email);
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT));
        emailInput.clear();
        emailInput.sendKeys(email);
        WebElement nextButton = wait.until(ExpectedConditions.elementToBeClickable(EMAIL_NEXT_BUTTON));
        nextButton.click();
        logger.info("Email entered and Next clicked.");
        return this;
    }

    public LoginPage enterPassword(String password) {
        logger.info("Entering password...");
        WebElement passwordInput = wait.until(ExpectedConditions.visibilityOfElementLocated(PASSWORD_INPUT));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        passwordInput.clear();
        passwordInput.sendKeys(password);
        WebElement nextButton = wait.until(ExpectedConditions.elementToBeClickable(PASSWORD_NEXT_BUTTON));
        nextButton.click();
        logger.info("Password entered and Next clicked.");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return this;
    }

    public boolean isAdditionalVerificationRequired() {
        try {
            Thread.sleep(3000);
            String currentUrl = driver.getCurrentUrl();
            logger.info("Post-login URL: {}", currentUrl);

            if (currentUrl.contains("mail.google.com/mail/u/") && !currentUrl.contains("accounts.google.com")) {
                logger.info("Already at inbox - no verification needed.");
                return false;
            }

            boolean challengePickerPresent = !driver.findElements(VERIFICATION_CHALLENGE).isEmpty();
            boolean onPasswordPage = currentUrl.contains("challenge/pwd");
            boolean otpPresent = !onPasswordPage && !driver.findElements(OTP_INPUT).isEmpty();
            boolean verificationUrl = (currentUrl.contains("/challenge/") && !onPasswordPage)
                    || currentUrl.contains("speedbump")
                    || currentUrl.contains("challenge/sk")
                    || currentUrl.contains("challenge/dp")
                    || currentUrl.contains("challenge/ipp");

            if (challengePickerPresent || otpPresent || verificationUrl) {
                logger.warn("!!! ADDITIONAL VERIFICATION DETECTED !!!");
                logger.warn("Current URL: {}", currentUrl);
                logger.warn("Challenge picker: {}, OTP: {}, Verification URL: {}",
                        challengePickerPresent, otpPresent, verificationUrl);
                return true;
            }

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

    public boolean waitForInboxToLoad() {
        logger.info("Waiting for Gmail inbox to load...");
        try {
            WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(TestConfig.LONG_TIMEOUT_SECONDS));
            longWait.until(d -> {
                String url = d.getCurrentUrl();
                return (url.contains("mail.google.com/mail/u/") && !url.contains("accounts.google.com"))
                        || url.contains("#inbox")
                        || url.contains("#label")
                        || url.contains("#all");
            });
            Thread.sleep(3000);
            logger.info("Gmail inbox loaded. Current URL: {}", driver.getCurrentUrl());
            return true;
        } catch (Exception e) {
            logger.error("Failed to load Gmail inbox: {}", e.getMessage());
            logger.error("Current URL: {}", driver.getCurrentUrl());
            return false;
        }
    }

    public boolean login(String email, String password) {
        navigateToGmail();
        enterEmail(email);
        enterPassword(password);
        if (isAdditionalVerificationRequired()) {
            logger.error("Login requires additional verification. Use an App Password or disable 2FA.");
            return false;
        }
        return waitForInboxToLoad();
    }
}
