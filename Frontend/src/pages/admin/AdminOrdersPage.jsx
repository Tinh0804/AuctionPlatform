import { useState, useEffect } from 'react';
import { ShoppingBag, Search, Filter, ChevronLeft, ChevronRight, CheckCircle, Clock, Truck, CalendarHeart, RefreshCw, XCircle, AlertTriangle } from 'lucide-react';
import { adminApi } from '@/features/admin/api';
import AdminOrderDetailModal from './components/AdminOrderDetailModal';

const STATUS_CONFIG = {
    'ALL': { label: 'Tất cả', icon: null, color: 'text-gray-600', bg: 'bg-gray-100', border: 'border-gray-200' },
    'PENDING_PAYMENT': { label: 'Chờ thanh toán', icon: Clock, color: 'text-yellow-700', bg: 'bg-yellow-50', border: 'border-yellow-200' },
    'PAID': { label: 'Đã thanh toán', icon: CreditCard, color: 'text-blue-700', bg: 'bg-blue-50', border: 'border-blue-200' },
    'SHIPPING': { label: 'Đang giao hàng', icon: Truck, color: 'text-purple-700', bg: 'bg-purple-50', border: 'border-purple-200' },
    'MEETING_SCHEDULED': { label: 'Đã hẹn gặp', icon: CalendarHeart, color: 'text-indigo-700', bg: 'bg-indigo-50', border: 'border-indigo-200' },
    'COMPLETED': { label: 'Hoàn thành', icon: CheckCircle, color: 'text-green-700', bg: 'bg-green-50', border: 'border-green-200' },
    'CANCELLED': { label: 'Đã hủy', icon: XCircle, color: 'text-red-700', bg: 'bg-red-50', border: 'border-red-200' },
    'DISPUTED': { label: 'Đang khiếu nại', icon: AlertTriangle, color: 'text-orange-700', bg: 'bg-orange-50', border: 'border-orange-200' }
};

// Assuming CreditCard is not in imports, add it.
import { CreditCard } from 'lucide-react';

