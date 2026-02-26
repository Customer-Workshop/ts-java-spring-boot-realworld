package io.spring.selenium.tests;

import static org.testng.Assert.*;

import io.spring.selenium.pages.HomePage;
import io.spring.selenium.pages.LoginPage;
import io.spring.selenium.pages.RegisterPage;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Selenium E2E tests for user login functionality. */
public class UserLoginTest extends BaseTest {

  private LoginPage loginPage;
  private String baseUrl;

  @Override
  @BeforeMethod
  public void setupTest() {
    super.setupTest();
    baseUrl = config.getProperty("base.url", "http://localhost:3000");
    loginPage = new LoginPage(driver);
  }

  @Test(groups = {"smoke", "regression"})
  public void testLoginPageLoads() {
    createTest("testLoginPageLoads", "Verify login page loads with all form elements");

    loginPage.navigateTo(baseUrl);

    assertEquals(loginPage.getPageTitle(), "Sign in", "Page title should be 'Sign in'");
    assertTrue(loginPage.isPageLoaded(), "All login form elements should be displayed");

    test.info("Login page loaded successfully with all form elements");
  }

  @Test(groups = {"smoke", "regression"})
  public void testSuccessfulLogin() {
    createTest("testSuccessfulLogin", "Verify user can login with valid credentials");

    // First register a user
    String timestamp = String.valueOf(System.currentTimeMillis());
    String username = "loginuser" + timestamp;
    String email = "loginuser" + timestamp + "@test.com";
    String password = "password123";

    RegisterPage registerPage = new RegisterPage(driver);
    registerPage.navigateTo(baseUrl);
    registerPage.register(username, email, password);

    WebDriverWait wait = new WebDriverWait(driver, 15);
    wait.until(ExpectedConditions.urlToBe(baseUrl + "/"));

    // Clear local storage and navigate to login
    driver.manage().deleteAllCookies();
    driver.navigate().to("javascript:void(window.localStorage.removeItem('user'))");

    loginPage.navigateTo(baseUrl);
    HomePage homePage = loginPage.login(email, password);

    wait.until(ExpectedConditions.urlToBe(baseUrl + "/"));

    assertTrue(
        driver.getCurrentUrl().equals(baseUrl + "/") || driver.getCurrentUrl().equals(baseUrl + ""),
        "Should be redirected to home page after login");

    test.info("User logged in successfully with email: " + email);
  }

  @Test(groups = {"regression"})
  public void testLoginWithWrongPassword() {
    createTest("testLoginWithWrongPassword", "Verify error shown for wrong password");

    loginPage.navigateTo(baseUrl);

    loginPage.enterEmail("nonexistent@test.com");
    loginPage.enterPassword("wrongpassword");
    loginPage.clickSignIn();

    // Wait for error message
    assertTrue(
        loginPage.isErrorMessageDisplayed(),
        "Error message should be displayed for wrong password");

    test.info("Error message displayed correctly for wrong password");
  }

  @Test(groups = {"regression"})
  public void testLoginWithEmptyEmail() {
    createTest("testLoginWithEmptyEmail", "Verify login fails with empty email");

    loginPage.navigateTo(baseUrl);

    loginPage.enterPassword("password123");
    loginPage.clickSignIn();

    // Should stay on login page
    String currentUrl = driver.getCurrentUrl();
    assertTrue(currentUrl.contains("/user/login"), "Should stay on login page when email is empty");

    test.info("Login prevented with empty email");
  }

  @Test(groups = {"regression"})
  public void testLoginWithEmptyPassword() {
    createTest("testLoginWithEmptyPassword", "Verify login fails with empty password");

    loginPage.navigateTo(baseUrl);

    loginPage.enterEmail("test@test.com");
    loginPage.clickSignIn();

    // Should stay on login page or show error
    String currentUrl = driver.getCurrentUrl();
    assertTrue(
        currentUrl.contains("/user/login"), "Should stay on login page when password is empty");

    test.info("Login prevented with empty password");
  }

  @Test(groups = {"regression"})
  public void testNavigateToRegisterFromLogin() {
    createTest(
        "testNavigateToRegisterFromLogin",
        "Verify 'Need an account?' link navigates to register page");

    loginPage.navigateTo(baseUrl);

    RegisterPage registerPage = loginPage.clickNeedAccountLink();

    WebDriverWait wait = new WebDriverWait(driver, 10);
    wait.until(ExpectedConditions.urlContains("/user/register"));

    assertTrue(
        driver.getCurrentUrl().contains("/user/register"), "Should navigate to register page");

    test.info("Successfully navigated from login to register page");
  }
}
