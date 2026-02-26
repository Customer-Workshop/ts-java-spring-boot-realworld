package io.spring.selenium.pages;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** Page object for the Article detail page. */
public class ArticlePage extends BasePage {

  @FindBy(css = ".article-page h1")
  private WebElement articleTitle;

  @FindBy(css = ".article-content p")
  private WebElement articleBody;

  @FindBy(css = ".article-meta .author")
  private WebElement authorLink;

  @FindBy(css = ".article-meta .date")
  private WebElement articleDate;

  @FindBy(css = ".tag-list .tag-pill")
  private List<WebElement> articleTags;

  @FindBy(css = "textarea[placeholder='Write a comment...']")
  private WebElement commentTextarea;

  @FindBy(css = "form.card button[type='submit']")
  private WebElement postCommentButton;

  @FindBy(css = ".card .card-block p")
  private List<WebElement> commentBodies;

  @FindBy(css = "button.btn-primary")
  private WebElement favoriteButton;

  @FindBy(css = "button.btn-outline-primary")
  private WebElement unfavoritedButton;

  @FindBy(css = ".article-actions button.btn-outline-secondary")
  private WebElement followButton;

  @FindBy(css = "button.btn-outline-danger")
  private WebElement deleteArticleButton;

  @FindBy(css = "a[href*='/editor/']")
  private WebElement editArticleLink;

  public ArticlePage(WebDriver driver) {
    super(driver);
  }

  public String getArticleTitle() {
    return getText(articleTitle);
  }

  public String getArticleBody() {
    return getText(articleBody);
  }

  public String getAuthorName() {
    return getText(authorLink);
  }

  public int getTagCount() {
    return articleTags.size();
  }

  public List<WebElement> getTags() {
    return articleTags;
  }

  public void writeComment(String commentText) {
    type(commentTextarea, commentText);
  }

  public void postComment() {
    click(postCommentButton);
  }

  public void addComment(String commentText) {
    writeComment(commentText);
    postComment();
  }

  public int getCommentCount() {
    return commentBodies.size();
  }

  public String getCommentText(int index) {
    if (index < commentBodies.size()) {
      return commentBodies.get(index).getText();
    }
    throw new IndexOutOfBoundsException("Comment index out of bounds: " + index);
  }

  public boolean isCommentFormDisplayed() {
    return isDisplayed(commentTextarea) && isDisplayed(postCommentButton);
  }

  public void clickFavorite() {
    try {
      if (isDisplayed(unfavoritedButton)) {
        click(unfavoritedButton);
      } else if (isDisplayed(favoriteButton)) {
        click(favoriteButton);
      }
    } catch (Exception e) {
      // Try clicking any favorite-related button
      List<WebElement> favButtons = driver.findElements(By.cssSelector("button.btn-primary"));
      if (!favButtons.isEmpty()) {
        favButtons.get(0).click();
      }
    }
  }

  public boolean isDeleteButtonDisplayed() {
    return isDisplayed(deleteArticleButton);
  }

  public boolean isEditLinkDisplayed() {
    return isDisplayed(editArticleLink);
  }

  public void clickDelete() {
    click(deleteArticleButton);
  }

  public EditorPage clickEdit() {
    click(editArticleLink);
    return new EditorPage(driver);
  }

  public boolean isPageLoaded() {
    try {
      wait.until(ExpectedConditions.visibilityOf(articleTitle));
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /** Delete a comment by its index. */
  public void deleteComment(int index) {
    List<WebElement> deleteButtons = driver.findElements(By.cssSelector(".card .ion-trash-a"));
    if (index < deleteButtons.size()) {
      deleteButtons.get(index).click();
    }
  }
}
