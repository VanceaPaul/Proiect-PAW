import axios from 'axios';

const api = axios.create({
  baseURL: '/api'
});

const TOKEN_KEY = 'token';

const readToken = () => {
  if (typeof window === 'undefined') {
    return null;
  }
  const { sessionStorage, localStorage } = window;
  const token = sessionStorage.getItem(TOKEN_KEY);
  if (token) {
    return token;
  }
  const legacy = localStorage.getItem(TOKEN_KEY);
  if (legacy) {
    sessionStorage.setItem(TOKEN_KEY, legacy);
    localStorage.removeItem(TOKEN_KEY);
    return legacy;
  }
  return null;
};

api.interceptors.request.use((config) => {
  const token = readToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;
