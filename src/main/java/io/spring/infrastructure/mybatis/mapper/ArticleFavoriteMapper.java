package io.spring.infrastructure.mybatis.mapper;

import io.spring.core.favorite.ArticleFavorite;

public interface ArticleFavoriteMapper {
  ArticleFavorite find(String articleId, String userId);

  void insert(ArticleFavorite articleFavorite);

  void delete(ArticleFavorite favorite);
}
