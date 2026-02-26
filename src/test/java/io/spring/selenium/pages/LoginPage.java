package io.spring.selenium.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** Page object for the Login page. */
public class LoginPage extends BasePage {

  @FindBy(css = "input[type='email']")
  private WebElement emailInput;

  @FindBy(css = "input[type='password']")
  private WebElement passwordInput;

  @FindBy(css = "button[type='submit']")
  private WebElement signInButton;

  @FindBy(css = "h1.text-xs-center")
  private WebElement pageTitle;

  @FindBy(linkText = "Need an account?")
  private WebElement needAccountLink;

  @FindBy(css = ".error-messages")
  private WebElement errorMessages;

  public LoginPage(WebDriver driver) {
    super(driver);
  }

  public void navigateTo(String baseUrl) {
    driver.get(baseUrl + "/user/login");
    wait.until(ExpectedConditions.visibilityOf(pageTitle));
  }

  public String getPageTitle() {
    return getText(pageTitle);
  }

  public void enterEmail(String email) {
    type(emailInput, email);
  }

  public void enterPassword(String password) {
    type(passwordInput, password);
  }

  public HomePage login(String email, String password) {
    enterEmail(email);
    enterPassword(password);
    click(signInButton);
    return new HomePage(driver);
  }

  public void clickSignIn() {
    click(signInButton);
  }

  public boolean isErrorMessageDisplayed() {
    try {
      wait.until(ExpectedConditions.visibilityOf(errorMessages));
      return errorMessages.isDisplayed();
    } catch (Exception e) {
      return false;
    }
  }

  public String getErrorMessageText() {
    try {
      wait.until(ExpectedConditions.visibilityOf(errorMessages));
      return errorMessages.getText();
    } catch (Exception e) {
      return "";
    }
  }

  public RegisterPage clickNeedAccountLink() {
    click(needAccountLink);
    return new RegisterPage(driver);
  }

  public boolean isPageLoaded() {
    return isDisplayed(pageTitle)
        && isDisplayed(emailInput)
        && isDisplayed(passwordInput)
        && isDisplayed(signInButton);
  }
}
