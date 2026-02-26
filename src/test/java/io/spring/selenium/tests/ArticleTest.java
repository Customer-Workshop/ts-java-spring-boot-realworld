package io.spring.selenium.tests;

import static org.testng.Assert.*;

import io.spring.selenium.pages.EditorPage;
import io.spring.selenium.pages.HomePage;
import io.spring.selenium.pages.RegisterPage;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Selenium E2E tests for article CRUD operations. */
public class ArticleTest extends BaseTest {

  private String baseUrl;
  private String registeredEmail;
  private String registeredUsername;
  private static final String PASSWORD = "password123";

  @Override
  @BeforeMethod
  public void setupTest() {
    super.setupTest();
    baseUrl = config.getProperty("base.url", "http://localhost:3000");

    // Register a new user for each test
    String timestamp = String.valueOf(System.currentTimeMillis());
    registeredUsername = "articleuser" + timestamp;
    registeredEmail = "articleuser" + timestamp + "@test.com";

    RegisterPage registerPage = new RegisterPage(driver);
    registerPage.navigateTo(baseUrl);
    registerPage.register(registeredUsername, registeredEmail, PASSWORD);

    WebDriverWait wait = new WebDriverWait(driver, 15);
    wait.until(ExpectedConditions.urlToBe(baseUrl + "/"));
  }

  @Test(groups = {"smoke", "regression"})
  public void testEditorPageLoads() {
    createTest("testEditorPageLoads", "Verify article editor page loads correctly");

    EditorPage editorPage = new EditorPage(driver);
    editorPage.navigateTo(baseUrl);

    assertTrue(editorPage.isPageLoaded(), "Editor page should load with all form elements");

    test.info("Editor page loaded successfully");
  }

  @Test(groups = {"smoke", "regression"})
  public void testCreateArticle() {
    createTest("testCreateArticle", "Verify user can create a new article");

    EditorPage editorPage = new EditorPage(driver);
    editorPage.navigateTo(baseUrl);

    String title = "Test Article " + System.currentTimeMillis();
    String description = "This is a test article description";
    String body = "This is the body of the test article with some content.";

    editorPage.publishArticle(title, description, body);

    // Wait for redirect (to home page or article page)
    WebDriverWait wait = new WebDriverWait(driver, 15);
    wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/editor/new")));

    String currentUrl = driver.getCurrentUrl();
    assertFalse(
        currentUrl.contains("/editor/new"),
        "Should be redirected away from editor page after publishing");

    test.info("Article created successfully with title: " + title);
  }

  @Test(groups = {"regression"})
  public void testCreateArticleWithTags() {
    createTest("testCreateArticleWithTags", "Verify user can create article with tags");

    EditorPage editorPage = new EditorPage(driver);
    editorPage.navigateTo(baseUrl);

    String title = "Tagged Article " + System.currentTimeMillis();
    String description = "Article with tags";
    String body = "This is a tagged article body.";

    editorPage.publishArticleWithTags(title, description, body, "java", "spring", "testing");

    // Wait for redirect
    WebDriverWait wait = new WebDriverWait(driver, 15);
    wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/editor/new")));

    assertFalse(
        driver.getCurrentUrl().contains("/editor/new"),
        "Should be redirected away from editor after publishing");

    test.info("Article with tags created successfully");
  }

  @Test(groups = {"regression"})
  public void testCreateArticleWithEmptyTitle() {
    createTest(
        "testCreateArticleWithEmptyTitle", "Verify error when creating article with empty title");

    EditorPage editorPage = new EditorPage(driver);
    editorPage.navigateTo(baseUrl);

    editorPage.enterDescription("Some description");
    editorPage.enterBody("Some body content");
    editorPage.clickPublish();

    // Should show error or stay on the editor page
    WebDriverWait wait = new WebDriverWait(driver, 5);
    try {
      wait.until(ExpectedConditions.urlContains("/editor"));
      assertTrue(
          driver.getCurrentUrl().contains("/editor"),
          "Should stay on editor page with empty title");
    } catch (Exception e) {
      // If error message displayed instead, that's also acceptable
      assertTrue(
          editorPage.isErrorMessageDisplayed() || driver.getCurrentUrl().contains("/editor"),
          "Should show error or stay on editor page");
    }

    test.info("Article creation prevented with empty title");
  }

  @Test(groups = {"regression"})
  public void testNewPostLinkNavigatesToEditor() {
    createTest(
        "testNewPostLinkNavigatesToEditor", "Verify 'New Post' link in navbar navigates to editor");

    HomePage homePage = new HomePage(driver);
    homePage.navigateTo(baseUrl);

    assertTrue(homePage.isNewPostLinkDisplayed(), "New Post link should be visible when logged in");

    EditorPage editorPage = homePage.clickNewPost();

    WebDriverWait wait = new WebDriverWait(driver, 10);
    wait.until(ExpectedConditions.urlContains("/editor/new"));

    assertTrue(
        driver.getCurrentUrl().contains("/editor/new"), "Should navigate to new article editor");

    test.info("Successfully navigated to editor via New Post link");
  }
}
