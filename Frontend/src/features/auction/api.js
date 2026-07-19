import apiClient from '@/services/apiClient';

export const auctionApi = {
  getAuctions: (params) =>
    apiClient.get('/auctions', { params }).then((r) => r.data),

  getAuctionById: (id) =>
    apiClient.get(`/auctions/${id}`).then((r) => r.data),

  createAuction: (formData) =>
    apiClient
      .post('/auctions/create-auction', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      .then((r) => r.data),

  getCategories: () =>
    apiClient.get('/auctions/categories').then((r) => r.data),

  placeBid: (auctionId, amount) =>
    apiClient
      .post(`/auctions/${auctionId}/bids`, { amount })
      .then((r) => r.data),

  getBidHistory: (auctionId) =>
    apiClient.get(`/auctions/${auctionId}/bids`).then((r) => r.data)
};
