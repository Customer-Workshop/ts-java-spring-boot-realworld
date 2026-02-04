'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { X } from 'lucide-react';
import { Article, NewArticleInput, UpdateArticleInput } from '@/types';
import { ArticleAPI } from '@/lib/api';
import { getErrorMessage } from '@/lib/utils';
import LoadingSpinner from './LoadingSpinner';

interface ArticleFormProps {
  article?: Article;
}

export default function ArticleForm({ article }: ArticleFormProps) {
  const router = useRouter();
  const [title, setTitle] = useState(article?.title || '');
  const [description, setDescription] = useState(article?.description || '');
  const [body, setBody] = useState(article?.body || '');
  const [tagInput, setTagInput] = useState('');
  const [tagList, setTagList] = useState<string[]>(article?.tagList || []);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errors, setErrors] = useState<string[]>([]);

  const handleAddTag = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' && tagInput.trim()) {
      e.preventDefault();
      if (!tagList.includes(tagInput.trim())) {
        setTagList([...tagList, tagInput.trim()]);
      }
      setTagInput('');
    }
  };

  const handleRemoveTag = (tagToRemove: string) => {
    setTagList(tagList.filter((tag) => tag !== tagToRemove));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    setErrors([]);

    const validationErrors: string[] = [];
    if (!title.trim()) validationErrors.push('Title is required');
    if (!description.trim()) validationErrors.push('Description is required');
    if (!body.trim()) validationErrors.push('Body is required');

    if (validationErrors.length > 0) {
      setErrors(validationErrors);
      setIsSubmitting(false);
      return;
    }

    try {
      let savedArticle: Article;

      if (article) {
        const updateData: UpdateArticleInput = {
          title: title.trim(),
          description: description.trim(),
          body: body.trim(),
        };
        savedArticle = await ArticleAPI.update(article.slug, updateData);
      } else {
        const createData: NewArticleInput = {
          title: title.trim(),
          description: description.trim(),
          body: body.trim(),
          tagList,
        };
        savedArticle = await ArticleAPI.create(createData);
      }

      router.push(`/article/${savedArticle.slug}`);
    } catch (err) {
      setErrors([getErrorMessage(err)]);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      {errors.length > 0 && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <ul className="list-disc list-inside text-red-600 space-y-1">
            {errors.map((error, index) => (
              <li key={index}>{error}</li>
            ))}
          </ul>
        </div>
      )}

      <div>
        <input
          type="text"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="Article Title"
          className="w-full px-4 py-3 text-xl border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
        />
      </div>

      <div>
        <input
          type="text"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          placeholder="What's this article about?"
          className="w-full px-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
        />
      </div>

      <div>
        <textarea
          value={body}
          onChange={(e) => setBody(e.target.value)}
          placeholder="Write your article (in markdown)"
          rows={12}
          className="w-full px-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent resize-none"
        />
      </div>

      {!article && (
        <div>
          <input
            type="text"
            value={tagInput}
            onChange={(e) => setTagInput(e.target.value)}
            onKeyDown={handleAddTag}
            placeholder="Enter tags (press Enter to add)"
            className="w-full px-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
          />
          {tagList.length > 0 && (
            <div className="flex flex-wrap gap-2 mt-3">
              {tagList.map((tag) => (
                <span
                  key={tag}
                  className="flex items-center gap-1 px-3 py-1 bg-gray-100 text-gray-700 rounded-full"
                >
                  {tag}
                  <button
                    type="button"
                    onClick={() => handleRemoveTag(tag)}
                    className="text-gray-400 hover:text-gray-600"
                  >
                    <X className="w-4 h-4" />
                  </button>
                </span>
              ))}
            </div>
          )}
        </div>
      )}

      <div className="flex justify-end">
        <button
          type="submit"
          disabled={isSubmitting}
          className="flex items-center gap-2 px-6 py-3 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          {isSubmitting ? (
            <>
              <LoadingSpinner size="sm" />
              <span>Publishing...</span>
            </>
          ) : (
            <span>{article ? 'Update Article' : 'Publish Article'}</span>
          )}
        </button>
      </div>
    </form>
  );
}
