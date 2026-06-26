package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.CursorPageParameter;
import io.spring.application.Page;
import io.spring.application.data.ArticleBookmarkDate;
import io.spring.core.user.User;
import java.util.List;
import java.util.Set;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArticleBookmarksReadService {
  boolean isUserBookmark(@Param("userId") String userId, @Param("articleId") String articleId);

  Set<String> userBookmarks(@Param("ids") List<String> ids, @Param("currentUser") User currentUser);

  List<String> findUserBookmarkedArticleIdsWithCursor(
      @Param("userId") String userId, @Param("page") CursorPageParameter page);

  List<String> findUserBookmarkedArticleIds(
      @Param("userId") String userId, @Param("page") Page page);

  int countUserBookmarks(@Param("userId") String userId);

  List<ArticleBookmarkDate> findBookmarkDates(
      @Param("userId") String userId, @Param("ids") List<String> ids);
}
