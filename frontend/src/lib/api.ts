import axios, { AxiosError, AxiosInstance, InternalAxiosRequestConfig } from 'axios';
import {
  User,
  Article,
  Comment,
  Profile,
  ArticlesResponse,
  TagsResponse,
  UserResponse,
  ProfileResponse,
  ArticleResponse,
  CommentsResponse,
  CommentResponse,
  LoginInput,
  RegisterInput,
  UpdateUserInput,
  NewArticleInput,
  UpdateArticleInput,
  NewCommentInput,
} from '@/types';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

const api: AxiosInstance = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  if (typeof window !== 'undefined') {
    const token = localStorage.getItem('token');
    if (token && config.headers) {
      config.headers.Authorization = `Token ${token}`;
    }
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      if (typeof window !== 'undefined') {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
      }
    }
    return Promise.reject(error);
  }
);

export const AuthAPI = {
  login: async (credentials: LoginInput): Promise<User> => {
    const response = await api.post<UserResponse>('/users/login', {
      user: credentials,
    });
    return response.data.user;
  },

  register: async (userData: RegisterInput): Promise<User> => {
    const response = await api.post<UserResponse>('/users', {
      user: userData,
    });
    return response.data.user;
  },

  getCurrentUser: async (): Promise<User> => {
    const response = await api.get<UserResponse>('/user');
    return response.data.user;
  },

  updateUser: async (userData: UpdateUserInput): Promise<User> => {
    const response = await api.put<UserResponse>('/user', {
      user: userData,
    });
    return response.data.user;
  },
};

export const ProfileAPI = {
  get: async (username: string): Promise<Profile> => {
    const response = await api.get<ProfileResponse>(`/profiles/${username}`);
    return response.data.profile;
  },

  follow: async (username: string): Promise<Profile> => {
    const response = await api.post<ProfileResponse>(
      `/profiles/${username}/follow`
    );
    return response.data.profile;
  },

  unfollow: async (username: string): Promise<Profile> => {
    const response = await api.delete<ProfileResponse>(
      `/profiles/${username}/follow`
    );
    return response.data.profile;
  },
};

export const ArticleAPI = {
  getAll: async (params?: {
    tag?: string;
    author?: string;
    favorited?: string;
    limit?: number;
    offset?: number;
  }): Promise<ArticlesResponse> => {
    const response = await api.get<ArticlesResponse>('/articles', { params });
    return response.data;
  },

  getFeed: async (params?: {
    limit?: number;
    offset?: number;
  }): Promise<ArticlesResponse> => {
    const response = await api.get<ArticlesResponse>('/articles/feed', {
      params,
    });
    return response.data;
  },

  get: async (slug: string): Promise<Article> => {
    const response = await api.get<ArticleResponse>(`/articles/${slug}`);
    return response.data.article;
  },

  create: async (articleData: NewArticleInput): Promise<Article> => {
    const response = await api.post<ArticleResponse>('/articles', {
      article: articleData,
    });
    return response.data.article;
  },

  update: async (
    slug: string,
    articleData: UpdateArticleInput
  ): Promise<Article> => {
    const response = await api.put<ArticleResponse>(`/articles/${slug}`, {
      article: articleData,
    });
    return response.data.article;
  },

  delete: async (slug: string): Promise<void> => {
    await api.delete(`/articles/${slug}`);
  },

  favorite: async (slug: string): Promise<Article> => {
    const response = await api.post<ArticleResponse>(
      `/articles/${slug}/favorite`
    );
    return response.data.article;
  },

  unfavorite: async (slug: string): Promise<Article> => {
    const response = await api.delete<ArticleResponse>(
      `/articles/${slug}/favorite`
    );
    return response.data.article;
  },
};

export const CommentAPI = {
  getAll: async (slug: string): Promise<Comment[]> => {
    const response = await api.get<CommentsResponse>(
      `/articles/${slug}/comments`
    );
    return response.data.comments;
  },

  create: async (slug: string, commentData: NewCommentInput): Promise<Comment> => {
    const response = await api.post<CommentResponse>(
      `/articles/${slug}/comments`,
      { comment: commentData }
    );
    return response.data.comment;
  },

  delete: async (slug: string, commentId: string): Promise<void> => {
    await api.delete(`/articles/${slug}/comments/${commentId}`);
  },
};

export const TagAPI = {
  getAll: async (): Promise<string[]> => {
    const response = await api.get<TagsResponse>('/tags');
    return response.data.tags;
  },
};

export default api;
