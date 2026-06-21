// ============================================================
// src/features/auth/api.js
// Tập trung các hàm gọi HTTP liên quan đến xác thực
// ============================================================
import apiClient from '@/services/apiClient';

/**
 * Đăng nhập bằng username/password
 */
export const login = (credentials) =>
  apiClient.post('/auth/login', credentials).then((r) => r.data);

/**
 * Đăng ký tài khoản mới
 */
export const register = (data) =>
  apiClient.post('/auth/register', data).then((r) => r.data);

/**
 * Làm mới access token bằng refresh token
 */
export const refreshToken = (token) =>
  apiClient.post('/auth/refresh', { token }).then((r) => r.data);

/**
 * Lấy thông tin profile của user đang đăng nhập
 */
export const getMyInfo = () =>
  apiClient.get('/users/my-info').then((r) => r.data);

/**
 * Đăng xuất (blacklist token)
 */
export const logout = (token) =>
  apiClient.post('/auth/logout', { token }).then((r) => r.data);

/**
 * Lấy danh sách thông báo
 */
export const getNotifications = () =>
  apiClient.get('/auth/me/notifications').then((r) => r.data);

/**
 * Đánh dấu thông báo đã đọc
 */
export const markNotificationRead = (id) =>
  apiClient.post(`/auth/notifications/${id}/read`).then((r) => r.data);
