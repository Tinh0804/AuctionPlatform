import React, { useState, useEffect, useCallback } from 'react';
import { Search, Filter, ShieldAlert, CheckCircle, Clock, Eye, XCircle } from 'lucide-react';
import toast from 'react-hot-toast';
import { adminApi } from '@/features/admin/api';
import AdminDisputeDetailModal from './components/AdminDisputeDetailModal';

export default function AdminDisputesPage() {
    const [disputes, setDisputes] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [statusFilter, setStatusFilter] = useState('');
    
    const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);
    const [selectedDisputeId, setSelectedDisputeId] = useState(null);

    const fetchDisputes = useCallback(async () => {
        try {
            setLoading(true);
            const res = await adminApi.getAllDisputes();
            if (res.result) {
                setDisputes(res.result);
            }
        } catch (error) {
            toast.error("Không thể tải danh sách khiếu nại");
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchDisputes();
    }, [fetchDisputes]);

    const handleViewDetail = (id) => {
        setSelectedDisputeId(id);
        setIsDetailModalOpen(true);
    };

    const handleResolutionSuccess = () => {
        setIsDetailModalOpen(false);
        fetchDisputes();
    };

    const filteredDisputes = disputes.filter(d => {
        const matchesSearch = 
            d.productName?.toLowerCase().includes(searchTerm.toLowerCase()) || 
            d.claimantName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
            d.sellerName?.toLowerCase().includes(searchTerm.toLowerCase());
        const matchesStatus = statusFilter ? d.status === statusFilter : true;
        return matchesSearch && matchesStatus;
    });

    const getStatusBadge = (status) => {
        switch (status) {
            case 'OPEN':
                return <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium bg-red-100 text-red-800 border border-red-200"><ShieldAlert className="w-3.5 h-3.5" /> Mới mở</span>;
            case 'UNDER_REVIEW':
                return <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium bg-amber-100 text-amber-800 border border-amber-200"><Clock className="w-3.5 h-3.5" /> Đang xem xét</span>;
            case 'RESOLVED':
                return <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800 border border-green-200"><CheckCircle className="w-3.5 h-3.5" /> Đã giải quyết</span>;
            case 'CLOSED':
                return <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium bg-gray-100 text-gray-800 border border-gray-200"><XCircle className="w-3.5 h-3.5" /> Đã đóng</span>;
            default:
                return <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium bg-gray-100 text-gray-800 border border-gray-200">{status}</span>;
        }
    };

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h2 className="text-2xl font-bold text-gray-900 tracking-tight">Quản lý Khiếu nại</h2>
                    <p className="text-sm text-gray-500 mt-1">
                        Xử lý các tranh chấp giữa người mua và người bán
                    </p>
                </div>
            </div>

            {/* Filter Section */}
            <div className="bg-white p-4 rounded-2xl border border-gray-100 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div className="flex-1 max-w-md relative">
                    <Search className="w-5 h-5 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                    <input 
                        type="text" 
                        placeholder="Tìm theo sản phẩm, người dùng..." 
                        value={searchTerm}
                        onChange={e => setSearchTerm(e.target.value)}
                        className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-gray-200 focus:ring-2 focus:ring-primary/20 focus:border-primary transition-colors bg-gray-50 focus:bg-white text-sm"
                    />
                </div>
                <div className="flex items-center gap-2">
                    <Filter className="w-5 h-5 text-gray-400" />
                    <select
                        value={statusFilter}
                        onChange={(e) => setStatusFilter(e.target.value)}
                        className="px-4 py-2.5 rounded-xl border border-gray-200 focus:ring-2 focus:ring-primary/20 focus:border-primary transition-colors bg-gray-50 focus:bg-white text-sm"
                    >
                        <option value="">Tất cả trạng thái</option>
                        <option value="OPEN">Mới mở</option>
                        <option value="UNDER_REVIEW">Đang xem xét</option>
                        <option value="RESOLVED">Đã giải quyết</option>
                        <option value="CLOSED">Đã đóng</option>
                    </select>
                </div>
            </div>

            {/* Table */}
            <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
                <div className="overflow-x-auto">
                    <table className="w-full">
                        <thead>
                            <tr className="bg-gray-50/50 border-b border-gray-100">
                                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Sản phẩm</th>
                                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider whitespace-nowrap">Người khiếu nại</th>
                                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Lý do</th>
                                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider whitespace-nowrap">Trạng thái</th>
                                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider whitespace-nowrap">Ngày tạo</th>
                                <th className="px-6 py-4 text-right text-xs font-semibold text-gray-500 uppercase tracking-wider whitespace-nowrap">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-100">
                            {loading ? (
                                <tr>
                                    <td colSpan="6" className="px-6 py-12 text-center text-gray-500">
                                        <div className="flex flex-col items-center justify-center">
                                            <div className="w-8 h-8 border-4 border-primary/20 border-t-primary rounded-full animate-spin mb-4"></div>
                                            Đang tải dữ liệu...
                                        </div>
                                    </td>
                                </tr>
                            ) : filteredDisputes.length === 0 ? (
                                <tr>
                                    <td colSpan="6" className="px-6 py-12 text-center text-gray-500">
                                        <ShieldAlert className="w-12 h-12 text-gray-300 mx-auto mb-3" />
                                        <p className="text-base font-medium text-gray-900">Không có khiếu nại nào</p>
                                    </td>
                                </tr>
                            ) : (
                                filteredDisputes.map(dispute => (
                                    <tr key={dispute.id} className="hover:bg-gray-50/50 transition-colors">
                                        <td className="px-6 py-4">
                                            <div className="flex items-center gap-3">
                                                <div className="w-10 h-10 rounded-xl bg-gray-100 overflow-hidden flex-shrink-0">
                                                    {dispute.productImageUrl ? (
                                                        <img src={dispute.productImageUrl} alt={dispute.productName} className="w-full h-full object-cover" />
                                                    ) : (
                                                        <div className="w-full h-full flex items-center justify-center text-gray-400">
                                                            <ShieldAlert className="w-5 h-5" />
                                                        </div>
                                                    )}
                                                </div>
                                                <div>
                                                    <span className="block font-semibold text-gray-900 line-clamp-1">{dispute.productName}</span>
                                                    <span className="text-xs text-gray-500">Mã đơn: {dispute.orderId?.substring(0,8)}...</span>
                                                </div>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <span className="text-sm font-medium text-gray-900">{dispute.claimantName}</span>
                                        </td>
                                        <td className="px-6 py-4 min-w-[200px]">
                                            <span className="text-sm text-gray-600 line-clamp-2" title={dispute.reason}>{dispute.reason}</span>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            {getStatusBadge(dispute.status)}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <span className="text-sm text-gray-600">
                                                {new Date(dispute.createdAt).toLocaleDateString('vi-VN')}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4 text-right whitespace-nowrap">
                                            <button 
                                                onClick={() => handleViewDetail(dispute.id)}
                                                className="p-2 text-gray-400 hover:text-primary hover:bg-primary/5 rounded-lg transition-colors inline-flex items-center gap-1 text-sm font-medium"
                                            >
                                                <Eye className="w-5 h-5" />
                                                <span>Xem</span>
                                            </button>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* Detail Modal */}
            {isDetailModalOpen && selectedDisputeId && (
                <AdminDisputeDetailModal 
                    disputeId={selectedDisputeId} 
                    onClose={() => setIsDetailModalOpen(false)} 
                    onSuccess={handleResolutionSuccess}
                />
            )}
        </div>
    );
}
