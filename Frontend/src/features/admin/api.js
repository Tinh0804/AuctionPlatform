import apiClient from '@/services/apiClient';

export const adminApi = {
  getOverviewStats: (period = 'week') =>
    apiClient.get(`/admin/stats/overview?period=${period}`).then(r => r.data),

  getAllUsers: (params) =>
    apiClient.get('/admin/users', { params }).then(r => r.data),

  getUserDetail: (id) =>
    apiClient.get(`/admin/users/${id}`).then(r => r.data),

  toggleUserStatus: (id) =>
    apiClient.put(`/admin/users/${id}/toggle-active`).then(r => r.data),

  toggleWalletStatus: (id) =>
    apiClient.put(`/admin/users/${id}/wallet/toggle-status`).then(r => r.data),

  updateVerificationStatus: (id, status) =>
    apiClient.put(`/admin/users/${id}/verification?status=${status}`).then(r => r.data),

  updateUser: (id, data) =>
    apiClient.put(`/admin/users/${id}`, data).then(r => r.data),

  deleteUser: (id) =>
    apiClient.delete(`/admin/users/${id}`).then(r => r.data),

  getAllCategories: () =>
    apiClient.get('/auctions/categories').then(r => r.data),

  createCategory: (data) =>
    apiClient.post('/admin/categories', data).then(r => r.data),

  updateCategory: (id, data) =>
    apiClient.put(`/admin/categories/${id}`, data).then(r => r.data),

  deleteCategory: (id) =>
    apiClient.delete(`/admin/categories/${id}`).then(r => r.data),

  getAllAuctions: (params) =>
    apiClient.get('/admin/auctions', { params }).then(r => r.data),

  getAuctionDetail: (id) =>
    apiClient.get(`/auctions/${id}`).then(r => r.data),

  updateAuctionStatus: (id, status) =>
    apiClient.put(`/admin/auctions/${id}/status?status=${status}`).then(r => r.data),

  updateAuction: (id, data) =>
    apiClient.put(`/admin/auctions/${id}`, data).then(r => r.data),

  deleteAuction: (id) =>
    apiClient.delete(`/admin/auctions/${id}`).then(r => r.data),

  // Disputes Management
  getAllDisputes: (params) =>
    apiClient.get('/admin/disputes', { params }).then(r => r.data),

  getDisputeDetail: (id) =>
    apiClient.get(`/admin/disputes/${id}`).then(r => r.data),

  resolveDispute: (id, data) =>
    apiClient.post(`/admin/disputes/${id}/resolve`, data).then(r => r.data)
};
