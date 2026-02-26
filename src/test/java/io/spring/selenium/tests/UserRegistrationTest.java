package io.spring.selenium.tests;

import static org.testng.Assert.*;

import io.spring.selenium.pages.HomePage;
import io.spring.selenium.pages.LoginPage;
import io.spring.selenium.pages.RegisterPage;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Selenium E2E tests for user registration functionality. */
public class UserRegistrationTest extends BaseTest {

  private RegisterPage registerPage;
  private String baseUrl;

  @Override
  @BeforeMethod
  public void setupTest() {
    super.setupTest();
    baseUrl = config.getProperty("base.url", "http://localhost:3000");
    registerPage = new RegisterPage(driver);
  }

  @Test(groups = {"smoke", "regression"})
  public void testRegisterPageLoads() {
    createTest("testRegisterPageLoads", "Verify register page loads with all form elements");

    registerPage.navigateTo(baseUrl);

    assertEquals(registerPage.getPageTitle(), "Sign Up", "Page title should be 'Sign Up'");
    assertTrue(registerPage.isPageLoaded(), "All register form elements should be displayed");

    test.info("Register page loaded successfully with all form elements");
  }

  @Test(groups = {"smoke", "regression"})
  public void testSuccessfulRegistration() {
    createTest("testSuccessfulRegistration", "Verify user can register with valid credentials");

    registerPage.navigateTo(baseUrl);

    String timestamp = String.valueOf(System.currentTimeMillis());
    String username = "testuser" + timestamp;
    String email = "testuser" + timestamp + "@test.com";
    String password = "password123";

    HomePage homePage = registerPage.register(username, email, password);

    // Wait for redirect to home page
    WebDriverWait wait = new WebDriverWait(driver, 15);
    wait.until(ExpectedConditions.urlToBe(baseUrl + "/"));

    assertTrue(
        driver.getCurrentUrl().equals(baseUrl + "/") || driver.getCurrentUrl().equals(baseUrl + ""),
        "Should be redirected to home page after registration");

    test.info("User registered successfully with username: " + username);
  }

  @Test(groups = {"regression"})
  public void testRegistrationWithEmptyUsername() {
    createTest(
        "testRegistrationWithEmptyUsername",
        "Verify error shown when registering with empty username");

    registerPage.navigateTo(baseUrl);

    registerPage.enterEmail("test@test.com");
    registerPage.enterPassword("password123");
    registerPage.clickSignUp();

    // Verify error message is displayed
    assertTrue(
        registerPage.isErrorMessageDisplayed(),
        "Error message should be displayed for empty username");

    test.info("Error message correctly displayed for empty username");
  }

  @Test(groups = {"regression"})
  public void testRegistrationWithEmptyEmail() {
    createTest(
        "testRegistrationWithEmptyEmail", "Verify error shown when registering with empty email");

    registerPage.navigateTo(baseUrl);

    registerPage.enterUsername("testuser");
    registerPage.enterPassword("password123");
    registerPage.clickSignUp();

    // The form should not submit or should show an error
    String currentUrl = driver.getCurrentUrl();
    assertTrue(
        currentUrl.contains("/user/register"), "Should stay on register page when email is empty");

    test.info("Registration prevented with empty email");
  }

  @Test(groups = {"regression"})
  public void testRegistrationWithEmptyPassword() {
    createTest(
        "testRegistrationWithEmptyPassword",
        "Verify error shown when registering with empty password");

    registerPage.navigateTo(baseUrl);

    registerPage.enterUsername("testuser");
    registerPage.enterEmail("test@test.com");
    registerPage.clickSignUp();

    // Should stay on register page or show error
    String currentUrl = driver.getCurrentUrl();
    assertTrue(
        currentUrl.contains("/user/register"),
        "Should stay on register page when password is empty");

    test.info("Registration prevented with empty password");
  }

  @Test(groups = {"regression"})
  public void testNavigateToLoginFromRegister() {
    createTest(
        "testNavigateToLoginFromRegister",
        "Verify 'Have an account?' link navigates to login page");

    registerPage.navigateTo(baseUrl);

    LoginPage loginPage = registerPage.clickHaveAccountLink();

    WebDriverWait wait = new WebDriverWait(driver, 10);
    wait.until(ExpectedConditions.urlContains("/user/login"));

    assertTrue(driver.getCurrentUrl().contains("/user/login"), "Should navigate to login page");

    test.info("Successfully navigated from register to login page");
  }
}
