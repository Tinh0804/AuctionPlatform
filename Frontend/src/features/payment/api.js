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

/**
 * Tạo thanh toán qua MoMo
 */
export const createMomoPayment = (invoiceId) =>
  apiClient
    .post(`/payments/momo/create`, { invoice_id: invoiceId })
    .then((r) => r.data);

/**
 * Tạo thanh toán qua VNPay
 */
export const createVnpayPayment = (invoiceId) =>
  apiClient
    .post(`/payments/vnpay/create`, { invoice_id: invoiceId })
    .then((r) => r.data);

/**
 * Nạp tiền vào ví qua MoMo
 */
export const depositWallet = (amount) =>
  apiClient
    .post('/wallets/deposit/momo', { amount })
    .then((r) => r.data);

/**
 * Xác minh kết quả thanh toán MoMo
 */
export const verifyMomoReturn = (params) =>
  apiClient
    .get('/payments/momo/return', { params })
    .then((r) => r.data);
