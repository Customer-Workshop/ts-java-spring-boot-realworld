package io.spring.selenium.tests;

import static org.testng.Assert.*;

import io.spring.selenium.pages.HomePage;
import io.spring.selenium.pages.ProfilePage;
import io.spring.selenium.pages.RegisterPage;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Selenium E2E tests for user profile functionality. */
public class ProfileTest extends BaseTest {

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
    registeredUsername = "profileuser" + timestamp;
    registeredEmail = "profileuser" + timestamp + "@test.com";

    RegisterPage registerPage = new RegisterPage(driver);
    registerPage.navigateTo(baseUrl);
    registerPage.register(registeredUsername, registeredEmail, PASSWORD);

    WebDriverWait wait = new WebDriverWait(driver, 15);
    wait.until(ExpectedConditions.urlToBe(baseUrl + "/"));
  }

  @Test(groups = {"smoke", "regression"})
  public void testViewOwnProfile() {
    createTest("testViewOwnProfile", "Verify user can view their own profile page");

    ProfilePage profilePage = new ProfilePage(driver);
    profilePage.navigateTo(baseUrl, registeredUsername);

    assertTrue(profilePage.isPageLoaded(), "Profile page should load");
    assertEquals(
        profilePage.getProfileUsername(),
        registeredUsername,
        "Profile username should match registered username");

    test.info("Own profile page loaded successfully for user: " + registeredUsername);
  }

  @Test(groups = {"regression"})
  public void testProfileDisplaysUsername() {
    createTest("testProfileDisplaysUsername", "Verify profile page displays correct username");

    ProfilePage profilePage = new ProfilePage(driver);
    profilePage.navigateTo(baseUrl, registeredUsername);

    String displayedUsername = profilePage.getProfileUsername();
    assertEquals(
        displayedUsername,
        registeredUsername,
        "Displayed username should match the registered username");

    test.info("Profile displays correct username: " + displayedUsername);
  }

  @Test(groups = {"regression"})
  public void testProfilePageHasImage() {
    createTest("testProfilePageHasImage", "Verify profile page displays user image");

    ProfilePage profilePage = new ProfilePage(driver);
    profilePage.navigateTo(baseUrl, registeredUsername);

    assertTrue(profilePage.isProfileImageDisplayed(), "Profile image should be displayed");

    test.info("Profile image is displayed");
  }

  @Test(groups = {"regression"})
  public void testNavigateToProfileFromNavbar() {
    createTest(
        "testNavigateToProfileFromNavbar",
        "Verify user can navigate to profile from navbar username link");

    HomePage homePage = new HomePage(driver);
    homePage.navigateTo(baseUrl);

    // Check if username link is in navbar
    assertTrue(
        homePage.isUsernameInNavbar(registeredUsername),
        "Username should appear in navbar when logged in");

    // Click on the username link in navbar
    driver.findElement(By.cssSelector("a[href='/profile/" + registeredUsername + "']")).click();

    WebDriverWait wait = new WebDriverWait(driver, 10);
    wait.until(ExpectedConditions.urlContains("/profile/" + registeredUsername));

    assertTrue(
        driver.getCurrentUrl().contains("/profile/" + registeredUsername),
        "Should navigate to profile page");

    test.info("Successfully navigated to profile from navbar");
  }

  @Test(groups = {"regression"})
  public void testProfileShowsEditSettingsForOwnProfile() {
    createTest(
        "testProfileShowsEditSettingsForOwnProfile",
        "Verify edit profile settings link is shown on own profile");

    ProfilePage profilePage = new ProfilePage(driver);
    profilePage.navigateTo(baseUrl, registeredUsername);

    assertTrue(
        profilePage.isEditProfileLinkDisplayed(),
        "Edit profile settings link should be displayed on own profile");

    test.info("Edit profile settings link is displayed on own profile");
  }
}
