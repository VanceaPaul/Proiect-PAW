import React, { createContext, useContext, useMemo, useState } from 'react';

const AuthContext = createContext();

const TOKEN_KEY = 'token';

const decodeRole = (token) => {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.role;
  } catch (error) {
    return null;
  }
};

const readStoredToken = () => {
  if (typeof window === 'undefined') {
    return null;
  }
  const { sessionStorage, localStorage } = window;
  const existing = sessionStorage.getItem(TOKEN_KEY);
  if (existing) {
    return existing;
  }
  const legacy = localStorage.getItem(TOKEN_KEY);
  if (legacy) {
    sessionStorage.setItem(TOKEN_KEY, legacy);
    localStorage.removeItem(TOKEN_KEY);
    return legacy;
  }
  return null;
};

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(() => readStoredToken());
  const [role, setRole] = useState(() => {
    const stored = readStoredToken();
    return stored ? decodeRole(stored) : null;
  });

  const login = (jwt) => {
    if (typeof window !== 'undefined') {
      window.sessionStorage.setItem(TOKEN_KEY, jwt);
      window.localStorage.removeItem(TOKEN_KEY);
    }
    setToken(jwt);
    setRole(decodeRole(jwt));
  };

  const logout = () => {
    if (typeof window !== 'undefined') {
      window.sessionStorage.removeItem(TOKEN_KEY);
      window.localStorage.removeItem(TOKEN_KEY);
    }
    setToken(null);
    setRole(null);
  };

  const value = useMemo(() => ({ token, role, login, logout }), [token, role]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => useContext(AuthContext);
