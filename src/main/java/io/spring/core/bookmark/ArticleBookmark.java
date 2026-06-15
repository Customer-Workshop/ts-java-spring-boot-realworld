package io.spring.core.bookmark;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@NoArgsConstructor
@Getter
@EqualsAndHashCode(exclude = "createdAt")
public class ArticleBookmark {
  private String articleId;
  private String userId;
  private DateTime createdAt;

  public ArticleBookmark(String articleId, String userId) {
    this.articleId = articleId;
    this.userId = userId;
    this.createdAt = new DateTime();
  }
}
