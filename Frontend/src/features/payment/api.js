import apiClient from '@/services/apiClient';

export const paymentApi = {
  getInvoice: (invoiceId) =>
    apiClient.get(`/invoices/${invoiceId}`).then((r) => r.data),

  createMomoPayment: (invoiceId) =>
    apiClient
      .post(`/payments/momo/create`, { invoice_id: invoiceId })
      .then((r) => r.data),

  createVnpayPayment: (invoiceId) =>
    apiClient
      .post(`/payments/vnpay/create`, { invoice_id: invoiceId })
      .then((r) => r.data),

  depositWallet: (amount) =>
    apiClient
      .post('/wallets/deposit/momo', { amount })
      .then((r) => r.data),

  verifyMomoReturn: (params) =>
    apiClient
      .get('/payments/momo/return', { params })
      .then((r) => r.data),

  verifyVnpayReturn: (queryString) =>
    apiClient
      .get(`/payments/vnpay/callback${queryString}`)
      .then((r) => r.data)
};
