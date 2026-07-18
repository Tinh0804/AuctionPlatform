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


// Categories Management
export const getAllCategories = () =>
    apiClient.get('/auctions/categories').then(r => r.data);

export const createCategory = (data) =>
    apiClient.post('/admin/categories', data).then(r => r.data);

export const updateCategory = (id, data) =>
    apiClient.put(`/admin/categories/${id}`, data).then(r => r.data);

export const deleteCategory = (id) =>
    apiClient.delete(`/admin/categories/${id}`).then(r => r.data);

// Auctions Management
export const getAllAuctions = (params) =>
    apiClient.get('/admin/auctions', { params }).then(r => r.data);

export const getAuctionDetail = (id) =>
    apiClient.get(`/auctions/${id}`).then(r => r.data);

export const updateAuctionStatus = (id, status) =>
    apiClient.put(`/admin/auctions/${id}/status?status=${status}`).then(r => r.data);

export const updateAuction = (id, data) =>
    apiClient.put(`/admin/auctions/${id}`, data).then(r => r.data);

export const deleteAuction = (id) =>
    apiClient.delete(`/admin/auctions/${id}`).then(r => r.data);
