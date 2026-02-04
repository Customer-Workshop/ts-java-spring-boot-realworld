'use client';

import Link from 'next/link';
import { useAuthStore } from '@/lib/store';
import { PenSquare, Settings, User, LogOut } from 'lucide-react';

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuthStore();

  return (
    <nav className="bg-white shadow-sm border-b border-gray-100 sticky top-0 z-50">
      <div className="max-w-6xl mx-auto px-4">
        <div className="flex justify-between items-center h-16">
          <Link
            href="/"
            className="text-2xl font-bold text-emerald-600 hover:text-emerald-700 transition-colors"
          >
            conduit
          </Link>

          <div className="flex items-center gap-1">
            <Link
              href="/"
              className="px-4 py-2 text-gray-600 hover:text-gray-900 rounded-lg hover:bg-gray-50 transition-colors"
            >
              Home
            </Link>

            {isAuthenticated ? (
              <>
                <Link
                  href="/editor"
                  className="flex items-center gap-2 px-4 py-2 text-gray-600 hover:text-gray-900 rounded-lg hover:bg-gray-50 transition-colors"
                >
                  <PenSquare className="w-4 h-4" />
                  <span>New Article</span>
                </Link>
                <Link
                  href="/settings"
                  className="flex items-center gap-2 px-4 py-2 text-gray-600 hover:text-gray-900 rounded-lg hover:bg-gray-50 transition-colors"
                >
                  <Settings className="w-4 h-4" />
                  <span>Settings</span>
                </Link>
                <Link
                  href={`/profile/${user?.username}`}
                  className="flex items-center gap-2 px-4 py-2 text-gray-600 hover:text-gray-900 rounded-lg hover:bg-gray-50 transition-colors"
                >
                  {user?.image ? (
                    <img
                      src={user.image}
                      alt={user.username}
                      className="w-6 h-6 rounded-full object-cover"
                    />
                  ) : (
                    <User className="w-4 h-4" />
                  )}
                  <span>{user?.username}</span>
                </Link>
                <button
                  onClick={logout}
                  className="flex items-center gap-2 px-4 py-2 text-gray-600 hover:text-red-600 rounded-lg hover:bg-red-50 transition-colors"
                >
                  <LogOut className="w-4 h-4" />
                  <span>Logout</span>
                </button>
              </>
            ) : (
              <>
                <Link
                  href="/login"
                  className="px-4 py-2 text-gray-600 hover:text-gray-900 rounded-lg hover:bg-gray-50 transition-colors"
                >
                  Sign in
                </Link>
                <Link
                  href="/register"
                  className="px-4 py-2 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 transition-colors"
                >
                  Sign up
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}