export default function AdminOrdersPage() {
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [pagination, setPagination] = useState({
        currentPage: 1,
        totalPages: 1,
        totalElements: 0,
        pageSize: 10
    });
    const [statusFilter, setStatusFilter] = useState('ALL');
    const [selectedOrder, setSelectedOrder] = useState(null);

    const fetchOrders = async (page = 1, status = statusFilter) => {
        try {
            setLoading(true);
            const params = { page, limit: pagination.pageSize };
            if (status !== 'ALL') {
                params.status = status;
            }
            const res = await adminApi.getAllOrders(params);
            const pageData = res.result || {};
            setOrders(pageData.content || []);
            setPagination({
                currentPage: (pageData.number || 0) + 1,
                totalPages: pageData.totalPages || 1,
                totalElements: pageData.totalElements || 0,
                pageSize: pageData.size || 10
            });
        } catch (error) {
            console.error('Error fetching orders:', error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchOrders();
    }, []);

    const handlePageChange = (newPage) => {
        if (newPage >= 1 && newPage <= pagination.totalPages) {
            fetchOrders(newPage);
        }
    };

    const handleFilterChange = (status) => {
        setStatusFilter(status);
        fetchOrders(1, status);
    };

    return (
        <div className="space-y-6">
            <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
                        <ShoppingBag className="w-8 h-8 text-primary" />
                        Quản lý Đơn hàng
                    </h1>
                    <p className="text-gray-500 mt-1">Tổng quan về tất cả các giao dịch và đơn hàng trên hệ thống</p>
                </div>
                
                <button 
                    onClick={() => fetchOrders(pagination.currentPage)}
                    className="p-2 bg-white border border-gray-200 rounded-xl text-gray-600 hover:text-primary hover:border-primary/50 transition-colors shadow-sm"
                    title="Làm mới"
                >
                    <RefreshCw className={`w-5 h-5 ${loading ? 'animate-spin' : ''}`} />
                </button>
            </div>

            {/* Filters */}
            <div className="bg-white p-4 rounded-2xl shadow-sm border border-gray-100 flex flex-wrap gap-2">
                <div className="flex items-center gap-2 mr-2 text-sm font-medium text-gray-500">
                    <Filter className="w-4 h-4" /> Lọc theo:
                </div>
                {Object.entries(STATUS_CONFIG).map(([key, config]) => {
                    const Icon = config.icon;
                    return (
                        <button
                            key={key}
                            onClick={() => handleFilterChange(key)}
                            className={`px-4 py-2 rounded-xl text-sm font-semibold transition-all flex items-center gap-2 ${
                                statusFilter === key
                                    ? 'bg-primary text-white shadow-md shadow-primary/20'
                                    : 'bg-gray-50 text-gray-600 hover:bg-gray-100 border border-gray-200'
                            }`}
                        >
                            {Icon && <Icon className="w-4 h-4" />}
                            {config.label}
                        </button>
                    );
                })}
            </div>

            {/* Data Table */}
            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden flex flex-col min-h-[500px]">
                <div className="overflow-x-auto">
                    <table className="w-full text-left border-collapse">
                        <thead>
                            <tr className="bg-gray-50/50 border-b border-gray-100 text-sm font-semibold text-gray-500 uppercase tracking-wider">
                                <th className="p-4 pl-6">ID & Sản phẩm</th>
                                <th className="p-4">Giá trị</th>
                                <th className="p-4">Người bán / Người mua</th>
                                <th className="p-4">Trạng thái</th>
                                <th className="p-4 text-right pr-6">Hành động</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-100">
                            {loading ? (
                                <tr>
                                    <td colSpan="5" className="p-12 text-center text-gray-500">
                                        <div className="flex flex-col items-center justify-center">
                                            <div className="w-10 h-10 border-4 border-primary/20 border-t-primary rounded-full animate-spin mb-4"></div>
                                            <p>Đang tải dữ liệu...</p>
                                        </div>
                                    </td>
                                </tr>
                            ) : orders.length === 0 ? (
                                <tr>
                                    <td colSpan="5" className="p-12 text-center">
                                        <div className="w-20 h-20 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-4">
                                            <ShoppingBag className="w-10 h-10 text-gray-300" />
                                        </div>
                                        <h3 className="text-lg font-bold text-gray-900 mb-1">Không có đơn hàng nào</h3>
                                        <p className="text-gray-500">Chưa có đơn hàng nào khớp với bộ lọc hiện tại.</p>
                                    </td>
                                </tr>
                            ) : (
                                orders.map(order => (
                                    <tr key={order.id} className="hover:bg-gray-50/50 transition-colors group">
                                        <td className="p-4 pl-6">
                                            <div className="flex items-center gap-3">
                                                <div className="w-12 h-12 rounded-lg bg-gray-100 overflow-hidden border border-gray-200 flex-shrink-0">
                                                    {order.productImageUrl ? (
                                                        <img src={order.productImageUrl} alt={order.productName} className="w-full h-full object-cover" />
                                                    ) : (
                                                        <ShoppingBag className="w-6 h-6 m-3 text-gray-400" />
                                                    )}
                                                </div>
                                                <div>
                                                    <p className="font-semibold text-gray-900 line-clamp-1" title={order.productName}>{order.productName}</p>
                                                    <p className="text-xs text-gray-500 font-mono mt-0.5">#{order.id.substring(0, 8)}...</p>
                                                    <p className="text-xs text-gray-400 mt-1">{new Date(order.createdAt).toLocaleDateString('vi-VN')}</p>
                                                </div>
                                            </div>
                                        </td>
                                        <td className="p-4">
                                            <p className="font-bold text-primary">{order.totalAmount?.toLocaleString('vi-VN')} đ</p>
                                            <p className="text-xs text-gray-500 mt-0.5">Cọc: {order.depositAmount?.toLocaleString('vi-VN')} đ</p>
                                        </td>
                                        <td className="p-4">
                                            <div className="text-sm">
                                                <p className="flex items-center gap-1" title={order.sellerId}>
                                                    <span className="text-gray-500">Bán:</span> 
                                                    <span className="font-medium text-gray-900">{order.sellerName}</span>
                                                    {order.sellerId && <span className="text-[10px] text-gray-400 font-mono ml-1">#{order.sellerId.substring(0, 6)}</span>}
                                                </p>
                                                <p className="mt-1 flex items-center gap-1" title={order.buyerId}>
                                                    <span className="text-gray-500">Mua:</span> 
                                                    <span className="font-medium text-gray-900">{order.buyerName}</span>
                                                    {order.buyerId && <span className="text-[10px] text-gray-400 font-mono ml-1">#{order.buyerId.substring(0, 6)}</span>}
                                                </p>
                                            </div>
                                        </td>
                                        <td className="p-4">
                                            {(() => {
                                                const config = STATUS_CONFIG[order.status] || STATUS_CONFIG['ALL'];
                                                const Icon = config.icon;
                                                return (
                                                    <div className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg border text-xs font-bold ${config.bg} ${config.color} ${config.border}`}>
                                                        {Icon && <Icon className="w-3.5 h-3.5" />}
                                                        {config.label}
                                                    </div>
                                                );
                                            })()}
                                        </td>
                                        <td className="p-4 pr-6 text-right">
                                            <button 
                                                onClick={() => setSelectedOrder(order)}
                                                className="px-4 py-2 bg-white border border-gray-200 text-gray-700 rounded-xl hover:bg-primary hover:text-white hover:border-primary transition-colors font-medium shadow-sm text-sm"
                                            >
                                                Chi tiết
                                            </button>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>

                {/* Pagination */}
                {!loading && orders.length > 0 && (
                    <div className="mt-auto px-6 py-4 border-t border-gray-100 flex items-center justify-between bg-gray-50/30">
                        <p className="text-sm text-gray-500">
                            Hiển thị <span className="font-bold text-gray-900">{orders.length}</span> / <span className="font-bold text-gray-900">{pagination.totalElements}</span> đơn hàng
                        </p>
                        <div className="flex items-center gap-2">
                            <button 
                                onClick={() => handlePageChange(pagination.currentPage - 1)}
                                disabled={pagination.currentPage === 1}
                                className="p-2 rounded-lg border border-gray-200 text-gray-600 hover:bg-white hover:border-gray-300 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
                            >
                                <ChevronLeft className="w-5 h-5" />
                            </button>
                            <span className="px-4 text-sm font-semibold text-gray-700">
                                Trang {pagination.currentPage} / {pagination.totalPages}
                            </span>
                            <button 
                                onClick={() => handlePageChange(pagination.currentPage + 1)}
                                disabled={pagination.currentPage === pagination.totalPages}
                                className="p-2 rounded-lg border border-gray-200 text-gray-600 hover:bg-white hover:border-gray-300 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
                            >
                                <ChevronRight className="w-5 h-5" />
                            </button>
                        </div>
                    </div>
                )}
            </div>

            {/* Detail Modal */}
            {selectedOrder && (
                <AdminOrderDetailModal 
                    order={selectedOrder} 
                    onClose={() => setSelectedOrder(null)} 
                    onRefresh={() => fetchOrders(pagination.currentPage)}
                />
            )}
        </div>
    );
}
