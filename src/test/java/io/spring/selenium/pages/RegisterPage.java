package io.spring.selenium.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** Page object for the Register page. */
public class RegisterPage extends BasePage {

  @FindBy(css = "input[type='text'][placeholder='Username']")
  private WebElement usernameInput;

  @FindBy(css = "input[type='email']")
  private WebElement emailInput;

  @FindBy(css = "input[type='password']")
  private WebElement passwordInput;

  @FindBy(css = "button[type='submit']")
  private WebElement signUpButton;

  @FindBy(css = "h1.text-xs-center")
  private WebElement pageTitle;

  @FindBy(linkText = "Have an account?")
  private WebElement haveAccountLink;

  @FindBy(css = ".error-messages")
  private WebElement errorMessages;

  public RegisterPage(WebDriver driver) {
    super(driver);
  }

  public void navigateTo(String baseUrl) {
    driver.get(baseUrl + "/user/register");
    wait.until(ExpectedConditions.visibilityOf(pageTitle));
  }

  public String getPageTitle() {
    return getText(pageTitle);
  }

  public void enterUsername(String username) {
    type(usernameInput, username);
  }

  public void enterEmail(String email) {
    type(emailInput, email);
  }

  public void enterPassword(String password) {
    type(passwordInput, password);
  }

  public HomePage register(String username, String email, String password) {
    enterUsername(username);
    enterEmail(email);
    enterPassword(password);
    click(signUpButton);
    return new HomePage(driver);
  }

  public void clickSignUp() {
    click(signUpButton);
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

  public LoginPage clickHaveAccountLink() {
    click(haveAccountLink);
    return new LoginPage(driver);
  }

  public boolean isPageLoaded() {
    return isDisplayed(pageTitle)
        && isDisplayed(usernameInput)
        && isDisplayed(emailInput)
        && isDisplayed(passwordInput)
        && isDisplayed(signUpButton);
  }
}
