package io.spring.bdd;

import static io.cucumber.junit.platform.engine.Constants.FEATURES_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.Suite;

/**
 * JUnit 5 test suite runner for Cucumber BDD tests.
 *
 * <p>This runner discovers and executes all Cucumber feature files located under {@code
 * src/test/resources/features}. Step definitions and hooks are scanned from the {@code
 * io.spring.bdd} package.
 *
 * <p>Reports are generated in two formats:
 *
 * <ul>
 *   <li>HTML report at {@code build/reports/cucumber/cucumber-report.html} for stakeholder review
 *   <li>JSON report at {@code build/reports/cucumber/cucumber-report.json} for CI integration
 * </ul>
 *
 * <p>Run via Gradle: {@code ./gradlew cucumberTest}
 */
@Suite
@IncludeEngines("cucumber")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "io.spring.bdd")
@ConfigurationParameter(key = FEATURES_PROPERTY_NAME, value = "src/test/resources/features")
@ConfigurationParameter(
    key = PLUGIN_PROPERTY_NAME,
    value =
        "pretty, html:build/reports/cucumber/cucumber-report.html,"
            + " json:build/reports/cucumber/cucumber-report.json")
public class CucumberTestRunner {}
