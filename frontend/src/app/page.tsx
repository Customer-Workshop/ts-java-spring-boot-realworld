'use client';

import { useState, useEffect, useCallback } from 'react';
import { Article } from '@/types';
import { ArticleAPI } from '@/lib/api';
import { useAuthStore } from '@/lib/store';
import ArticlePreview from '@/components/ArticlePreview';
import TagList from '@/components/TagList';
import Pagination from '@/components/Pagination';
import LoadingSpinner from '@/components/LoadingSpinner';

const ARTICLES_PER_PAGE = 10;

export default function HomePage() {
  const { isAuthenticated, isLoading: authLoading } = useAuthStore();
  const [articles, setArticles] = useState<Article[]>([]);
  const [articlesCount, setArticlesCount] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [selectedTag, setSelectedTag] = useState<string | undefined>();
  const [activeTab, setActiveTab] = useState<'global' | 'feed'>('global');
  const [isLoading, setIsLoading] = useState(true);

  const fetchArticles = useCallback(async () => {
    setIsLoading(true);
    try {
      const offset = (currentPage - 1) * ARTICLES_PER_PAGE;
      let response;

      if (activeTab === 'feed' && isAuthenticated) {
        response = await ArticleAPI.getFeed({
          limit: ARTICLES_PER_PAGE,
          offset,
        });
      } else {
        response = await ArticleAPI.getAll({
          tag: selectedTag,
          limit: ARTICLES_PER_PAGE,
          offset,
        });
      }

      setArticles(response.articles);
      setArticlesCount(response.articlesCount);
    } catch (error) {
      console.error('Failed to fetch articles:', error);
    } finally {
      setIsLoading(false);
    }
  }, [currentPage, selectedTag, activeTab, isAuthenticated]);

  useEffect(() => {
    if (!authLoading) {
      fetchArticles();
    }
  }, [fetchArticles, authLoading]);

  const handleTagSelect = (tag: string | undefined) => {
    setSelectedTag(tag);
    setCurrentPage(1);
    setActiveTab('global');
  };

  const handleTabChange = (tab: 'global' | 'feed') => {
    setActiveTab(tab);
    setSelectedTag(undefined);
    setCurrentPage(1);
  };

  const handleArticleUpdate = (updatedArticle: Article) => {
    setArticles(
      articles.map((a) => (a.slug === updatedArticle.slug ? updatedArticle : a))
    );
  };

  const totalPages = Math.ceil(articlesCount / ARTICLES_PER_PAGE);

  return (
    <div>
      <div className="bg-emerald-600 text-white py-12 mb-8">
        <div className="max-w-6xl mx-auto px-4 text-center">
          <h1 className="text-5xl font-bold mb-4">conduit</h1>
          <p className="text-xl opacity-90">A place to share your knowledge.</p>
        </div>
      </div>

      <div className="max-w-6xl mx-auto px-4 pb-12">
        <div className="flex flex-col lg:flex-row gap-8">
          <div className="flex-1">
            <div className="border-b border-gray-200 mb-6">
              <div className="flex gap-4">
                {isAuthenticated && (
                  <button
                    onClick={() => handleTabChange('feed')}
                    className={`pb-3 px-1 border-b-2 transition-colors ${
                      activeTab === 'feed'
                        ? 'border-emerald-600 text-emerald-600'
                        : 'border-transparent text-gray-500 hover:text-gray-700'
                    }`}
                  >
                    Your Feed
                  </button>
                )}
                <button
                  onClick={() => handleTabChange('global')}
                  className={`pb-3 px-1 border-b-2 transition-colors ${
                    activeTab === 'global' && !selectedTag
                      ? 'border-emerald-600 text-emerald-600'
                      : 'border-transparent text-gray-500 hover:text-gray-700'
                  }`}
                >
                  Global Feed
                </button>
                {selectedTag && (
                  <span className="pb-3 px-1 border-b-2 border-emerald-600 text-emerald-600">
                    #{selectedTag}
                  </span>
                )}
              </div>
            </div>

            {isLoading ? (
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

          <div className="lg:w-72">
            <TagList selectedTag={selectedTag} onTagSelect={handleTagSelect} />
          </div>
        </div>
      </div>
    </div>
  );
}
