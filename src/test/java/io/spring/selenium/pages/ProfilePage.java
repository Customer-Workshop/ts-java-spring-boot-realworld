package io.spring.selenium.pages;

import java.util.List;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** Page object for the Profile page. */
public class ProfilePage extends BasePage {

  @FindBy(css = ".user-info h4")
  private WebElement profileUsername;

  @FindBy(css = ".user-info p")
  private WebElement profileBio;

  @FindBy(css = ".user-info img")
  private WebElement profileImage;

  @FindBy(css = "a[href*='/user/settings']")
  private WebElement editProfileLink;

  @FindBy(css = "button.action-btn")
  private WebElement followButton;

  @FindBy(css = ".article-preview")
  private List<WebElement> articlePreviews;

  @FindBy(css = ".articles-toggle .nav-link")
  private List<WebElement> articlesTabs;

  public ProfilePage(WebDriver driver) {
    super(driver);
  }

  public void navigateTo(String baseUrl, String username) {
    driver.get(baseUrl + "/profile/" + username);
    wait.until(ExpectedConditions.visibilityOf(profileUsername));
  }

  public String getProfileUsername() {
    return getText(profileUsername);
  }

  public String getProfileBio() {
    return getText(profileBio);
  }

  public boolean isProfileImageDisplayed() {
    return isDisplayed(profileImage);
  }

  public boolean isEditProfileLinkDisplayed() {
    return isDisplayed(editProfileLink);
  }

  public boolean isFollowButtonDisplayed() {
    return isDisplayed(followButton);
  }

  public void clickFollow() {
    click(followButton);
  }

  public String getFollowButtonText() {
    return getText(followButton);
  }

  public SettingsPage clickEditProfile() {
    click(editProfileLink);
    return new SettingsPage(driver);
  }

  public int getArticlePreviewCount() {
    return articlePreviews.size();
  }

  /** Click on the 'My Articles' tab. */
  public void clickMyArticlesTab() {
    for (WebElement tab : articlesTabs) {
      if (tab.getText().contains("My Articles") || tab.getText().contains("My Posts")) {
        click(tab);
        return;
      }
    }
  }

  /** Click on the 'Favorited Articles' tab. */
  public void clickFavoritedArticlesTab() {
    for (WebElement tab : articlesTabs) {
      if (tab.getText().contains("Favorited")) {
        click(tab);
        return;
      }
    }
  }

  public boolean isPageLoaded() {
    try {
      wait.until(ExpectedConditions.visibilityOf(profileUsername));
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
