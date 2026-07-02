// ============================================================
// src/features/payment/api.js
// Tập trung các hàm gọi HTTP liên quan đến thanh toán
// ============================================================
import apiClient from '@/services/apiClient';

/**
 * Lấy thông tin invoice để checkout
 */
export const getInvoice = (invoiceId) =>
  apiClient.get(`/invoices/${invoiceId}`).then((r) => r.data);

export const createMomoPayment = (invoiceId) =>
  apiClient
    .post(`/payments/momo/create`, { invoice_id: invoiceId })
    .then((r) => r.data);

export const createVnpayPayment = (invoiceId) =>
  apiClient
    .post(`/payments/vnpay/create`, { invoice_id: invoiceId })
    .then((r) => r.data);

export const depositWallet = (amount) =>
  apiClient
    .post('/wallets/deposit/momo', { amount })
    .then((r) => r.data);

export const verifyMomoReturn = (params) =>
  apiClient
    .get('/payments/momo/return', { params })
    .then((r) => r.data);

/**
 * Xác minh kết quả thanh toán VNPay
 */
export const verifyVnpayReturn = (queryString) =>
  apiClient
    .get(`/payments/vnpay/callback${queryString}`)
    .then((r) => r.data);
