export interface ArticleList {
  articles: ArticleType[];
}

export interface Article {
  article: ArticleType;
}

export type ArticleType = {
  tagList: string[];
  createdAt: string;
  author: Author;
  description: string;
  title: string;
  body: string;
  slug: string;
  updatedAt: string;
  favoritesCount: number;
  favorited: boolean;
};

export type Author = {
  username: string;
  bio: string;
  image: string;
  following: boolean;
};
