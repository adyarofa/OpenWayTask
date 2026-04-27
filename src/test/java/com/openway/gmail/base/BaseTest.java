package com.openway.gmail.base;

import com.openway.gmail.config.TestConfig;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.time.Duration;

/**
 * Base test class that handles WebDriver lifecycle management.
 * All test classes should extend this class to get a properly
 * configured Chrome browser instance.
 */
public abstract class BaseTest {

    protected static final Logger logger = LoggerFactory.getLogger(BaseTest.class);
    protected WebDriver driver;
    protected WebDriverWait wait;

    /**
     * Sets up Chrome WebDriver before all tests in the class.
     * Uses WebDriverManager for automatic driver binary management,
     * ensuring the test works on any computer without manual ChromeDriver setup.
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {
        logger.info("=== Setting up Chrome WebDriver ===");

        // Automatically download and configure the correct ChromeDriver
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        // Chrome options for stability
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");

        // Disable automation detection flags that Google might use
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.addArguments("--disable-blink-features=AutomationControlled");

        // Create a new Chrome window
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(TestConfig.DEFAULT_TIMEOUT_SECONDS));

        logger.info("Chrome browser opened successfully.");
    }

    /**
     * Tears down the WebDriver after all tests in the class are complete.
     */
    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            logger.info("=== Closing Chrome browser ===");
            driver.quit();
        }
    }

    /**
     * Get a WebDriverWait with a custom timeout.
     *
     * @param timeoutSeconds the timeout in seconds
     * @return a new WebDriverWait instance
     */
    protected WebDriverWait getWait(int timeoutSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    }
}
