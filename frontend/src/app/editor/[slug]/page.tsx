'use client';

import { useEffect, useState } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { Article } from '@/types';
import { ArticleAPI } from '@/lib/api';
import { useAuthStore } from '@/lib/store';
import ArticleForm from '@/components/ArticleForm';
import LoadingSpinner from '@/components/LoadingSpinner';

export default function EditArticlePage() {
  const router = useRouter();
  const params = useParams();
  const slug = params.slug as string;
  const { user, isAuthenticated, isLoading: authLoading } = useAuthStore();
  const [article, setArticle] = useState<Article | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login');
      return;
    }

    const fetchArticle = async () => {
      try {
        const fetchedArticle = await ArticleAPI.get(slug);
        if (fetchedArticle.author.username !== user?.username) {
          router.push(`/article/${slug}`);
          return;
        }
        setArticle(fetchedArticle);
      } catch {
        setError('Article not found');
      } finally {
        setIsLoading(false);
      }
    };

    if (!authLoading && isAuthenticated) {
      fetchArticle();
    }
  }, [slug, authLoading, isAuthenticated, user, router]);

  if (authLoading || isLoading) {
    return <LoadingSpinner className="py-12" />;
  }

  if (error) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-12 text-center">
        <p className="text-red-500">{error}</p>
      </div>
    );
  }

  if (!article) {
    return null;
  }

  return (
    <div className="max-w-3xl mx-auto px-4 py-12">
      <h1 className="text-3xl font-bold text-gray-900 mb-8">Edit Article</h1>
      <ArticleForm article={article} />
    </div>
  );
}
