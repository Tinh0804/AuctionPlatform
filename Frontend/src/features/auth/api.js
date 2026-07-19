import apiClient from '@/services/apiClient';

export const authApi = {
  login: (credentials) =>
    apiClient.post('/auth/login', credentials).then((r) => r.data),

  register: (data) =>
    apiClient.post('/auth/register', data).then((r) => r.data),

  refreshToken: (token) =>
    apiClient.post('/auth/refresh', { token }).then((r) => r.data),

  getMyInfo: () =>
    apiClient.get('/users/my-info').then((r) => r.data),

  logout: (token) =>
    apiClient.post('/auth/logout', { token }).then((r) => r.data),

  getNotifications: () =>
    apiClient.get('/notifications/my').then((r) => r.data),

  markNotificationRead: (id) =>
    apiClient.post(`/notifications/${id}/read`).then((r) => r.data),

  updateMyInfo: (data) =>
    apiClient.put('/users/my-info', data).then((r) => r.data),

  uploadAvatar: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return apiClient.post('/users/my-info/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }).then((r) => r.data);
  },

  updateMyPhone: (firebaseIdToken) =>
    apiClient.post('/users/my-info/phone', { firebase_id_token: firebaseIdToken }).then((r) => r.data),

  addAddress: (data) =>
    apiClient.post('/users/my-info/addresses', data).then((r) => r.data),

  updateAddress: (id, data) =>
    apiClient.put(`/users/my-info/addresses/${id}`, data).then((r) => r.data),

  deleteAddress: (id) =>
    apiClient.delete(`/users/my-info/addresses/${id}`).then((r) => r.data)
};
