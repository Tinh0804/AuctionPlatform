// ============================================================
// src/config/index.js
// Định nghĩa các hằng số tĩnh dùng toàn project
// ============================================================

export const ROLES = {
  ADMIN: 'ROLE_ADMIN',
  SELLER: 'ROLE_SELLER',
  BIDDER: 'ROLE_BIDDER',
};

export const AUCTION_STATUS = {
  PENDING: 'PENDING',
  ACTIVE: 'ACTIVE',
  ENDED: 'ENDED',
  CANCELLED: 'CANCELLED',
};

export const PRODUCT_STATUS = {
  PENDING: 'PENDING',
  APPROVED: 'APPROVED',
  REJECTED: 'REJECTED',
};

export const ROUTES = {
  HOME: '/',
  LOGIN: '/login',
  PROFILE: '/profile',
  EKYC: '/ekyc',
  AUCTIONS: '/auctions',
  AUCTION_DETAIL: '/auctions/:id',
  CREATE_AUCTION: '/auctions/create',
  CHECKOUT: '/invoices/:invoice_id/checkout',
  PAYMENT_RESULT: '/payment/:status',
  NOT_FOUND: '*',
};

export const API_BASE_URL =
  import.meta.env.VITE_API_URL || 'https://alone-spinner-estimator.ngrok-free.dev';
