'use client';

import { useEffect, useState } from 'react';
import { TagAPI } from '@/lib/api';
import { Tag } from 'lucide-react';

interface TagListProps {
  selectedTag?: string;
  onTagSelect: (tag: string | undefined) => void;
}

export default function TagList({ selectedTag, onTagSelect }: TagListProps) {
  const [tags, setTags] = useState<string[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchTags = async () => {
      try {
        const fetchedTags = await TagAPI.getAll();
        setTags(fetchedTags);
      } catch (error) {
        console.error('Failed to fetch tags:', error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchTags();
  }, []);

  if (isLoading) {
    return (
      <div className="bg-gray-50 rounded-lg p-4">
        <h3 className="text-gray-700 font-medium mb-3 flex items-center gap-2">
          <Tag className="w-4 h-4" />
          Popular Tags
        </h3>
        <div className="flex flex-wrap gap-2">
          {[1, 2, 3, 4, 5].map((i) => (
            <div
              key={i}
              className="h-6 w-16 bg-gray-200 rounded-full animate-pulse"
            />
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="bg-gray-50 rounded-lg p-4">
      <h3 className="text-gray-700 font-medium mb-3 flex items-center gap-2">
        <Tag className="w-4 h-4" />
        Popular Tags
      </h3>
      <div className="flex flex-wrap gap-2">
        {tags.map((tag) => (
          <button
            key={tag}
            onClick={() => onTagSelect(selectedTag === tag ? undefined : tag)}
            className={`px-3 py-1 text-sm rounded-full transition-colors ${
              selectedTag === tag
                ? 'bg-emerald-600 text-white'
                : 'bg-gray-200 text-gray-600 hover:bg-gray-300'
            }`}
          >
            {tag}
          </button>
        ))}
        {tags.length === 0 && (
          <p className="text-gray-400 text-sm">No tags available</p>
        )}
      </div>
    </div>
  );
}
