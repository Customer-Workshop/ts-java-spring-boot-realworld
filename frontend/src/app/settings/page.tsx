'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { AuthAPI } from '@/lib/api';
import { useAuthStore } from '@/lib/store';
import { getErrorMessage } from '@/lib/utils';
import LoadingSpinner from '@/components/LoadingSpinner';

export default function SettingsPage() {
  const router = useRouter();
  const { user, setUser, logout, isAuthenticated, isLoading: authLoading } = useAuthStore();
  const [image, setImage] = useState('');
  const [username, setUsername] = useState('');
  const [bio, setBio] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [errors, setErrors] = useState<string[]>([]);
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login');
    }
  }, [authLoading, isAuthenticated, router]);

  useEffect(() => {
    if (user) {
      setImage(user.image || '');
      setUsername(user.username);
      setBio(user.bio || '');
      setEmail(user.email);
    }
  }, [user]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setErrors([]);
    setSuccess(false);

    const validationErrors: string[] = [];
    if (!username.trim()) validationErrors.push('Username is required');
    if (!email.trim()) validationErrors.push('Email is required');

    if (validationErrors.length > 0) {
      setErrors(validationErrors);
      setIsLoading(false);
      return;
    }

    try {
      const updatedUser = await AuthAPI.updateUser({
        image: image || undefined,
        username,
        bio: bio || undefined,
        email,
        password: password || undefined,
      });
      setUser(updatedUser);
      setPassword('');
      setSuccess(true);
    } catch (err) {
      setErrors([getErrorMessage(err)]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleLogout = () => {
    logout();
    router.push('/');
  };

  if (authLoading || !isAuthenticated) {
    return <LoadingSpinner className="py-12" />;
  }

  return (
    <div className="max-w-xl mx-auto px-4 py-12">
      <h1 className="text-3xl font-bold text-gray-900 text-center mb-8">
        Your Settings
      </h1>

      {errors.length > 0 && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-6">
          <ul className="list-disc list-inside text-red-600 space-y-1">
            {errors.map((error, index) => (
              <li key={index}>{error}</li>
            ))}
          </ul>
        </div>
      )}

      {success && (
        <div className="bg-green-50 border border-green-200 rounded-lg p-4 mb-6">
          <p className="text-green-600">Settings updated successfully!</p>
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <input
            type="text"
            value={image}
            onChange={(e) => setImage(e.target.value)}
            placeholder="URL of profile picture"
            className="w-full px-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
          />
        </div>
        <div>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="Username"
            className="w-full px-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
          />
        </div>
        <div>
          <textarea
            value={bio}
            onChange={(e) => setBio(e.target.value)}
            placeholder="Short bio about you"
            rows={6}
            className="w-full px-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent resize-none"
          />
        </div>
        <div>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="Email"
            className="w-full px-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
          />
        </div>
        <div>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="New Password (leave blank to keep current)"
            className="w-full px-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
          />
        </div>
        <div className="flex justify-end">
          <button
            type="submit"
            disabled={isLoading}
            className="px-6 py-3 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center gap-2"
          >
            {isLoading ? (
              <>
                <LoadingSpinner size="sm" />
                <span>Updating...</span>
              </>
            ) : (
              <span>Update Settings</span>
            )}
          </button>
        </div>
      </form>

      <hr className="my-8 border-gray-200" />

      <button
        onClick={handleLogout}
        className="w-full py-3 border border-red-500 text-red-500 rounded-lg hover:bg-red-50 transition-colors"
      >
        Or click here to logout
      </button>
    </div>
  );
}
