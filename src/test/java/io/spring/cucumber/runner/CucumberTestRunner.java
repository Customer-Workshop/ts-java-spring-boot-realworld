package io.spring.cucumber.runner;

import static io.cucumber.junit.platform.engine.Constants.FEATURES_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Cucumber BDD Test Runner.
 *
 * <p>Runs all feature files in src/test/resources/features and produces both HTML and JSON reports
 * that can be shared with business stakeholders.
 *
 * <p>Reports are generated at:
 *
 * <ul>
 *   <li>HTML: build/reports/cucumber/cucumber-report.html
 *   <li>JSON: build/reports/cucumber/cucumber-report.json
 * </ul>
 *
 * <p>Run with: ./gradlew cucumberTest
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = FEATURES_PROPERTY_NAME, value = "classpath:features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "io.spring.cucumber")
@ConfigurationParameter(
    key = PLUGIN_PROPERTY_NAME,
    value =
        "pretty,"
            + "html:build/reports/cucumber/cucumber-report.html,"
            + "json:build/reports/cucumber/cucumber-report.json")
public class CucumberTestRunner {}
