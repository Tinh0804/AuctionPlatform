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

/**
 * Cập nhật thông tin profile cơ bản (tên, ngày sinh, giới tính, email, avatar)
 */
export const updateMyInfo = (data) =>
  apiClient.put('/users/my-info', data).then((r) => r.data);

/**
 * Cập nhật số điện thoại (sau khi xác thực OTP thành công lấy firebaseIdToken)
 */
export const updateMyPhone = (firebaseIdToken) =>
  apiClient.post('/users/my-info/phone', { firebase_id_token: firebaseIdToken }).then((r) => r.data);

/**
 * Thêm địa chỉ mới
 */
export const addAddress = (data) =>
  apiClient.post('/users/my-info/addresses', data).then((r) => r.data);

/**
 * Cập nhật địa chỉ
 */
export const updateAddress = (id, data) =>
  apiClient.put(`/users/my-info/addresses/${id}`, data).then((r) => r.data);

/**
 * Xóa địa chỉ
 */
export const deleteAddress = (id) =>
  apiClient.delete(`/users/my-info/addresses/${id}`).then((r) => r.data);
