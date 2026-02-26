package io.spring.selenium.pages;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** Page object for the Home page. */
public class HomePage extends BasePage {

  @FindBy(css = ".navbar-brand")
  private WebElement navbarBrand;

  @FindBy(css = ".banner h1")
  private WebElement bannerTitle;

  @FindBy(css = ".tag-list .tag-pill")
  private List<WebElement> popularTags;

  @FindBy(css = ".article-preview")
  private List<WebElement> articlePreviews;

  @FindBy(linkText = "Sign in")
  private WebElement signInLink;

  @FindBy(linkText = "Sign up")
  private WebElement signUpLink;

  @FindBy(linkText = "Home")
  private WebElement homeLink;

  @FindBy(css = "a[href='/editor/new']")
  private WebElement newPostLink;

  @FindBy(css = "a[href='/user/settings']")
  private WebElement settingsLink;

  public HomePage(WebDriver driver) {
    super(driver);
  }

  public void navigateTo(String baseUrl) {
    driver.get(baseUrl);
    wait.until(
        ExpectedConditions.or(
            ExpectedConditions.visibilityOf(bannerTitle),
            ExpectedConditions.presenceOfElementLocated(By.cssSelector(".navbar-brand"))));
  }

  public String getBannerTitle() {
    return getText(bannerTitle);
  }

  public boolean isBannerDisplayed() {
    return isDisplayed(bannerTitle);
  }

  public boolean isSignInLinkDisplayed() {
    return isDisplayed(signInLink);
  }

  public boolean isSignUpLinkDisplayed() {
    return isDisplayed(signUpLink);
  }

  public boolean isNewPostLinkDisplayed() {
    return isDisplayed(newPostLink);
  }

  public boolean isSettingsLinkDisplayed() {
    return isDisplayed(settingsLink);
  }

  public LoginPage clickSignIn() {
    click(signInLink);
    return new LoginPage(driver);
  }

  public RegisterPage clickSignUp() {
    click(signUpLink);
    return new RegisterPage(driver);
  }

  public EditorPage clickNewPost() {
    click(newPostLink);
    return new EditorPage(driver);
  }

  public SettingsPage clickSettings() {
    click(settingsLink);
    return new SettingsPage(driver);
  }

  public int getArticlePreviewCount() {
    return articlePreviews.size();
  }

  public int getPopularTagCount() {
    return popularTags.size();
  }

  public String getNavbarBrandText() {
    return getText(navbarBrand);
  }

  public boolean isLoggedInNavbarDisplayed() {
    return isNewPostLinkDisplayed() && isSettingsLinkDisplayed();
  }

  public boolean isLoggedOutNavbarDisplayed() {
    return isSignInLinkDisplayed() && isSignUpLinkDisplayed();
  }

  /** Click on an article preview by index. */
  public ArticlePage clickArticlePreview(int index) {
    if (index < articlePreviews.size()) {
      WebElement preview = articlePreviews.get(index);
      WebElement readMoreLink = preview.findElement(By.cssSelector("a.preview-link"));
      // Scroll element into view before clicking (needed for headless mode)
      ((JavascriptExecutor) driver)
          .executeScript("arguments[0].scrollIntoView(true);", readMoreLink);
      wait.until(ExpectedConditions.elementToBeClickable(readMoreLink));
      readMoreLink.click();
      return new ArticlePage(driver);
    }
    throw new IndexOutOfBoundsException("Article preview index out of bounds: " + index);
  }

  /** Get the title of an article preview by index. */
  public String getArticlePreviewTitle(int index) {
    if (index < articlePreviews.size()) {
      WebElement preview = articlePreviews.get(index);
      WebElement title = preview.findElement(By.cssSelector("h1"));
      return title.getText();
    }
    throw new IndexOutOfBoundsException("Article preview index out of bounds: " + index);
  }

  /** Check if the current user's username appears in the navbar. */
  public boolean isUsernameInNavbar(String username) {
    try {
      WebElement usernameLink =
          driver.findElement(By.cssSelector("a[href='/profile/" + username + "']"));
      return usernameLink.isDisplayed();
    } catch (Exception e) {
      return false;
    }
  }
}
