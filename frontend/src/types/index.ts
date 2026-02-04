export interface User {
  id: string;
  email: string;
  username: string;
  bio: string | null;
  image: string | null;
  token?: string;
}

export interface Profile {
  username: string;
  bio: string | null;
  image: string | null;
  following: boolean;
}

export interface Article {
  id: string;
  slug: string;
  title: string;
  description: string;
  body: string;
  tagList: string[];
  createdAt: string;
  updatedAt: string;
  favorited: boolean;
  favoritesCount: number;
  author: Profile;
}

export interface Comment {
  id: string;
  createdAt: string;
  updatedAt: string;
  body: string;
  author: Profile;
}

export interface ArticlesResponse {
  articles: Article[];
  articlesCount: number;
}

export interface TagsResponse {
  tags: string[];
}

export interface UserResponse {
  user: User;
}

export interface ProfileResponse {
  profile: Profile;
}

export interface ArticleResponse {
  article: Article;
}

export interface CommentsResponse {
  comments: Comment[];
}

export interface CommentResponse {
  comment: Comment;
}

export interface ApiError {
  errors: {
    body: string[];
  };
}

export interface LoginInput {
  email: string;
  password: string;
}

export interface RegisterInput {
  username: string;
  email: string;
  password: string;
}

export interface UpdateUserInput {
  email?: string;
  username?: string;
  password?: string;
  bio?: string;
  image?: string;
}

export interface NewArticleInput {
  title: string;
  description: string;
  body: string;
  tagList?: string[];
}

export interface UpdateArticleInput {
  title?: string;
  description?: string;
  body?: string;
}

export interface NewCommentInput {
  body: string;
}
