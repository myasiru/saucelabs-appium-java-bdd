package com.saucelabs.pages;

import com.saucelabs.utilities.Driver;
import com.saucelabs.utilities.WaitUtils;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Abstract base class for all Screen Object Model (SOM) page classes.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Initialises Appium {@link PageFactory} with {@link AppiumFieldDecorator}.</li>
 *   <li>Exposes the thread-bound {@link AppiumDriver} to subclasses via {@link #driver()}.</li>
 *   <li>Provides reusable interaction methods (click, type, getText) that internally
 *       apply explicit waits before each action, eliminating boilerplate in page classes.</li>
 * </ul>
 */
public abstract class BasePage {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    /** PageFactory decorator timeout (seconds). Should be set to 0 – waits handled by WaitUtils. */
    private static final int DECORATOR_TIMEOUT_S = 0;

    protected BasePage() {
        PageFactory.initElements(
                new AppiumFieldDecorator(driver(), Duration.ofSeconds(DECORATOR_TIMEOUT_S)),
                this
        );
        log.debug("[{}] Page initialised.", getClass().getSimpleName());
    }

    // =========================================================================
    // Driver Access
    // =========================================================================

    /**
     * Returns the current thread's driver. Always use this method inside page
     * classes rather than calling {@code Driver.get()} directly, to keep pages
     * thread-safe in parallel execution.
     */
    protected AppiumDriver driver() {
        return Driver.get();
    }

    // =========================================================================
    // Guarded Interaction Helpers
    // =========================================================================

    /**
     * Waits for element to be clickable, then clicks it.
     *
     * @param element target element
     */
    protected void click(WebElement element) {
        log.debug("Clicking element: {}", element);
        WaitUtils.waitForClickability(element).click();
    }

    /**
     * Waits for element to be visible, clears any existing text, then types the value.
     *
     * @param element target element
     * @param text    text to enter
     */
    protected void type(WebElement element, String text) {
        log.debug("Typing '{}' into element: {}", text, element);
        WaitUtils.waitForVisibility(element).clear();
        element.sendKeys(text);
    }

    /**
     * Waits for element to be visible, then returns its trimmed text content.
     *
     * @param element target element
     * @return trimmed text
     */
    protected String getText(WebElement element) {
        String text = WaitUtils.waitForVisibility(element).getText();
        log.debug("getText -> '{}'", text);
        return text.trim();
    }

    /**
     * Checks whether an element is currently displayed without throwing on failure.
     *
     * @param element target element
     * @return {@code true} if element is displayed
     */
    protected boolean isDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (Exception e) {
            log.debug("isDisplayed check returned false for element: {}", element);
            return false;
        }
    }

    /**
     * Returns the page title / name for logging and debugging purposes.
     * Override in each page class.
     */
    public abstract String getPageTitle();
}
