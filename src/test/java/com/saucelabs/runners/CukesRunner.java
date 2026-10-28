package com.saucelabs.runners;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;

/**
 * Primary Cucumber test runner powered by the JUnit Platform Suite engine.
 *
 * <p>
 * Tag filtering is controlled by the Maven property {@code cucumber.tags}
 * (default: {@code @smoke}), so there is no need to hard-code tags here.
 * Override at runtime:
 * 
 * <pre>
 *   mvn test -Dcucumber.tags="@smoke"       # smoke only
 *   mvn test -Pregression                   # regression profile
 *   mvn test -Pall                          # everything
 * </pre>
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.saucelabs.step_definitions")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, html:target/cucumber-reports/cucumber-report.html, json:target/cucumber-reports/cucumber-report.json")
@ConfigurationParameter(key = "cucumber.publish.quiet", value = "true")
@ConfigurationParameter(key = "cucumber.snippet-type", value = "camelcase")
public class CukesRunner {
}
