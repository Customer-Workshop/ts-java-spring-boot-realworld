package io.spring.cucumber.steps;

import io.spring.core.article.Article;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Shared state object for passing data between Cucumber step definitions. This is scoped per
 * scenario via Cucumber's dependency injection.
 */
@Component
@io.cucumber.spring.ScenarioScope
public class SharedState {

  private User currentUser;
  private String currentToken;
  private User otherUser;
  private String otherToken;
  private MvcResult lastResponse;
  private Article lastCreatedArticle;
  private Comment lastCreatedComment;

  public void reset() {
    currentUser = null;
    currentToken = null;
    otherUser = null;
    otherToken = null;
    lastResponse = null;
    lastCreatedArticle = null;
    lastCreatedComment = null;
  }

  public User getCurrentUser() {
    return currentUser;
  }

  public void setCurrentUser(User currentUser) {
    this.currentUser = currentUser;
  }

  public String getCurrentToken() {
    return currentToken;
  }

  public void setCurrentToken(String currentToken) {
    this.currentToken = currentToken;
  }

  public User getOtherUser() {
    return otherUser;
  }

  public void setOtherUser(User otherUser) {
    this.otherUser = otherUser;
  }

  public String getOtherToken() {
    return otherToken;
  }

  public void setOtherToken(String otherToken) {
    this.otherToken = otherToken;
  }

  public MvcResult getLastResponse() {
    return lastResponse;
  }

  public void setLastResponse(MvcResult lastResponse) {
    this.lastResponse = lastResponse;
  }

  public Article getLastCreatedArticle() {
    return lastCreatedArticle;
  }

  public void setLastCreatedArticle(Article lastCreatedArticle) {
    this.lastCreatedArticle = lastCreatedArticle;
  }

  public Comment getLastCreatedComment() {
    return lastCreatedComment;
  }

  public void setLastCreatedComment(Comment lastCreatedComment) {
    this.lastCreatedComment = lastCreatedComment;
  }
}
