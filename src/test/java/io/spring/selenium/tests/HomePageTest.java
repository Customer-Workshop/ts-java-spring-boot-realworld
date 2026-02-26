package io.spring.selenium.tests;

import static org.testng.Assert.*;

import io.spring.selenium.pages.HomePage;
import io.spring.selenium.pages.RegisterPage;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Selenium E2E tests for the Home page. */
public class HomePageTest extends BaseTest {

  private HomePage homePage;
  private String baseUrl;

  @Override
  @BeforeMethod
  public void setupTest() {
    super.setupTest();
    baseUrl = config.getProperty("base.url", "http://localhost:3000");
    homePage = new HomePage(driver);
  }

  @Test(groups = {"smoke", "regression"})
  public void testHomePageLoads() {
    createTest("testHomePageLoads", "Verify home page loads correctly");

    homePage.navigateTo(baseUrl);

    assertTrue(homePage.isBannerDisplayed(), "Banner should be displayed on home page");
    assertEquals(homePage.getNavbarBrandText(), "conduit", "Navbar brand should display 'conduit'");

    test.info("Home page loaded successfully");
  }

  @Test(groups = {"smoke", "regression"})
  public void testHomePageShowsSignInSignUpWhenLoggedOut() {
    createTest(
        "testHomePageShowsSignInSignUpWhenLoggedOut",
        "Verify Sign in and Sign up links shown when not logged in");

    homePage.navigateTo(baseUrl);

    assertTrue(homePage.isSignInLinkDisplayed(), "Sign in link should be displayed");
    assertTrue(homePage.isSignUpLinkDisplayed(), "Sign up link should be displayed");
    assertTrue(homePage.isLoggedOutNavbarDisplayed(), "Logged out navbar should be displayed");

    test.info("Sign in and Sign up links displayed correctly for logged out user");
  }

  @Test(groups = {"smoke", "regression"})
  public void testHomePageShowsNewPostAndSettingsWhenLoggedIn() {
    createTest(
        "testHomePageShowsNewPostAndSettingsWhenLoggedIn",
        "Verify New Post and Settings links shown when logged in");

    // Register a user first
    String timestamp = String.valueOf(System.currentTimeMillis());
    String username = "homeuser" + timestamp;
    String email = "homeuser" + timestamp + "@test.com";

    RegisterPage registerPage = new RegisterPage(driver);
    registerPage.navigateTo(baseUrl);
    registerPage.register(username, email, "password123");

    WebDriverWait wait = new WebDriverWait(driver, 15);
    wait.until(ExpectedConditions.urlToBe(baseUrl + "/"));

    homePage = new HomePage(driver);
    homePage.navigateTo(baseUrl);

    assertTrue(homePage.isNewPostLinkDisplayed(), "New Post link should be displayed");
    assertTrue(homePage.isSettingsLinkDisplayed(), "Settings link should be displayed");
    assertTrue(homePage.isLoggedInNavbarDisplayed(), "Logged in navbar should be displayed");

    test.info("New Post and Settings links displayed correctly for logged in user");
  }

  @Test(groups = {"regression"})
  public void testHomePageBannerTitle() {
    createTest("testHomePageBannerTitle", "Verify home page banner has correct title");

    homePage.navigateTo(baseUrl);

    String bannerTitle = homePage.getBannerTitle();
    assertNotNull(bannerTitle, "Banner title should not be null");
    assertTrue(bannerTitle.length() > 0, "Banner title should not be empty");

    test.info("Banner title: " + bannerTitle);
  }

  @Test(groups = {"regression"})
  public void testNavbarBrandLinksToHome() {
    createTest("testNavbarBrandLinksToHome", "Verify navbar brand links to home page");

    homePage.navigateTo(baseUrl);

    String navBrand = homePage.getNavbarBrandText();
    assertNotNull(navBrand, "Navbar brand text should not be null");
    assertEquals(navBrand, "conduit", "Navbar brand should be 'conduit'");

    test.info("Navbar brand text: " + navBrand);
  }
}
