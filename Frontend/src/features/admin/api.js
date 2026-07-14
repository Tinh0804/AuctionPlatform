import apiClient from '@/services/apiClient';

export const getOverviewStats = (period = 'week') =>
    apiClient.get(`/admin/stats/overview?period=${period}`).then(r => r.data);
