'use client';

import { useEffect, useState, useCallback } from 'react';
import { useParams } from 'next/navigation';
import Link from 'next/link';
import { Settings, UserPlus, UserMinus } from 'lucide-react';
import { Article, Profile } from '@/types';
import { ArticleAPI, ProfileAPI } from '@/lib/api';
import { useAuthStore } from '@/lib/store';
import ArticlePreview from '@/components/ArticlePreview';
import Pagination from '@/components/Pagination';
import LoadingSpinner from '@/components/LoadingSpinner';

const ARTICLES_PER_PAGE = 5;

export default function ProfilePage() {
  const params = useParams();
  const username = params.username as string;
  const { user, isAuthenticated } = useAuthStore();
  const [profile, setProfile] = useState<Profile | null>(null);
  const [articles, setArticles] = useState<Article[]>([]);
  const [articlesCount, setArticlesCount] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [activeTab, setActiveTab] = useState<'author' | 'favorited'>('author');
  const [isLoading, setIsLoading] = useState(true);
  const [isArticlesLoading, setIsArticlesLoading] = useState(true);
  const [isFollowing, setIsFollowing] = useState(false);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const fetchedProfile = await ProfileAPI.get(username);
        setProfile(fetchedProfile);
      } catch (err) {
        console.error('Failed to fetch profile:', err);
      } finally {
        setIsLoading(false);
      }
    };

    fetchProfile();
  }, [username]);

  const fetchArticles = useCallback(async () => {
    setIsArticlesLoading(true);
    try {
      const offset = (currentPage - 1) * ARTICLES_PER_PAGE;
      const params =
        activeTab === 'author'
          ? { author: username, limit: ARTICLES_PER_PAGE, offset }
          : { favorited: username, limit: ARTICLES_PER_PAGE, offset };

      const response = await ArticleAPI.getAll(params);
      setArticles(response.articles);
      setArticlesCount(response.articlesCount);
    } catch (err) {
      console.error('Failed to fetch articles:', err);
    } finally {
      setIsArticlesLoading(false);
    }
  }, [username, activeTab, currentPage]);

  useEffect(() => {
    fetchArticles();
  }, [fetchArticles]);

  const handleFollow = async () => {
    if (!isAuthenticated || !profile || isFollowing) return;

    setIsFollowing(true);
    try {
      const updatedProfile = profile.following
        ? await ProfileAPI.unfollow(username)
        : await ProfileAPI.follow(username);
      setProfile(updatedProfile);
    } catch (err) {
      console.error('Failed to toggle follow:', err);
    } finally {
      setIsFollowing(false);
    }
  };

  const handleTabChange = (tab: 'author' | 'favorited') => {
    setActiveTab(tab);
    setCurrentPage(1);
  };

  const handleArticleUpdate = (updatedArticle: Article) => {
    setArticles(
      articles.map((a) => (a.slug === updatedArticle.slug ? updatedArticle : a))
    );
  };

  const totalPages = Math.ceil(articlesCount / ARTICLES_PER_PAGE);
  const isOwnProfile = user?.username === username;

  if (isLoading) {
    return <LoadingSpinner className="py-12" />;
  }

  if (!profile) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-12 text-center">
        <p className="text-gray-500">User not found</p>
      </div>
    );
  }

  return (
    <div>
      <div className="bg-gray-100 py-8">
        <div className="max-w-4xl mx-auto px-4 text-center">
          {profile.image ? (
            <img
              src={profile.image}
              alt={profile.username}
              className="w-24 h-24 rounded-full object-cover mx-auto mb-4"
            />
          ) : (
            <div className="w-24 h-24 rounded-full bg-emerald-600 flex items-center justify-center mx-auto mb-4">
              <span className="text-white text-3xl font-medium">
                {profile.username.charAt(0).toUpperCase()}
              </span>
            </div>
          )}

          <h1 className="text-2xl font-bold text-gray-900 mb-2">
            {profile.username}
          </h1>

          {profile.bio && (
            <p className="text-gray-500 mb-4 max-w-md mx-auto">{profile.bio}</p>
          )}

          {isOwnProfile ? (
            <Link
              href="/settings"
              className="inline-flex items-center gap-2 px-4 py-2 border border-gray-300 text-gray-600 rounded hover:bg-gray-200 transition-colors"
            >
              <Settings className="w-4 h-4" />
              <span>Edit Profile Settings</span>
            </Link>
          ) : (
            isAuthenticated && (
              <button
                onClick={handleFollow}
                disabled={isFollowing}
                className={`inline-flex items-center gap-2 px-4 py-2 border rounded transition-colors disabled:opacity-50 ${
                  profile.following
                    ? 'border-gray-300 text-gray-600 hover:bg-gray-200'
                    : 'border-gray-300 text-gray-600 hover:bg-gray-200'
                }`}
              >
                {profile.following ? (
                  <>
                    <UserMinus className="w-4 h-4" />
                    <span>Unfollow {profile.username}</span>
                  </>
                ) : (
                  <>
                    <UserPlus className="w-4 h-4" />
                    <span>Follow {profile.username}</span>
                  </>
                )}
              </button>
            )
          )}
        </div>
      </div>

      <div className="max-w-4xl mx-auto px-4 py-8">
        <div className="border-b border-gray-200 mb-6">
          <div className="flex gap-4">
            <button
              onClick={() => handleTabChange('author')}
              className={`pb-3 px-1 border-b-2 transition-colors ${
                activeTab === 'author'
                  ? 'border-emerald-600 text-emerald-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700'
              }`}
            >
              My Articles
            </button>
            <button
              onClick={() => handleTabChange('favorited')}
              className={`pb-3 px-1 border-b-2 transition-colors ${
                activeTab === 'favorited'
                  ? 'border-emerald-600 text-emerald-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700'
              }`}
            >
              Favorited Articles
            </button>
          </div>
        </div>

        {isArticlesLoading ? (
          <LoadingSpinner className="py-12" />
        ) : articles.length > 0 ? (
          <>
            {articles.map((article) => (
              <ArticlePreview
                key={article.slug}
                article={article}
                onUpdate={handleArticleUpdate}
              />
            ))}
            <Pagination
              currentPage={currentPage}
              totalPages={totalPages}
              onPageChange={setCurrentPage}
            />
          </>
        ) : (
          <p className="text-gray-400 text-center py-12">
            No articles are here... yet.
          </p>
        )}
      </div>
    </div>
  );
}
