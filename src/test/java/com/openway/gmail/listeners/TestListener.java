package com.openway.gmail.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {

    private static final Logger logger = LoggerFactory.getLogger(TestListener.class);

    @Override
    public void onStart(ITestContext context) {
        logger.info("╔══════════════════════════════════════════════════════╗");
        logger.info("║  TEST SUITE STARTED: {}",
                padRight(context.getName(), 30) + "║");
        logger.info("╚══════════════════════════════════════════════════════╝");
    }

    @Override
    public void onFinish(ITestContext context) {
        logger.info("╔══════════════════════════════════════════════════════╗");
        logger.info("║  TEST SUITE FINISHED: {}",
                padRight(context.getName(), 30) + "║");
        logger.info("║  Passed:  {}", padRight(String.valueOf(
                context.getPassedTests().size()), 40) + "║");
        logger.info("║  Failed:  {}", padRight(String.valueOf(
                context.getFailedTests().size()), 40) + "║");
        logger.info("║  Skipped: {}", padRight(String.valueOf(
                context.getSkippedTests().size()), 40) + "║");
        logger.info("╚══════════════════════════════════════════════════════╝");
    }

    @Override
    public void onTestStart(ITestResult result) {
        logger.info("──────────────────────────────────────────────────────");
        logger.info("▶ STARTING TEST: {}", result.getMethod().getMethodName());
        logger.info("  Description: {}", result.getMethod().getDescription());
        logger.info("──────────────────────────────────────────────────────");
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        logger.info("✅ TEST PASSED: {} ({}ms)",
                result.getMethod().getMethodName(),
                result.getEndMillis() - result.getStartMillis());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        logger.error("❌ TEST FAILED: {}", result.getMethod().getMethodName());
        logger.error("   Reason: {}", result.getThrowable().getMessage());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        logger.warn("⏭ TEST SKIPPED: {}", result.getMethod().getMethodName());
        if (result.getThrowable() != null) {
            logger.warn("   Reason: {}", result.getThrowable().getMessage());
        }
    }

    private String padRight(String text, int length) {
        if (text.length() >= length) {
            return text.substring(0, length);
        }
        return text + " ".repeat(length - text.length());
    }
}
