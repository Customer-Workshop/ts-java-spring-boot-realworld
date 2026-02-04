'use client';

import { useEffect, useState } from 'react';
import { useRouter, useParams } from 'next/navigation';
import Link from 'next/link';
import { marked } from 'marked';
import { Heart, Edit, Trash2, UserPlus, UserMinus } from 'lucide-react';
import { Article } from '@/types';
import { ArticleAPI, ProfileAPI } from '@/lib/api';
import { useAuthStore } from '@/lib/store';
import { formatDate } from '@/lib/utils';
import CommentSection from '@/components/CommentSection';
import LoadingSpinner from '@/components/LoadingSpinner';

export default function ArticlePage() {
  const router = useRouter();
  const params = useParams();
  const slug = params.slug as string;
  const { user, isAuthenticated } = useAuthStore();
  const [article, setArticle] = useState<Article | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isDeleting, setIsDeleting] = useState(false);
  const [isFollowing, setIsFollowing] = useState(false);
  const [isFavoriting, setIsFavoriting] = useState(false);

  useEffect(() => {
    const fetchArticle = async () => {
      try {
        const fetchedArticle = await ArticleAPI.get(slug);
        setArticle(fetchedArticle);
      } catch (err) {
        console.error('Failed to fetch article:', err);
      } finally {
        setIsLoading(false);
      }
    };

    fetchArticle();
  }, [slug]);

  const handleDelete = async () => {
    if (!confirm('Are you sure you want to delete this article?')) return;

    setIsDeleting(true);
    try {
      await ArticleAPI.delete(slug);
      router.push('/');
    } catch (err) {
      console.error('Failed to delete article:', err);
    } finally {
      setIsDeleting(false);
    }
  };

  const handleFollow = async () => {
    if (!isAuthenticated || !article || isFollowing) return;

    setIsFollowing(true);
    try {
      const profile = article.author.following
        ? await ProfileAPI.unfollow(article.author.username)
        : await ProfileAPI.follow(article.author.username);
      setArticle({
        ...article,
        author: { ...article.author, following: profile.following },
      });
    } catch (err) {
      console.error('Failed to toggle follow:', err);
    } finally {
      setIsFollowing(false);
    }
  };

  const handleFavorite = async () => {
    if (!isAuthenticated || !article || isFavoriting) return;

    setIsFavoriting(true);
    try {
      const updatedArticle = article.favorited
        ? await ArticleAPI.unfavorite(slug)
        : await ArticleAPI.favorite(slug);
      setArticle(updatedArticle);
    } catch (err) {
      console.error('Failed to toggle favorite:', err);
    } finally {
      setIsFavoriting(false);
    }
  };

  if (isLoading) {
    return <LoadingSpinner className="py-12" />;
  }

  if (!article) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-12 text-center">
        <p className="text-gray-500">Article not found</p>
      </div>
    );
  }

  const isAuthor = user?.username === article.author.username;
  const htmlContent = marked(article.body);

  return (
    <div>
      <div className="bg-gray-900 text-white py-8">
        <div className="max-w-4xl mx-auto px-4">
          <h1 className="text-4xl font-bold mb-6">{article.title}</h1>

          <div className="flex flex-wrap items-center gap-4">
            <div className="flex items-center gap-3">
              <Link href={`/profile/${article.author.username}`}>
                {article.author.image ? (
                  <img
                    src={article.author.image}
                    alt={article.author.username}
                    className="w-10 h-10 rounded-full object-cover"
                  />
                ) : (
                  <div className="w-10 h-10 rounded-full bg-emerald-600 flex items-center justify-center">
                    <span className="text-white font-medium">
                      {article.author.username.charAt(0).toUpperCase()}
                    </span>
                  </div>
                )}
              </Link>
              <div>
                <Link
                  href={`/profile/${article.author.username}`}
                  className="text-emerald-400 hover:text-emerald-300"
                >
                  {article.author.username}
                </Link>
                <p className="text-gray-400 text-sm">
                  {formatDate(article.createdAt)}
                </p>
              </div>
            </div>

            {isAuthor ? (
              <div className="flex gap-2">
                <Link
                  href={`/editor/${article.slug}`}
                  className="flex items-center gap-1 px-3 py-1 border border-gray-400 text-gray-300 rounded hover:bg-gray-800 transition-colors"
                >
                  <Edit className="w-4 h-4" />
                  <span>Edit Article</span>
                </Link>
                <button
                  onClick={handleDelete}
                  disabled={isDeleting}
                  className="flex items-center gap-1 px-3 py-1 border border-red-500 text-red-500 rounded hover:bg-red-500 hover:text-white transition-colors disabled:opacity-50"
                >
                  <Trash2 className="w-4 h-4" />
                  <span>{isDeleting ? 'Deleting...' : 'Delete Article'}</span>
                </button>
              </div>
            ) : (
              <div className="flex gap-2">
                {isAuthenticated && (
                  <button
                    onClick={handleFollow}
                    disabled={isFollowing}
                    className={`flex items-center gap-1 px-3 py-1 border rounded transition-colors disabled:opacity-50 ${
                      article.author.following
                        ? 'border-gray-400 text-gray-300 hover:bg-gray-800'
                        : 'border-gray-400 text-gray-300 hover:bg-gray-800'
                    }`}
                  >
                    {article.author.following ? (
                      <>
                        <UserMinus className="w-4 h-4" />
                        <span>Unfollow {article.author.username}</span>
                      </>
                    ) : (
                      <>
                        <UserPlus className="w-4 h-4" />
                        <span>Follow {article.author.username}</span>
                      </>
                    )}
                  </button>
                )}
                <button
                  onClick={handleFavorite}
                  disabled={!isAuthenticated || isFavoriting}
                  className={`flex items-center gap-1 px-3 py-1 border rounded transition-colors disabled:opacity-50 ${
                    article.favorited
                      ? 'bg-emerald-600 border-emerald-600 text-white'
                      : 'border-emerald-600 text-emerald-400 hover:bg-emerald-600 hover:text-white'
                  }`}
                >
                  <Heart
                    className={`w-4 h-4 ${article.favorited ? 'fill-current' : ''}`}
                  />
                  <span>
                    {article.favorited ? 'Unfavorite' : 'Favorite'} Article (
                    {article.favoritesCount})
                  </span>
                </button>
              </div>
            )}
          </div>
        </div>
      </div>

      <div className="max-w-4xl mx-auto px-4 py-8">
        <div
          className="prose max-w-none mb-8"
          dangerouslySetInnerHTML={{ __html: htmlContent }}
        />

        {article.tagList.length > 0 && (
          <div className="flex flex-wrap gap-2 mb-8 pb-8 border-b border-gray-200">
            {article.tagList.map((tag) => (
              <Link
                key={tag}
                href={`/?tag=${tag}`}
                className="px-3 py-1 text-sm text-gray-500 border border-gray-200 rounded-full hover:bg-gray-50"
              >
                {tag}
              </Link>
            ))}
          </div>
        )}

        <CommentSection slug={slug} />
      </div>
    </div>
  );
}
