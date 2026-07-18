import apiClient from '@/services/apiClient';

export const getOverviewStats = (period = 'week') =>
    apiClient.get(`/admin/stats/overview?period=${period}`).then(r => r.data);

// Users Management
export const getAllUsers = (params) =>
    apiClient.get('/admin/users', { params }).then(r => r.data);

export const getUserDetail = (id) =>
    apiClient.get(`/admin/users/${id}`).then(r => r.data);

export const toggleUserStatus = (id) =>
    apiClient.put(`/admin/users/${id}/toggle-active`).then(r => r.data);

export const toggleWalletStatus = (id) =>
    apiClient.put(`/admin/users/${id}/wallet/toggle-status`).then(r => r.data);

export const updateVerificationStatus = (id, status) =>
    apiClient.put(`/admin/users/${id}/verification?status=${status}`).then(r => r.data);

export const updateUser = (id, data) =>
    apiClient.put(`/admin/users/${id}`, data).then(r => r.data);

export const deleteUser = (id) =>
    apiClient.delete(`/admin/users/${id}`).then(r => r.data);

