// ============================================================
// src/services/apiClient.js
// Axios instance trung tâm - Tối ưu luồng Auth (Queue Refresh)
// ============================================================
import axios from 'axios';
import { TOKEN_KEY, REFRESH_TOKEN_KEY, USER_KEY } from '@/config/constants';

export const API_URL = import.meta.env.VITE_API_URL;
export const WS_URL = import.meta.env.VITE_WS_URL;

const apiClient = axios.create({
  baseURL: API_URL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// ─── Request Interceptor ─────────────────────────────────────────────────────
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(TOKEN_KEY);
    
    // Danh sách các public endpoint không cần đính kèm token
    const publicEndpoints = ['/auth/login', '/auth/refresh', '/auth/register'];
    const isPublic = publicEndpoints.some(endpoint => config.url?.includes(endpoint));

    if (token && !isPublic) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    // Bypass ngrok free tier browser warning
    config.headers['ngrok-skip-browser-warning'] = 'true';
    
    return config;
  },
  (error) => Promise.reject(error)
);

// ─── Response Interceptor (Refresh Token with Queue) ─────────────────────────
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) prom.reject(error);
    else prom.resolve(token);
  });
  failedQueue = [];
};

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      // Bỏ qua logic refresh token nếu là các API public (login, register...)
      const publicEndpoints = ['/auth/login', '/auth/refresh', '/auth/register'];
      if (publicEndpoints.some(endpoint => originalRequest.url?.includes(endpoint))) {
        return Promise.reject(error);
      }

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return apiClient(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const token = localStorage.getItem(TOKEN_KEY);
      const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);

      if (!refreshToken) {
        // Clear auth and redirect to login
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(REFRESH_TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
        if (!window.location.pathname.includes('/login')) {
            window.location.href = '/login?sessionExpired=true';
        }
        return Promise.reject(error);
      }

      try {
        // Lưu ý: Backend Auction yêu cầu body gửi lên gồm cả token và refreshToken
        const { data } = await axios.post(`${API_URL}/auth/refresh`, { 
            token: token, 
            refreshToken: refreshToken 
        });
        
        const newToken = data?.result?.token || data?.token;
        const newRefreshToken = data?.result?.refreshToken || data?.refreshToken;
        
        if (newToken) {
          localStorage.setItem(TOKEN_KEY, newToken);
          if (newRefreshToken) {
              localStorage.setItem(REFRESH_TOKEN_KEY, newRefreshToken);
          }
          apiClient.defaults.headers.common.Authorization = `Bearer ${newToken}`;
          processQueue(null, newToken);
          originalRequest.headers.Authorization = `Bearer ${newToken}`;
          return apiClient(originalRequest);
        }
      } catch (err) {
        processQueue(err, null);
        // Clear auth and redirect to login
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(REFRESH_TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
        if (!window.location.pathname.includes('/login')) {
            window.location.href = '/login?sessionExpired=true';
        }
        return Promise.reject(err);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

export default apiClient;
