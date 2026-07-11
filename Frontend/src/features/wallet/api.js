
import apiClient from '@/services/apiClient';


export const requestDeposit = (amount, provider) =>
  apiClient
    .post(`/wallets/deposit/request?amount=${amount}&provider=${provider}`)
    .then((r) => r.data);

export const requestWithdraw = (withdrawInfo) =>
  apiClient
    .post('/wallets/withdraw', withdrawInfo)
    .then((r) => r.data);


export const setupPin = (data) =>
  apiClient
    .post('/wallets/pin/setup', data)
    .then((r) => r.data);

export const getWalletHistory = () =>
  apiClient
    .get('/wallets/history')
    .then((r) => r.data);
