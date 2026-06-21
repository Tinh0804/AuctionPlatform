// ============================================================
// src/features/auction/api.js
// Tập trung các hàm gọi HTTP liên quan đến đấu giá
// ============================================================
import apiClient from '@/services/apiClient';

/**
 * Lấy danh sách phiên đấu giá (có filter)
 */
export const getAuctions = (params) =>
  apiClient.get('/auctions', { params }).then((r) => r.data);

/**
 * Lấy chi tiết một phiên đấu giá
 */
export const getAuctionById = (id) =>
  apiClient.get(`/auctions/${id}`).then((r) => r.data);

/**
 * Tạo phiên đấu giá mới (multipart/form-data)
 */
export const createAuction = (formData) =>
  apiClient
    .post('/auctions/create-auction', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    .then((r) => r.data);

/**
 * Lấy danh sách danh mục sản phẩm
 */
export const getCategories = () =>
  apiClient.get('/auctions/categories').then((r) => r.data);

/**
 * Đặt giá (bid) vào một phiên đấu giá
 */
export const placeBid = (auctionId, amount) =>
  apiClient
    .post(`/auctions/${auctionId}/bids`, { amount })
    .then((r) => r.data);

/**
 * Lấy lịch sử đặt giá của một phiên
 */
export const getBidHistory = (auctionId) =>
  apiClient.get(`/auctions/${auctionId}/bids`).then((r) => r.data);
