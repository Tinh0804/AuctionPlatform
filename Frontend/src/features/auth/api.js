
import apiClient from '@/services/apiClient';

export const login = (credentials) =>
  apiClient.post('/auth/login', credentials).then((r) => r.data);

export const register = (data) =>
  apiClient.post('/auth/register', data).then((r) => r.data);


export const refreshToken = (token) =>
  apiClient.post('/auth/refresh', { token }).then((r) => r.data);


export const getMyInfo = () =>
  apiClient.get('/users/my-info').then((r) => r.data);

export const logout = (token) =>
  apiClient.post('/auth/logout', { token }).then((r) => r.data);

export const getNotifications = () =>
  apiClient.get('/notifications/my').then((r) => r.data);

export const markNotificationRead = (id) =>
  apiClient.post(`/notifications/${id}/read`).then((r) => r.data);

export const updateMyInfo = (data) =>
  apiClient.put('/users/my-info', data).then((r) => r.data);

export const uploadAvatar = (file) => {
  const formData = new FormData();
  formData.append('file', file);
  return apiClient.post('/users/my-info/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }).then((r) => r.data);
};

export const updateMyPhone = (firebaseIdToken) =>
  apiClient.post('/users/my-info/phone', { firebase_id_token: firebaseIdToken }).then((r) => r.data);

export const addAddress = (data) =>
  apiClient.post('/users/my-info/addresses', data).then((r) => r.data);

export const updateAddress = (id, data) =>
  apiClient.put(`/users/my-info/addresses/${id}`, data).then((r) => r.data);

export const deleteAddress = (id) =>
  apiClient.delete(`/users/my-info/addresses/${id}`).then((r) => r.data);
