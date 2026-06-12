import axios from 'axios';

export const API_URL = import.meta.env.VITE_API_URL || 'https://alone-spinner-estimator.ngrok-free.dev';

const client = axios.create({
    baseURL: API_URL,
});

client.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        // Bypass ngrok free tier browser warning
        config.headers['ngrok-skip-browser-warning'] = 'true';
        return config;
    },
    (error) => Promise.reject(error)
);

export const WS_URL = API_URL.replace(/^http/, 'ws');

export default client;
