package io.spring.selenium.pages;

import java.util.List;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** Page object for the Article Editor page. */
public class EditorPage extends BasePage {

  @FindBy(css = "input[placeholder='Article Title']")
  private WebElement titleInput;

  @FindBy(css = "input[placeholder=\"What's this article about?\"]")
  private WebElement descriptionInput;

  @FindBy(css = "textarea[placeholder='Write your article (in markdown)']")
  private WebElement bodyTextarea;

  @FindBy(css = "input[placeholder='Enter tags']")
  private WebElement tagInput;

  @FindBy(css = "button.btn-primary")
  private WebElement publishButton;

  @FindBy(css = ".tag-list .tag-pill")
  private List<WebElement> tagPills;

  @FindBy(css = ".error-messages")
  private WebElement errorMessages;

  public EditorPage(WebDriver driver) {
    super(driver);
  }

  public void navigateTo(String baseUrl) {
    driver.get(baseUrl + "/editor/new");
    wait.until(ExpectedConditions.visibilityOf(titleInput));
  }

  public void enterTitle(String title) {
    type(titleInput, title);
  }

  public void enterDescription(String description) {
    type(descriptionInput, description);
  }

  public void enterBody(String body) {
    type(bodyTextarea, body);
  }

  public void enterTag(String tag) {
    WebElement visibleTagInput = waitForVisibility(tagInput);
    visibleTagInput.sendKeys(tag);
    visibleTagInput.sendKeys(Keys.ENTER);
  }

  public void publishArticle(String title, String description, String body) {
    enterTitle(title);
    enterDescription(description);
    enterBody(body);
    clickPublish();
  }

  public void publishArticleWithTags(
      String title, String description, String body, String... tags) {
    enterTitle(title);
    enterDescription(description);
    enterBody(body);
    for (String tag : tags) {
      enterTag(tag);
    }
    clickPublish();
  }

  public void clickPublish() {
    click(publishButton);
  }

  public int getTagCount() {
    return tagPills.size();
  }

  public boolean isPageLoaded() {
    return isDisplayed(titleInput)
        && isDisplayed(descriptionInput)
        && isDisplayed(bodyTextarea)
        && isDisplayed(publishButton);
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
}
