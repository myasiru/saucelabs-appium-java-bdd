package com.saucelabs.step_definitions;

import com.saucelabs.utilities.ConfigurationReader;
import com.saucelabs.utilities.Driver;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cucumber lifecycle hooks for session management and failure reporting.
 *
 * <p>{@link Before} – Initialises the Appium driver for each scenario
 * (the driver is lazily created on first access via {@link Driver#get()}).
 *
 * <p>{@link After} – Captures a screenshot on failure (attached to the
 * Cucumber HTML/JSON report), then quits the driver session cleanly.
 */
public class Hooks {

    private static final Logger LOG = LoggerFactory.getLogger(Hooks.class);
    private static final boolean SCREENSHOT_ON_FAILURE =
            ConfigurationReader.getBoolean("screenshot.on.failure");

    // =========================================================================
    // @Before
    // =========================================================================

    /**
     * Runs before every scenario.
     * Eagerly initialises the driver so that session start-up time is counted
     * as part of the scenario execution and visible in reports.
     *
     * @param scenario the Cucumber {@link Scenario} about to start
     */
    @Before(order = 0)
    public void beforeScenario(Scenario scenario) {
        LOG.info("╔══════════════════════════════════════════════════════════╗");
        LOG.info("║  SCENARIO START: {}", scenario.getName());
        LOG.info("║  Tags: {}", scenario.getSourceTagNames());
        LOG.info("╚══════════════════════════════════════════════════════════╝");

        // Force driver initialisation before first step
        Driver.get();
    }

    // =========================================================================
    // @After
    // =========================================================================

    /**
     * Runs after every scenario.
     *
     * <ol>
     *   <li>If the scenario failed, takes a screenshot and attaches it to the report.</li>
     *   <li>Quits the driver, ensuring no orphan Appium sessions remain.</li>
     * </ol>
     *
     * @param scenario the completed {@link Scenario}
     */
    @After(order = 0)
    public void afterScenario(Scenario scenario) {
        LOG.info("Scenario '{}' finished with status: {}", scenario.getName(), scenario.getStatus());

        if (scenario.isFailed() && SCREENSHOT_ON_FAILURE) {
            captureAndAttachScreenshot(scenario);
        }

        Driver.quit();
        LOG.info("────────────────────────────────────────────────────────────\n");
    }

    // =========================================================================
    // Private Helpers
    // =========================================================================

    /**
     * Takes a full-page screenshot and attaches the PNG bytes to the Cucumber report.
     * The attachment is visible in Cucumber HTML, JSON, and Allure reports.
     *
     * @param scenario the failed scenario
     */
    private void captureAndAttachScreenshot(Scenario scenario) {
        try {
            if (!Driver.isActive()) {
                LOG.warn("Driver is not active; cannot capture screenshot for '{}'.", scenario.getName());
                return;
            }
            byte[] screenshot = ((TakesScreenshot) Driver.get()).getScreenshotAs(OutputType.BYTES);
            String attachName = "FAILURE_" + sanitize(scenario.getName()) + ".png";
            scenario.attach(screenshot, "image/png", attachName);
            LOG.info("Screenshot captured and attached: {}", attachName);
        } catch (Exception e) {
            LOG.error("Failed to capture screenshot for scenario '{}': {}", scenario.getName(), e.getMessage());
        }
    }

    /**
     * Sanitises a scenario name so it is safe to use as a file-attachment name.
     */
    private String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9_\\-]", "_").substring(0, Math.min(name.length(), 60));
    }
}
