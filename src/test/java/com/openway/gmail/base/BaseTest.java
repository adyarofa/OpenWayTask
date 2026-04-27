package com.openway.gmail.base;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.openway.gmail.config.TestConfig;

import io.github.bonigarcia.wdm.WebDriverManager;
public abstract class BaseTest {

    protected static final Logger logger = LoggerFactory.getLogger(BaseTest.class);
    protected WebDriver driver;
    protected WebDriverWait wait;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        logger.info("=== Setting up Chrome WebDriver ===");

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.addArguments("--disable-blink-features=AutomationControlled");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(TestConfig.DEFAULT_TIMEOUT_SECONDS));

        logger.info("Chrome browser opened successfully.");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            logger.info("=== Closing Chrome browser ===");
            driver.quit();
        }
    }

    protected WebDriverWait getWait(int timeoutSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    }
}
