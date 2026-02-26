package io.spring.selenium.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** Page object for the Settings page. */
public class SettingsPage extends BasePage {

  @FindBy(css = "input[placeholder='URL of profile picture']")
  private WebElement imageUrlInput;

  @FindBy(css = "input[placeholder='Username']")
  private WebElement usernameInput;

  @FindBy(css = "textarea[placeholder='Short bio about you']")
  private WebElement bioTextarea;

  @FindBy(css = "input[placeholder='Email']")
  private WebElement emailInput;

  @FindBy(css = "input[placeholder='New Password']")
  private WebElement passwordInput;

  @FindBy(css = "button[type='submit']")
  private WebElement updateSettingsButton;

  @FindBy(css = "button.btn-outline-danger")
  private WebElement logoutButton;

  @FindBy(css = "h1.text-xs-center")
  private WebElement pageTitle;

  @FindBy(css = ".error-messages")
  private WebElement errorMessages;

  public SettingsPage(WebDriver driver) {
    super(driver);
  }

  public void navigateTo(String baseUrl) {
    driver.get(baseUrl + "/user/settings");
    wait.until(ExpectedConditions.visibilityOf(pageTitle));
  }

  public String getPageTitle() {
    return getText(pageTitle);
  }

  public void enterImageUrl(String imageUrl) {
    type(imageUrlInput, imageUrl);
  }

  public void enterUsername(String username) {
    type(usernameInput, username);
  }

  public void enterBio(String bio) {
    type(bioTextarea, bio);
  }

  public void enterEmail(String email) {
    type(emailInput, email);
  }

  public void enterPassword(String password) {
    type(passwordInput, password);
  }

  public void clickUpdateSettings() {
    click(updateSettingsButton);
  }

  public HomePage clickLogout() {
    click(logoutButton);
    return new HomePage(driver);
  }

  public String getCurrentUsername() {
    return waitForVisibility(usernameInput).getAttribute("value");
  }

  public String getCurrentEmail() {
    return waitForVisibility(emailInput).getAttribute("value");
  }

  public String getCurrentBio() {
    return waitForVisibility(bioTextarea).getAttribute("value");
  }

  public String getCurrentImageUrl() {
    return waitForVisibility(imageUrlInput).getAttribute("value");
  }

  public boolean isPageLoaded() {
    return isDisplayed(pageTitle) && isDisplayed(updateSettingsButton) && isDisplayed(logoutButton);
  }

  public boolean isErrorMessageDisplayed() {
    try {
      wait.until(ExpectedConditions.visibilityOf(errorMessages));
      return errorMessages.isDisplayed();
    } catch (Exception e) {
      return false;
    }
  }
}
