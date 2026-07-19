import React, { useEffect, useState } from 'react';
import { adminApi } from '@/features/admin/api';
import { Users, Scale, ShoppingBag, AlertTriangle, TrendingUp, DollarSign } from 'lucide-react';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

export default function AdminDashboardPage() {
    const [overview, setOverview] = useState({
        totalUsers: 0,
        totalAuctions: 0,
        totalOrders: 0,
        totalDisputes: 0,
        totalRevenue: 0
    });
    const [chartData, setChartData] = useState([]);
    const [loading, setLoading] = useState(true);
    const [period, setPeriod] = useState('week');

    useEffect(() => {
        const fetchStats = async () => {
            try {
                const overviewRes = await adminApi.getOverviewStats(period);
                
                if (overviewRes.result) {
                    setOverview({
                        totalUsers: overviewRes.result.totalUsers,
                        totalAuctions: overviewRes.result.totalAuctions,
                        totalOrders: overviewRes.result.totalOrders,
                        totalDisputes: overviewRes.result.totalDisputes,
                        totalRevenue: overviewRes.result.totalRevenue,
                    });
                    setChartData(overviewRes.result.chartData || []);
                }
            } catch (error) {
                console.error("Failed to fetch dashboard stats", error);
            } finally {
                setLoading(false);
            }
        };

        fetchStats();
    }, [period]);

    const statCards = [
        { title: 'Tổng người dùng', value: overview.totalUsers, icon: Users, color: 'text-blue-600', bg: 'bg-blue-50' },
        { title: 'Tổng phiên đấu giá', value: overview.totalAuctions, icon: Scale, color: 'text-indigo-600', bg: 'bg-indigo-50' },
        { title: 'Tổng đơn hàng', value: overview.totalOrders, icon: ShoppingBag, color: 'text-emerald-600', bg: 'bg-emerald-50' },
        { title: 'Khiếu nại', value: overview.totalDisputes, icon: AlertTriangle, color: 'text-red-600', bg: 'bg-red-50' },
    ];

    const formatCurrency = (value) => {
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
    };

    if (loading) {
        return <div className="flex items-center justify-center h-full"><span className="loading loading-spinner text-[#111111] loading-lg"></span></div>;
    }

    return (
        <div className="space-y-6">
            {/* Top Stats */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                {statCards.map((stat, idx) => (
                    <div key={idx} className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 flex items-center justify-between">
                        <div>
                            <p className="text-sm font-medium text-gray-500 mb-1">{stat.title}</p>
                            <h3 className="text-2xl font-bold text-gray-900">{stat.value.toLocaleString('vi-VN')}</h3>
                        </div>
                        <div className={`w-12 h-12 rounded-full ${stat.bg} ${stat.color} flex items-center justify-center`}>
                            <stat.icon className="h-6 w-6" />
                        </div>
                    </div>
                ))}
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* Revenue Chart */}
                <div className="lg:col-span-2 bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
                    <div className="flex items-center justify-between mb-6">
                        <div>
                            <h3 className="text-lg font-bold text-gray-900">Doanh thu theo thời gian</h3>
                            <p className="text-sm text-gray-500">Thống kê doanh thu từ phí nền tảng</p>
                        </div>
                        <div className="flex items-center gap-3">
                            <select 
                                className="select select-bordered select-sm w-32 focus:outline-none"
                                value={period}
                                onChange={(e) => setPeriod(e.target.value)}
                            >
                                <option value="week">7 ngày qua</option>
                                <option value="month">30 ngày qua</option>
                                <option value="year">12 tháng qua</option>
                            </select>
                            <div className="flex items-center gap-2 px-3 py-1.5 bg-green-50 text-green-700 rounded-lg font-medium text-sm">
                                <TrendingUp className="h-4 w-4" />
                                <span>Tăng trưởng</span>
                            </div>
                        </div>
                    </div>
                    
                    <div className="h-80 w-full">
                        {chartData.length > 0 ? (
                            <ResponsiveContainer width="100%" height="100%">
                                <AreaChart data={chartData} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
                                    <defs>
                                        <linearGradient id="colorRevenue" x1="0" y1="0" x2="0" y2="1">
                                            <stop offset="5%" stopColor="#111111" stopOpacity={0.1}/>
                                            <stop offset="95%" stopColor="#111111" stopOpacity={0}/>
                                        </linearGradient>
                                    </defs>
                                    <XAxis 
                                        dataKey="date" 
                                        axisLine={false}
                                        tickLine={false}
                                        tick={{ fill: '#6B7280', fontSize: 12 }}
                                        dy={10}
                                    />
                                    <YAxis 
                                        axisLine={false}
                                        tickLine={false}
                                        tick={{ fill: '#6B7280', fontSize: 12 }}
                                        tickFormatter={(value) => `${(value / 1000000).toFixed(0)}M`}
                                        dx={-10}
                                    />
                                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E5E7EB" />
                                    <Tooltip 
                                        contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)' }}
                                        formatter={(value) => [formatCurrency(value), 'Doanh thu']}
                                        labelStyle={{ color: '#6B7280', marginBottom: '4px' }}
                                    />
                                    <Area type="monotone" dataKey="revenue" stroke="#111111" strokeWidth={3} fillOpacity={1} fill="url(#colorRevenue)" />
                                </AreaChart>
                            </ResponsiveContainer>
                        ) : (
                            <div className="h-full flex items-center justify-center text-gray-500">
                                Chưa có dữ liệu doanh thu
                            </div>
                        )}
                    </div>
                </div>

                {/* Total Revenue Summary */}
                <div className="bg-[#111111] rounded-2xl p-6 shadow-sm text-white flex flex-col justify-between relative overflow-hidden">
                    <div className="absolute top-0 right-0 p-8 opacity-10 pointer-events-none">
                        <DollarSign className="w-32 h-32" />
                    </div>
                    <div>
                        <h3 className="text-lg font-medium text-gray-400 mb-1">Tổng doanh thu nền tảng</h3>
                        <p className="text-4xl font-black mt-2">
                            {formatCurrency(overview.totalRevenue)}
                        </p>
                    </div>
                    
                    <div className="mt-8 space-y-4">
                        <div className="bg-white/10 rounded-xl p-4 backdrop-blur-sm">
                            <p className="text-sm text-gray-300">Trung bình mỗi kỳ ({period === 'year' ? '12 tháng' : period === 'month' ? '30 ngày' : '7 ngày'})</p>
                            <p className="text-xl font-bold mt-1">
                                {chartData.length > 0 
                                    ? formatCurrency(chartData.reduce((acc, curr) => acc + curr.revenue, 0) / chartData.length)
                                    : '0 đ'}
                            </p>
                        </div>
                        <button className="w-full py-3 bg-white text-[#111111] font-bold rounded-xl hover:bg-gray-100 transition-colors">
                            Xuất báo cáo
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}
