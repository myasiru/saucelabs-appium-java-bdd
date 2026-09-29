package com.saucelabs.utilities;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

/**
 * W3C Actions API gesture helpers for mobile interactions.
 *
 * <p>All gestures are implemented using the W3C Pointer/Pause input model,
 * which is the only supported approach in Appium 2.x. Legacy
 * {@code TouchAction} / {@code MultiTouchAction} APIs are NOT used.
 *
 * <p>Coordinate calculations are always relative to the current screen
 * {@link Dimension}, so gestures remain stable across different device
 * resolutions.
 */
public final class GestureUtils {

    private static final Logger LOG = LoggerFactory.getLogger(GestureUtils.class);

    /** Duration for a standard swipe gesture. */
    private static final Duration SWIPE_DURATION = Duration.ofMillis(600);

    /** Duration for a long-press / drag gesture. */
    private static final Duration LONG_PRESS_DURATION = Duration.ofMillis(1500);

    private GestureUtils() {}

    // =========================================================================
    // Scroll Gestures
    // =========================================================================

    /**
     * Scrolls the screen downward by the specified fraction.
     *
     * @param scrollFraction fraction of screen height to scroll (0.0 – 1.0)
     */
    public static void scrollDown(double scrollFraction) {
        Dimension size = Driver.get().manage().window().getSize();
        int startX    = size.width  / 2;
        int startY    = (int) (size.height * 0.75);
        int endY      = (int) (size.height * (0.75 - scrollFraction));

        LOG.debug("Scrolling down: startY={}, endY={}", startY, endY);
        swipe(startX, startY, startX, endY);
    }

    /**
     * Scrolls the screen upward by the specified fraction.
     *
     * @param scrollFraction fraction of screen height to scroll (0.0 – 1.0)
     */
    public static void scrollUp(double scrollFraction) {
        Dimension size = Driver.get().manage().window().getSize();
        int startX    = size.width  / 2;
        int startY    = (int) (size.height * 0.25);
        int endY      = (int) (size.height * (0.25 + scrollFraction));

        LOG.debug("Scrolling up: startY={}, endY={}", startY, endY);
        swipe(startX, startY, startX, endY);
    }

    /**
     * Scrolls down repeatedly until an element becomes visible, or the max
     * number of swipes is reached.
     *
     * @param element   target element to scroll into view
     * @param maxSwipes maximum number of downward swipes to attempt
     */
    public static void scrollDownToElement(WebElement element, int maxSwipes) {
        LOG.info("Scrolling down to element. Max swipes: {}", maxSwipes);
        for (int i = 0; i < maxSwipes; i++) {
            try {
                if (element.isDisplayed()) {
                    LOG.info("Element found after {} swipe(s).", i);
                    return;
                }
            } catch (Exception e) {
                // Element not yet in DOM – keep scrolling
            }
            scrollDown(0.35);
        }
        LOG.warn("Element may not be visible after {} swipes.", maxSwipes);
    }

    // =========================================================================
    // Swipe Gestures
    // =========================================================================

    /**
     * Swipes horizontally to the left (reveals next page / carousel item).
     *
     * @param swipeFraction fraction of screen width to swipe (0.0 – 1.0)
     */
    public static void swipeLeft(double swipeFraction) {
        Dimension size = Driver.get().manage().window().getSize();
        int startX = (int) (size.width * 0.85);
        int endX   = (int) (size.width * (0.85 - swipeFraction));
        int midY   = size.height / 2;

        LOG.debug("Swiping left: startX={}, endX={}", startX, endX);
        swipe(startX, midY, endX, midY);
    }

    /**
     * Swipes horizontally to the right (reveals previous page / carousel item).
     *
     * @param swipeFraction fraction of screen width to swipe (0.0 – 1.0)
     */
    public static void swipeRight(double swipeFraction) {
        Dimension size = Driver.get().manage().window().getSize();
        int startX = (int) (size.width * 0.15);
        int endX   = (int) (size.width * (0.15 + swipeFraction));
        int midY   = size.height / 2;

        LOG.debug("Swiping right: startX={}, endX={}", startX, endX);
        swipe(startX, midY, endX, midY);
    }

