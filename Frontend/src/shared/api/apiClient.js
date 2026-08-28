// ============================================================
// src/services/apiClient.js
// Axios instance trung tâm — thay thế src/api/client.js
// ============================================================
import axios from 'axios';
import { REFRESH_TOKEN_KEY, TOKEN_KEY, USER_KEY } from '@/config/constants';

export const API_URL =
  import.meta.env.VITE_API_URL;

const apiClient = axios.create({
  baseURL: API_URL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(TOKEN_KEY);
    const publicEndpoints = ['/auth/login', '/auth/refresh', '/auth/register'];
    const isPublic = publicEndpoints.some((endpoint) => config.url?.includes(endpoint));
    if (token && !isPublic) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    // Bypass ngrok free tier browser warning
    config.headers['ngrok-skip-browser-warning'] = 'true';
    return config;
  },
  (error) => Promise.reject(error)
);

let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach(({ resolve, reject }) => {
    if (error) reject(error);
    else resolve(token);
  });
  failedQueue = [];
};

const clearSession = () => {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
};

// Chỉ thực hiện một refresh request khi nhiều API cùng trả về 401.
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      const publicEndpoints = ['/auth/login', '/auth/refresh', '/auth/register'];
      if (publicEndpoints.some((endpoint) => originalRequest.url?.includes(endpoint))) {
        return Promise.reject(error);
      }

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return apiClient(originalRequest);
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const token = localStorage.getItem(TOKEN_KEY);
      const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);
      if (!token || !refreshToken) {
        const sessionError = new Error('Missing refresh token');
        processQueue(sessionError);
        clearSession();
        isRefreshing = false;
        if (!window.location.pathname.includes('/login')) {
          window.location.href = '/login?sessionExpired=true';
        }
        return Promise.reject(error);
      }

      try {
        const res = await axios.post(`${API_URL}/auth/refresh`, { token, refreshToken });
        const payload = res.data?.result || res.data;
        const newToken = payload?.token;
        const newRefreshToken = payload?.refreshToken;
        if (newToken) {
          localStorage.setItem(TOKEN_KEY, newToken);
          if (newRefreshToken) localStorage.setItem(REFRESH_TOKEN_KEY, newRefreshToken);
          apiClient.defaults.headers.common.Authorization = `Bearer ${newToken}`;
          processQueue(null, newToken);
          originalRequest.headers.Authorization = `Bearer ${newToken}`;
          return apiClient(originalRequest);
        }
        throw new Error('Refresh response did not contain a token');
      } catch (refreshError) {
        processQueue(refreshError);
        clearSession();
        if (!window.location.pathname.includes('/login')) {
          window.location.href = '/login?sessionExpired=true';
        }
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }
    return Promise.reject(error);
  }
);

export const WS_URL = import.meta.env.VITE_WS_URL;

export default apiClient;
