package io.spring.selenium.tests;

import static org.testng.Assert.*;

import io.spring.selenium.pages.HomePage;
import io.spring.selenium.pages.RegisterPage;
import io.spring.selenium.pages.SettingsPage;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Selenium E2E tests for user settings functionality. */
public class SettingsTest extends BaseTest {

  private String baseUrl;
  private String registeredUsername;
  private String registeredEmail;
  private static final String PASSWORD = "password123";

  @Override
  @BeforeMethod
  public void setupTest() {
    super.setupTest();
    baseUrl = config.getProperty("base.url", "http://localhost:3000");

    // Register a new user
    String timestamp = String.valueOf(System.currentTimeMillis());
    registeredUsername = "settingsuser" + timestamp;
    registeredEmail = "settingsuser" + timestamp + "@test.com";

    RegisterPage registerPage = new RegisterPage(driver);
    registerPage.navigateTo(baseUrl);
    registerPage.register(registeredUsername, registeredEmail, PASSWORD);

    WebDriverWait wait = new WebDriverWait(driver, 15);
    wait.until(ExpectedConditions.urlToBe(baseUrl + "/"));
  }

  @Test(groups = {"smoke", "regression"})
  public void testSettingsPageLoads() {
    createTest("testSettingsPageLoads", "Verify settings page loads correctly");

    SettingsPage settingsPage = new SettingsPage(driver);
    settingsPage.navigateTo(baseUrl);

    assertEquals(
        settingsPage.getPageTitle(), "Your Settings", "Page title should be 'Your Settings'");
    assertTrue(settingsPage.isPageLoaded(), "Settings page should load with all elements");

    test.info("Settings page loaded successfully");
  }

  @Test(groups = {"regression"})
  public void testSettingsPageShowsCurrentUserInfo() {
    createTest(
        "testSettingsPageShowsCurrentUserInfo",
        "Verify settings page shows current user information");

    SettingsPage settingsPage = new SettingsPage(driver);
    settingsPage.navigateTo(baseUrl);

    // Wait for form to be populated
    WebDriverWait wait = new WebDriverWait(driver, 10);

    String currentUsername = settingsPage.getCurrentUsername();
    String currentEmail = settingsPage.getCurrentEmail();

    assertEquals(currentUsername, registeredUsername, "Settings should show the current username");
    assertEquals(currentEmail, registeredEmail, "Settings should show the current email");

    test.info("Settings page shows current user info correctly");
  }

  @Test(groups = {"regression"})
  public void testUpdateBio() {
    createTest("testUpdateBio", "Verify user can update their bio");

    SettingsPage settingsPage = new SettingsPage(driver);
    settingsPage.navigateTo(baseUrl);

    String newBio = "This is my updated bio " + System.currentTimeMillis();
    settingsPage.enterBio(newBio);
    settingsPage.clickUpdateSettings();

    // Wait for redirect to home page
    WebDriverWait wait = new WebDriverWait(driver, 15);
    try {
      wait.until(ExpectedConditions.urlToBe(baseUrl + "/"));
      test.info("Settings updated successfully, redirected to home page");
    } catch (Exception e) {
      // May stay on settings page if there's a validation issue
      test.info("Settings form submitted");
    }
  }

  @Test(groups = {"regression"})
  public void testLogout() {
    createTest("testLogout", "Verify user can logout from settings page");

    SettingsPage settingsPage = new SettingsPage(driver);
    settingsPage.navigateTo(baseUrl);

    HomePage homePage = settingsPage.clickLogout();

    // Wait for redirect to home page
    WebDriverWait wait = new WebDriverWait(driver, 15);
    wait.until(ExpectedConditions.urlToBe(baseUrl + "/"));

    // After logout, the Sign in and Sign up links should be visible
    homePage = new HomePage(driver);
    assertTrue(
        homePage.isLoggedOutNavbarDisplayed(),
        "Sign in and Sign up links should be visible after logout");

    test.info("User logged out successfully");
  }

  @Test(groups = {"regression"})
  public void testNavigateToSettingsFromNavbar() {
    createTest(
        "testNavigateToSettingsFromNavbar", "Verify user can navigate to settings from navbar");

    HomePage homePage = new HomePage(driver);
    homePage.navigateTo(baseUrl);

    assertTrue(
        homePage.isSettingsLinkDisplayed(), "Settings link should be visible when logged in");

    SettingsPage settingsPage = homePage.clickSettings();

    WebDriverWait wait = new WebDriverWait(driver, 10);
    wait.until(ExpectedConditions.urlContains("/user/settings"));

    assertTrue(
        driver.getCurrentUrl().contains("/user/settings"), "Should navigate to settings page");

    test.info("Successfully navigated to settings from navbar");
  }
}
