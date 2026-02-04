'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { Trash2, Send } from 'lucide-react';
import { Comment } from '@/types';
import { CommentAPI } from '@/lib/api';
import { useAuthStore } from '@/lib/store';
import { formatDate, getErrorMessage } from '@/lib/utils';
import LoadingSpinner from './LoadingSpinner';

interface CommentSectionProps {
  slug: string;
}

export default function CommentSection({ slug }: CommentSectionProps) {
  const { user, isAuthenticated } = useAuthStore();
  const [comments, setComments] = useState<Comment[]>([]);
  const [newComment, setNewComment] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchComments = async () => {
      try {
        const fetchedComments = await CommentAPI.getAll(slug);
        setComments(fetchedComments);
      } catch (err) {
        console.error('Failed to fetch comments:', err);
      } finally {
        setIsLoading(false);
      }
    };

    fetchComments();
  }, [slug]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newComment.trim() || isSubmitting) return;

    setIsSubmitting(true);
    setError(null);

    try {
      const comment = await CommentAPI.create(slug, { body: newComment });
      setComments([comment, ...comments]);
      setNewComment('');
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async (commentId: string) => {
    try {
      await CommentAPI.delete(slug, commentId);
      setComments(comments.filter((c) => c.id !== commentId));
    } catch (err) {
      console.error('Failed to delete comment:', err);
    }
  };

  if (isLoading) {
    return <LoadingSpinner className="py-8" />;
  }

  return (
    <div className="max-w-3xl mx-auto">
      {isAuthenticated ? (
        <form onSubmit={handleSubmit} className="mb-8">
          <div className="border border-gray-200 rounded-lg overflow-hidden">
            <textarea
              value={newComment}
              onChange={(e) => setNewComment(e.target.value)}
              placeholder="Write a comment..."
              rows={3}
              className="w-full p-4 resize-none focus:outline-none"
            />
            <div className="flex items-center justify-between bg-gray-50 px-4 py-3">
              <div className="flex items-center gap-2">
                {user?.image ? (
                  <img
                    src={user.image}
                    alt={user.username}
                    className="w-8 h-8 rounded-full object-cover"
                  />
                ) : (
                  <div className="w-8 h-8 rounded-full bg-emerald-100 flex items-center justify-center">
                    <span className="text-emerald-600 font-medium text-sm">
                      {user?.username.charAt(0).toUpperCase()}
                    </span>
                  </div>
                )}
                <span className="text-gray-600 text-sm">{user?.username}</span>
              </div>
              <button
                type="submit"
                disabled={isSubmitting || !newComment.trim()}
                className="flex items-center gap-2 px-4 py-2 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
              >
                <Send className="w-4 h-4" />
                <span>Post Comment</span>
              </button>
            </div>
          </div>
          {error && <p className="mt-2 text-red-500 text-sm">{error}</p>}
        </form>
      ) : (
        <div className="mb-8 p-4 bg-gray-50 rounded-lg text-center">
          <p className="text-gray-600">
            <Link href="/login" className="text-emerald-600 hover:underline">
              Sign in
            </Link>{' '}
            or{' '}
            <Link href="/register" className="text-emerald-600 hover:underline">
              sign up
            </Link>{' '}
            to add comments on this article.
          </p>
        </div>
      )}

      <div className="space-y-4">
        {comments.map((comment) => (
          <div
            key={comment.id}
            className="border border-gray-200 rounded-lg overflow-hidden"
          >
            <div className="p-4">
              <p className="text-gray-700 whitespace-pre-wrap">{comment.body}</p>
            </div>
            <div className="flex items-center justify-between bg-gray-50 px-4 py-3">
              <div className="flex items-center gap-2">
                <Link href={`/profile/${comment.author.username}`}>
                  {comment.author.image ? (
                    <img
                      src={comment.author.image}
                      alt={comment.author.username}
                      className="w-6 h-6 rounded-full object-cover"
                    />
                  ) : (
                    <div className="w-6 h-6 rounded-full bg-emerald-100 flex items-center justify-center">
                      <span className="text-emerald-600 font-medium text-xs">
                        {comment.author.username.charAt(0).toUpperCase()}
                      </span>
                    </div>
                  )}
                </Link>
                <Link
                  href={`/profile/${comment.author.username}`}
                  className="text-emerald-600 hover:underline text-sm"
                >
                  {comment.author.username}
                </Link>
                <span className="text-gray-400 text-sm">
                  {formatDate(comment.createdAt)}
                </span>
              </div>
              {user?.username === comment.author.username && (
                <button
                  onClick={() => handleDelete(comment.id)}
                  className="p-1 text-gray-400 hover:text-red-500 transition-colors"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              )}
            </div>
          </div>
        ))}
        {comments.length === 0 && (
          <p className="text-center text-gray-400 py-8">No comments yet.</p>
        )}
      </div>
    </div>
  );
}
