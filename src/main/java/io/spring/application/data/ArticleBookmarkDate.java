package io.spring.application.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.joda.time.DateTime;

@Getter
@Setter
@NoArgsConstructor
public class ArticleBookmarkDate {
  private String articleId;
  private DateTime createdAt;
}
