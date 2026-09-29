package com.saucelabs.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Thread-safe singleton configuration reader.
 * Loads properties once from {@code src/test/resources/configuration.properties}
 * and caches them for the lifetime of the JVM.
 */
public final class ConfigurationReader {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationReader.class);
    private static final String CONFIG_FILE = "configuration.properties";
    private static final Properties PROPERTIES = new Properties();

    static {
        loadProperties();
    }

    private ConfigurationReader() {
        // Utility class – no instantiation
    }

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    private static void loadProperties() {
        // First attempt: classpath (works when Maven copies resources to target/)
        try (InputStream cpStream = ConfigurationReader.class
                .getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (cpStream != null) {
                PROPERTIES.load(cpStream);
                LOG.info("Configuration loaded from classpath: {}", CONFIG_FILE);
                return;
            }
        } catch (IOException e) {
            LOG.warn("Could not load from classpath, falling back to file system. Reason: {}", e.getMessage());
        }

        // Second attempt: file-system (useful during IDE runs before compilation)
        String fsPath = "src/test/resources/" + CONFIG_FILE;
        try (FileInputStream fsStream = new FileInputStream(fsPath)) {
            PROPERTIES.load(fsStream);
            LOG.info("Configuration loaded from file system: {}", fsPath);
        } catch (IOException e) {
            throw new RuntimeException("FATAL: Cannot locate " + CONFIG_FILE +
                    ". Checked classpath and " + fsPath, e);
        }
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Returns the value associated with {@code key}.
     *
     * @param key property key
     * @return trimmed property value
     * @throws RuntimeException if key is absent
     */
    public static String get(String key) {
        String value = PROPERTIES.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new RuntimeException("Property '" + key + "' not found in " + CONFIG_FILE);
        }
        return value.trim();
    }

    /**
     * Returns the value associated with {@code key}, or {@code defaultValue} if absent.
     */
    public static String getOrDefault(String key, String defaultValue) {
        String value = PROPERTIES.getProperty(key);
        return (value == null || value.isBlank()) ? defaultValue : value.trim();
    }

    /**
     * Returns the int value of the given property key.
     */
    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    /**
     * Returns the int value or {@code defaultValue} if absent / unparsable.
     */
    public static int getIntOrDefault(String key, int defaultValue) {
        try {
            return Integer.parseInt(getOrDefault(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            LOG.warn("Property '{}' is not a valid integer. Using default: {}", key, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Returns the boolean value of the given property key.
     */
    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }
}
