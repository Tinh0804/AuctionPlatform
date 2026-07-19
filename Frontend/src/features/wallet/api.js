import apiClient from '@/services/apiClient';

export const walletApi = {
  requestDeposit: (amount, provider) =>
    apiClient
      .post(`/wallets/deposit/request?amount=${amount}&provider=${provider}`)
      .then((r) => r.data),

  requestWithdraw: (withdrawInfo) =>
    apiClient
      .post('/wallets/withdraw', withdrawInfo)
      .then((r) => r.data),

  setupPin: (data) =>
    apiClient
      .post('/wallets/pin/setup', data)
      .then((r) => r.data),

  getWalletHistory: () =>
    apiClient
      .get('/wallets/history')
      .then((r) => r.data)
};
