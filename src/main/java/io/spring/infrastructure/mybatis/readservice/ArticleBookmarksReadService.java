package io.spring.infrastructure.mybatis.readservice;

import io.spring.core.user.User;
import java.util.List;
import java.util.Set;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArticleBookmarksReadService {
  boolean isUserBookmark(@Param("userId") String userId, @Param("articleId") String articleId);

  Set<String> userBookmarks(@Param("ids") List<String> ids, @Param("currentUser") User currentUser);
}
