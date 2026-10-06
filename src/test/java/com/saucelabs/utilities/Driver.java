package com.saucelabs.utilities;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

/**
 * Thread-safe Singleton Driver Factory.
 *
 * <p>
 * Uses {@link ThreadLocal} to maintain one {@link AppiumDriver} per thread,
 * enabling safe parallel test execution. Supports Android (UiAutomator2) and
 * iOS (XCUITest) natively.
 */
public final class Driver {

    private static final Logger LOG = LoggerFactory.getLogger(Driver.class);

    /** One driver instance per thread. */
    private static final ThreadLocal<AppiumDriver> DRIVER_THREAD_LOCAL = new ThreadLocal<>();

    private Driver() {
        // Utility class – no instantiation
    }

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Returns the current thread's {@link AppiumDriver}.
     * Lazily initialises the driver if it has not been created yet.
     *
     * @return the thread-bound driver instance
     */
    public static AppiumDriver get() {
        if (DRIVER_THREAD_LOCAL.get() == null) {
            LOG.info("[Thread-{}] No driver found. Initialising new driver...", Thread.currentThread().getId());
            initialise();
        }
        return DRIVER_THREAD_LOCAL.get();
    }

    /**
     * Quits the driver and removes it from the thread-local store.
     * Should be called in {@code @After} hooks to prevent resource leaks.
     */
    public static void quit() {
        AppiumDriver driver = DRIVER_THREAD_LOCAL.get();
        if (driver != null) {
            try {
                driver.quit();
                LOG.info("[Thread-{}] Driver session closed successfully.", Thread.currentThread().getId());
            } catch (Exception e) {
                LOG.error("[Thread-{}] Error while quitting driver: {}", Thread.currentThread().getId(),
                        e.getMessage());
            } finally {
                DRIVER_THREAD_LOCAL.remove();
            }
        }
    }

    /**
     * Returns {@code true} if a driver is currently active on this thread.
     */
    public static boolean isActive() {
        return DRIVER_THREAD_LOCAL.get() != null;
    }

    // =========================================================================
    // Initialisation Logic
    // =========================================================================

    private static void initialise() {
        String platform = ConfigurationReader.getOrDefault("platform", "android").toLowerCase();
        URL serverUrl = buildServerUrl();

        AppiumDriver driver = switch (platform) {
            case "android" -> createAndroidDriver(serverUrl);
            case "ios" -> createIosDriver(serverUrl);
            default -> throw new IllegalArgumentException("Unsupported platform: '" + platform +
                    "'. Supported values: android, ios");
        };

        DRIVER_THREAD_LOCAL.set(driver);
        LOG.info("[Thread-{}] {} driver initialised. Session ID: {}",
                Thread.currentThread().getId(), platform.toUpperCase(), driver.getSessionId());
    }

    // ─── Android ─────────────────────────────────────────────────────────────

    private static AndroidDriver createAndroidDriver(URL serverUrl) {
        String appPath = resolveAppPath(ConfigurationReader.get("android.app"));

        UiAutomator2Options options = new UiAutomator2Options()
                .setDeviceName(ConfigurationReader.get("android.deviceName"))
                .setPlatformVersion(ConfigurationReader.get("android.platformVersion"))
                .setApp(appPath)
                .setAppPackage(ConfigurationReader.get("android.appPackage"))
                .setAppActivity(ConfigurationReader.get("android.appActivity"))
                .setAutomationName(ConfigurationReader.get("android.automationName"))
                .setNoReset(Boolean.parseBoolean(ConfigurationReader.getOrDefault("android.noReset", "false")))
                .setFullReset(Boolean.parseBoolean(ConfigurationReader.getOrDefault("android.fullReset", "false")))
                .setNewCommandTimeout(Duration.ofSeconds(
                        ConfigurationReader.getIntOrDefault("android.newCommandTimeout", 60)))
                .setAutoGrantPermissions(Boolean.parseBoolean(
                        ConfigurationReader.getOrDefault("android.autoGrantPermissions", "true")));

        // ── Pixel 7 / Android 14 specific capabilities ────────────────────────
        String avdName = ConfigurationReader.getOrDefault("android.avdName", "");
        if (!avdName.isBlank()) {
            options.setAvd(avdName);
            LOG.info("AVD name set to '{}'. Appium will auto-launch if emulator is not running.", avdName);
        }

        boolean chromedriverAutodownload = Boolean.parseBoolean(
                ConfigurationReader.getOrDefault("android.chromedriverAutodownload", "false"));
        if (chromedriverAutodownload) {
            options.setCapability("appium:chromedriverAutodownload", true);
            LOG.info("chromedriverAutodownload enabled for Android 14 / Pixel 7.");
        }

        LOG.debug("Creating AndroidDriver with options: {}", options);
        return new AndroidDriver(serverUrl, options);
    }

    // ─── iOS ─────────────────────────────────────────────────────────────────

    private static IOSDriver createIosDriver(URL serverUrl) {
        String appPath = resolveAppPath(ConfigurationReader.get("ios.app"));

        XCUITestOptions options = new XCUITestOptions()
                .setDeviceName(ConfigurationReader.get("ios.deviceName"))
                .setPlatformVersion(ConfigurationReader.get("ios.platformVersion"))
                .setApp(appPath)
                .setBundleId(ConfigurationReader.get("ios.bundleId"))
                .setAutomationName(ConfigurationReader.get("ios.automationName"));

        LOG.debug("Creating IOSDriver with options: {}", options);
        return new IOSDriver(serverUrl, options);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private static URL buildServerUrl() {
        String rawUrl = ConfigurationReader.getOrDefault("appium.server.url", "http://127.0.0.1:4723");
        try {
            return new URL(rawUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Appium server URL: " + rawUrl, e);
        }
    }

    /**
     * Resolves the app path relative to the project root if not absolute.
     */
    private static String resolveAppPath(String configuredPath) {
        File file = new File(configuredPath);
        if (file.isAbsolute() && file.exists()) {
            return file.getAbsolutePath();
        }

        File relative = new File(System.getProperty("user.dir"), configuredPath);
        if (!relative.exists()) {
            throw new RuntimeException("APK/IPA not found at: " + relative.getAbsolutePath() +
                    ". Ensure the app file is present at the configured path.");
        }
        LOG.info("Resolved app path: {}", relative.getAbsolutePath());
        return relative.getAbsolutePath();
    }
}