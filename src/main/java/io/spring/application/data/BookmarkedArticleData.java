package io.spring.application.data;

import io.spring.application.DateTimeCursor;
import io.spring.application.Node;
import io.spring.application.PageCursor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.joda.time.DateTime;

/**
 * Read-model node for a current user's bookmarked article. Carries the bookmark's {@code
 * created_at} so cursor pagination is anchored on bookmark time (newest-bookmarked first) rather
 * than the article's own create/update time.
 */
@Getter
@AllArgsConstructor
public class BookmarkedArticleData implements Node {
  private ArticleData article;
  private DateTime bookmarkedAt;

  @Override
  public PageCursor getCursor() {
    return new DateTimeCursor(bookmarkedAt);
  }
}
