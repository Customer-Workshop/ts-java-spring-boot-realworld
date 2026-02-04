'use client';

import Link from 'next/link';
import { Heart } from 'lucide-react';
import { Article } from '@/types';
import { formatDate } from '@/lib/utils';
import { ArticleAPI } from '@/lib/api';
import { useAuthStore } from '@/lib/store';
import { useState } from 'react';

interface ArticlePreviewProps {
  article: Article;
  onUpdate?: (article: Article) => void;
}

export default function ArticlePreview({ article, onUpdate }: ArticlePreviewProps) {
  const { isAuthenticated } = useAuthStore();
  const [isLoading, setIsLoading] = useState(false);

  const handleFavorite = async (e: React.MouseEvent) => {
    e.preventDefault();
    if (!isAuthenticated || isLoading) return;

    setIsLoading(true);
    try {
      const updatedArticle = article.favorited
        ? await ArticleAPI.unfavorite(article.slug)
        : await ArticleAPI.favorite(article.slug);
      onUpdate?.(updatedArticle);
    } catch (error) {
      console.error('Failed to toggle favorite:', error);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <article className="py-6 border-b border-gray-100 last:border-b-0">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-3">
          <Link href={`/profile/${article.author.username}`}>
            {article.author.image ? (
              <img
                src={article.author.image}
                alt={article.author.username}
                className="w-10 h-10 rounded-full object-cover"
              />
            ) : (
              <div className="w-10 h-10 rounded-full bg-emerald-100 flex items-center justify-center">
                <span className="text-emerald-600 font-medium">
                  {article.author.username.charAt(0).toUpperCase()}
                </span>
              </div>
            )}
          </Link>
          <div>
            <Link
              href={`/profile/${article.author.username}`}
              className="text-emerald-600 hover:text-emerald-700 font-medium"
            >
              {article.author.username}
            </Link>
            <p className="text-gray-400 text-sm">{formatDate(article.createdAt)}</p>
          </div>
        </div>
        <button
          onClick={handleFavorite}
          disabled={!isAuthenticated || isLoading}
          className={`flex items-center gap-1 px-3 py-1 rounded-md border transition-colors ${
            article.favorited
              ? 'bg-emerald-600 text-white border-emerald-600'
              : 'text-emerald-600 border-emerald-600 hover:bg-emerald-600 hover:text-white'
          } ${!isAuthenticated ? 'opacity-50 cursor-not-allowed' : ''}`}
        >
          <Heart className={`w-4 h-4 ${article.favorited ? 'fill-current' : ''}`} />
          <span>{article.favoritesCount}</span>
        </button>
      </div>

      <Link href={`/article/${article.slug}`} className="block group">
        <h2 className="text-xl font-semibold text-gray-900 group-hover:text-emerald-600 transition-colors mb-2">
          {article.title}
        </h2>
        <p className="text-gray-500 mb-4 line-clamp-2">{article.description}</p>
        <div className="flex items-center justify-between">
          <span className="text-emerald-600 text-sm">Read more...</span>
          {article.tagList.length > 0 && (
            <div className="flex flex-wrap gap-1">
              {article.tagList.map((tag) => (
                <span
                  key={tag}
                  className="px-2 py-1 text-xs text-gray-400 border border-gray-200 rounded-full"
                >
                  {tag}
                </span>
              ))}
            </div>
          )}
        </div>
      </Link>
    </article>
  );
}
