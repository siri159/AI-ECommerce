import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import authApi from '../api/authApi';

const AuthContext = createContext(null);

const STORAGE_KEY = 'ecommerce_auth';

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  });

  const [token, setToken] = useState(() => localStorage.getItem('jwt_token') || '');

  useEffect(() => {
    if (user && token) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(user));
    } else {
      localStorage.removeItem(STORAGE_KEY);
    }
  }, [user, token]);

  const login = async (credentials) => {
    const response = await authApi.login(credentials);
    const authUser = {
      userId: response.userId,
      name: response.name,
      email: response.email,
      role: response.role,
    };

    setToken(response.token);
    localStorage.setItem('jwt_token', response.token);
    setUser(authUser);
    return response;
  };

  const register = async (payload) => {
    const response = await authApi.register(payload);
    const authUser = {
      userId: response.userId,
      name: response.name,
      email: response.email,
      role: response.role,
    };

    setToken(response.token);
    localStorage.setItem('jwt_token', response.token);
    setUser(authUser);
    return response;
  };

  const logout = () => {
    setUser(null);
    setToken('');
    localStorage.removeItem('jwt_token');
    localStorage.removeItem(STORAGE_KEY);
  };

  const value = useMemo(
    () => ({
      user,
      token,
      isAuthenticated: !!token,
      role: user?.role || null,
      login,
      register,
      logout,
    }),
    [user, token]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}
