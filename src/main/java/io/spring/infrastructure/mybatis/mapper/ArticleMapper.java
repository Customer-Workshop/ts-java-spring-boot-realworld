package io.spring.infrastructure.mybatis.mapper;

import io.spring.core.article.Article;
import io.spring.core.article.Tag;

public interface ArticleMapper {
  void insert(Article article);

  Article findById(String id);

  Tag findTag(String tagName);

  void insertTag(Tag tag);

  void insertArticleTagRelation(String articleId, String tagId);

  Article findBySlug(String slug);

  void update(Article article);

  void delete(String id);
}
