package com.saucelabs.utilities;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

/**
 * Centralised explicit and fluent wait helpers for the framework.
 *
 * <p>All timeouts default to values declared in {@code configuration.properties}.
 * Every public method is thread-safe because it retrieves the driver via
 * {@link Driver#get()} (ThreadLocal-backed).
 */
public final class WaitUtils {

    private static final Logger LOG = LoggerFactory.getLogger(WaitUtils.class);

    private static final long EXPLICIT_TIMEOUT_S = ConfigurationReader.getIntOrDefault("explicit.wait.timeout", 20);
    private static final long FLUENT_TIMEOUT_S   = ConfigurationReader.getIntOrDefault("fluent.wait.timeout", 15);
    private static final long FLUENT_POLLING_MS  = ConfigurationReader.getIntOrDefault("fluent.wait.polling", 500);

    private WaitUtils() {}

    // =========================================================================
    // Explicit Waits (WebDriverWait)
    // =========================================================================

    /**
     * Waits until the element is visible on screen.
     *
     * @param element target element
     * @return the same element once visible
     */
    public static WebElement waitForVisibility(WebElement element) {
        LOG.debug("Waiting for element visibility: {}", element);
        return getWait().until(ExpectedConditions.visibilityOf(element));
    }

    /**
     * Waits until the element is clickable (visible + enabled).
     *
     * @param element target element
     * @return the same element once clickable
     */
    public static WebElement waitForClickability(WebElement element) {
        LOG.debug("Waiting for element clickability: {}", element);
        return getWait().until(ExpectedConditions.elementToBeClickable(element));
    }

    /**
     * Waits until a list contains at least one visible element.
     *
     * @param elements list of elements
     * @return the list once visible
     */
    public static List<WebElement> waitForVisibilityOfAll(List<WebElement> elements) {
        LOG.debug("Waiting for visibility of element list (size={})", elements.size());
        return getWait().until(ExpectedConditions.visibilityOfAllElements(elements));
    }

    /**
     * Waits until an element's text contains the expected substring.
     *
     * @param element      target element
     * @param partialText  text substring to find
     */
    public static void waitForTextToContain(WebElement element, String partialText) {
        LOG.debug("Waiting for element to contain text: '{}'", partialText);
        getWait().until(ExpectedConditions.textToBePresentInElement(element, partialText));
    }

    /**
     * Waits until an element is invisible / removed from DOM.
     *
     * @param element target element
     */
    public static void waitForInvisibility(WebElement element) {
        LOG.debug("Waiting for element invisibility: {}", element);
        getWait().until(ExpectedConditions.invisibilityOf(element));
    }

    // =========================================================================
    // Fluent Waits (polling with ignored exceptions)
    // =========================================================================

    /**
     * Fluent wait for element visibility – tolerates {@code NoSuchElementException}
     * and {@code StaleElementReferenceException} during polling.
     *
     * @param element target element
     * @return the element once visible
     */
    public static WebElement fluentWaitForVisibility(WebElement element) {
        LOG.debug("FluentWait for element visibility: {}", element);
        return getFluentWait()
                .until(ExpectedConditions.visibilityOf(element));
    }

    /**
     * Fluent wait for element clickability.
     *
     * @param element target element
     * @return the element once clickable
     */
    public static WebElement fluentWaitForClickability(WebElement element) {
        LOG.debug("FluentWait for element clickability: {}", element);
        return getFluentWait()
                .until(ExpectedConditions.elementToBeClickable(element));
    }

    // =========================================================================
    // Convenience pause (use sparingly)
    // =========================================================================

    /**
     * Hard pause – use only when dynamic waits are impractical.
     * Prefer explicit/fluent waits wherever possible.
     *
     * @param millis milliseconds to sleep
     */
    public static void pause(long millis) {
        LOG.warn("Hard pause of {}ms invoked. Consider replacing with an explicit wait.", millis);
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // =========================================================================
    // Private Factories
    // =========================================================================

    private static WebDriverWait getWait() {
        return new WebDriverWait(Driver.get(), Duration.ofSeconds(EXPLICIT_TIMEOUT_S));
    }

    private static FluentWait<AppiumDriver> getFluentWait() {
        return new FluentWait<>(Driver.get())
                .withTimeout(Duration.ofSeconds(FLUENT_TIMEOUT_S))
                .pollingEvery(Duration.ofMillis(FLUENT_POLLING_MS))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);
    }
}
