package io.spring.bdd;

import java.io.File;
import java.util.Collections;
import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;

/**
 * Standalone entry point for generating enhanced Cucumber HTML reports using the Masterthought
 * reporting library.
 *
 * <p>Invoked by the Gradle {@code generateCucumberReport} task as a {@code JavaExec} process. This
 * avoids placing the Masterthought dependency on the Gradle buildscript classpath, which can cause
 * jar-instrumentation cache conflicts with Gradle 7.x.
 *
 * <p>Usage: {@code java io.spring.bdd.GenerateCucumberReport <jsonPath> <outputDir> <projectName>}
 *
 * <ul>
 *   <li>{@code jsonPath} &mdash; path to the Cucumber JSON report file
 *   <li>{@code outputDir} &mdash; directory where the HTML report will be written
 *   <li>{@code projectName} &mdash; project name displayed in the report header
 * </ul>
 */
public final class GenerateCucumberReport {

  private GenerateCucumberReport() {}

  /** Generates a Masterthought Cucumber HTML report from the provided arguments. */
  public static void main(String[] args) {
    if (args.length < 3) {
      System.err.println(
          "Usage: GenerateCucumberReport <jsonReportPath> <outputDirectory> <projectName>");
      System.exit(1);
    }

    String jsonReportPath = args[0];
    String outputDirectory = args[1];
    String projectName = args[2];

    File jsonFile = new File(jsonReportPath);
    if (!jsonFile.exists()) {
      System.out.println(
          "Cucumber JSON report not found at "
              + jsonReportPath
              + ". Skipping Masterthought report generation.");
      return;
    }

    File outputDir = new File(outputDirectory);
    if (!outputDir.exists()) {
      outputDir.mkdirs();
    }

    Configuration configuration = new Configuration(outputDir, projectName);
    configuration.addClassifications("Platform", System.getProperty("os.name"));

    ReportBuilder reportBuilder =
        new ReportBuilder(Collections.singletonList(jsonFile.getAbsolutePath()), configuration);
    reportBuilder.generateReports();

    System.out.println(
        "Masterthought Cucumber report generated at: "
            + outputDir.getAbsolutePath()
            + "/cucumber-html-reports/overview-features.html");
  }
}