    /**
     * Swipes on a specific element in the given direction.
     *
     * @param element   element to swipe on
     * @param direction "LEFT" | "RIGHT" | "UP" | "DOWN"
     */
    public static void swipeOnElement(WebElement element, String direction) {
        Point  loc  = element.getLocation();
        Dimension sz = element.getSize();
        int cx = loc.x + sz.width  / 2;
        int cy = loc.y + sz.height / 2;

        int startX, startY, endX, endY;
        int offset = 80;
        switch (direction.toUpperCase()) {
            case "LEFT"  -> { startX = cx + offset; startY = cy; endX = cx - offset; endY = cy; }
            case "RIGHT" -> { startX = cx - offset; startY = cy; endX = cx + offset; endY = cy; }
            case "UP"    -> { startX = cx; startY = cy + offset; endX = cx; endY = cy - offset; }
            case "DOWN"  -> { startX = cx; startY = cy - offset; endX = cx; endY = cy + offset; }
            default      -> throw new IllegalArgumentException("Unknown direction: " + direction);
        }
        LOG.debug("Swiping {} on element: center=({},{})", direction, cx, cy);
        swipe(startX, startY, endX, endY);
    }

    // =========================================================================
    // Tap Gestures
    // =========================================================================

    /**
     * Performs a W3C single tap on the given element.
     *
     * @param element element to tap
     */
    public static void tap(WebElement element) {
        Point  loc = element.getLocation();
        Dimension sz = element.getSize();
        int x = loc.x + sz.width  / 2;
        int y = loc.y + sz.height / 2;
        LOG.debug("Tapping element at ({},{})", x, y);
        tapAtCoordinate(x, y);
    }

    /**
     * Performs a W3C single tap at the given screen coordinates.
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    public static void tapAtCoordinate(int x, int y) {
        PointerInput finger    = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence      sequence = new Sequence(finger, 0);

        sequence.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y));
        sequence.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        sequence.addAction(new Pause(finger, Duration.ofMillis(80)));
        sequence.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        Driver.get().perform(List.of(sequence));
    }

    // =========================================================================
    // Drag & Drop
    // =========================================================================

    /**
     * Drags an element from its current position to the target element.
     *
     * @param source element to drag
     * @param target drop target element
     */
    public static void dragAndDrop(WebElement source, WebElement target) {
        Point srcCenter = getCenterPoint(source);
        Point tgtCenter = getCenterPoint(target);

        LOG.debug("Drag from ({},{}) to ({},{})",
                srcCenter.x, srcCenter.y, tgtCenter.x, tgtCenter.y);

        PointerInput finger    = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence      sequence = new Sequence(finger, 0);

        sequence.addAction(finger.createPointerMove(Duration.ZERO,
                PointerInput.Origin.viewport(), srcCenter.x, srcCenter.y));
        sequence.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        sequence.addAction(new Pause(finger, LONG_PRESS_DURATION));
        sequence.addAction(finger.createPointerMove(Duration.ofMillis(800),
                PointerInput.Origin.viewport(), tgtCenter.x, tgtCenter.y));
        sequence.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        Driver.get().perform(List.of(sequence));
    }

    // =========================================================================
    // Core W3C Swipe Implementation
    // =========================================================================

    /**
     * Executes a low-level W3C swipe action from (startX, startY) to (endX, endY).
     */
    private static void swipe(int startX, int startY, int endX, int endY) {
        AppiumDriver driver = Driver.get();

        PointerInput finger    = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence      sequence = new Sequence(finger, 0);

        sequence.addAction(finger.createPointerMove(Duration.ZERO,
                PointerInput.Origin.viewport(), startX, startY));
        sequence.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        sequence.addAction(new Pause(finger, Duration.ofMillis(100)));
        sequence.addAction(finger.createPointerMove(SWIPE_DURATION,
                PointerInput.Origin.viewport(), endX, endY));
        sequence.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(List.of(sequence));
    }

    private static Point getCenterPoint(WebElement element) {
        Point     loc = element.getLocation();
        Dimension sz  = element.getSize();
        return new Point(loc.x + sz.width / 2, loc.y + sz.height / 2);
    }
}
