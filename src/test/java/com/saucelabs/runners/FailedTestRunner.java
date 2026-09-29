package com.saucelabs.runners;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;

/**
 * Re-run runner for failed scenarios only.
 *
 * <p>Works in tandem with the {@code rerun} Cucumber plugin (added in
 * {@link CukesRunner}). After a test run:
 * <ol>
 *   <li>A {@code @rerun} file is generated at {@code target/rerun.txt} listing
 *       each failed scenario URI + line number.</li>
 *   <li>Running this class re-executes only those failed scenarios.</li>
 * </ol>
 *
 * <p>Usage:
 * <pre>
 *   mvn test -Dtest=FailedTestRunner -Dcucumber.features="@target/rerun.txt"
 * </pre>
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.saucelabs.step_definitions")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, html:target/cucumber-reports/failed-report.html")
@ConfigurationParameter(key = "cucumber.publish.quiet", value = "true")
public class FailedTestRunner {
}
