import React, { createContext, useContext, useState, useCallback } from 'react';
import { loginUser } from '../api/campaigns';

interface AuthState {
  token: string | null;
  username: string | null;
  isAuthenticated: boolean;
  hasMarketingEntitlement: boolean;
}

interface AuthContextType extends AuthState {
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  setEntitlement: (has: boolean) => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [state, setState] = useState<AuthState>(() => {
    const token = localStorage.getItem('token');
    const username = localStorage.getItem('username');
    return {
      token,
      username,
      isAuthenticated: !!token,
      hasMarketingEntitlement: true,
    };
  });

  const login = useCallback(async (email: string, password: string) => {
    const user = await loginUser(email, password);
    localStorage.setItem('token', user.token);
    localStorage.setItem('username', user.username);
    setState({
      token: user.token,
      username: user.username,
      isAuthenticated: true,
      hasMarketingEntitlement: true,
    });
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    setState({
      token: null,
      username: null,
      isAuthenticated: false,
      hasMarketingEntitlement: false,
    });
  }, []);

  const setEntitlement = useCallback((has: boolean) => {
    setState((prev) => ({ ...prev, hasMarketingEntitlement: has }));
  }, []);

  return (
    <AuthContext.Provider value={{ ...state, login, logout, setEntitlement }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
}
