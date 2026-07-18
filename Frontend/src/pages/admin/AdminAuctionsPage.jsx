import React, { useState, useEffect, useCallback } from 'react';
import { Scale, Search, Eye, Filter, CheckCircle, XCircle, Clock } from 'lucide-react';
import toast from 'react-hot-toast';
import { getAllAuctions, getAuctionDetail, updateAuctionStatus } from '@/features/admin/api';
import AdminAuctionDetailModal from './components/AdminAuctionDetailModal';
import AdminEditAuctionModal from './components/AdminEditAuctionModal';
import { deleteAuction } from '@/features/admin/api';
import { Edit2, Trash2 } from 'lucide-react';

export default function AdminAuctionsPage() {
    const [auctions, setAuctions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [statusFilter, setStatusFilter] = useState('');
    
    // Pagination states
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const [pageSize] = useState(12);

    // Modal states
    const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [selectedAuctionForEdit, setSelectedAuctionForEdit] = useState(null);
    const [selectedAuctionId, setSelectedAuctionId] = useState(null);

    const fetchAuctions = useCallback(async () => {
        try {
            setLoading(true);
            const params = { page, size: pageSize };
            if (statusFilter) params.status = statusFilter;
            
            const res = await getAllAuctions(params);
            if (res.result) {
                setAuctions(res.result.content);
                setTotalPages(res.result.totalPages);
            }
        } catch (error) {
            toast.error("Không thể tải danh sách phiên đấu giá");
        } finally {
            setLoading(false);
        }
    }, [page, pageSize, statusFilter]);

    useEffect(() => {
        fetchAuctions();
    }, [fetchAuctions]);

    const handleViewDetail = (id) => {
        setSelectedAuctionId(id);
        setIsDetailModalOpen(true);
    };

    const handleEdit = async (auction) => {
        try {
            const res = await getAuctionDetail(auction.id);
            if (res.result) {
                setSelectedAuctionForEdit(res.result);
                setIsEditModalOpen(true);
            }
        } catch (error) {
            toast.error("Không thể lấy thông tin để sửa");
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm("Bạn có chắc chắn muốn xóa phiên đấu giá này?")) return;
        try {
            await deleteAuction(id);
            toast.success("Đã xóa phiên đấu giá");
            fetchAuctions();
        } catch (error) {
            toast.error(error.response?.data?.message || "Không thể xóa");
        }
    };

    const handleStatusChange = async (id, status) => {
        if (!window.confirm(`Bạn có chắc chắn muốn ${status === 'APPROVED' ? 'duyệt' : 'từ chối'} phiên đấu giá này?`)) return;
        
        try {
            await updateAuctionStatus(id, status);
            toast.success(`Đã ${status === 'APPROVED' ? 'duyệt' : 'từ chối'} phiên đấu giá`);
            fetchAuctions();
            setIsDetailModalOpen(false);
        } catch (error) {
            toast.error(error.response?.data?.message || "Thao tác thất bại");
        }
    };

    const filteredAuctions = auctions.filter(a => 
        a.productName?.toLowerCase().includes(searchTerm.toLowerCase()) || 
        a.categoryName?.toLowerCase().includes(searchTerm.toLowerCase())
    );

    const getStatusBadge = (status) => {
        switch (status) {
            case 'PENDING':
                return <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium bg-amber-100 text-amber-800 border border-amber-200"><Clock className="w-3.5 h-3.5" /> Chờ duyệt</span>;
            case 'APPROVED':
                return <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800 border border-green-200"><CheckCircle className="w-3.5 h-3.5" /> Đã duyệt</span>;
            case 'CANCELLED':
                return <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium bg-red-100 text-red-800 border border-red-200"><XCircle className="w-3.5 h-3.5" /> Bị từ chối</span>;
            case 'IN_AUCTION':
                return <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800 border border-blue-200"><Scale className="w-3.5 h-3.5" /> Đang đấu giá</span>;
            case 'SOLD':
                return <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium bg-purple-100 text-purple-800 border border-purple-200"><CheckCircle className="w-3.5 h-3.5" /> Đã bán</span>;
            default:
                return <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium bg-gray-100 text-gray-800 border border-gray-200">{status}</span>;
        }
    };

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h2 className="text-2xl font-bold text-gray-900 tracking-tight">Quản lý Đấu giá</h2>
                    <p className="text-sm text-gray-500 mt-1">
                        Duyệt các phiên đấu giá và xem chi tiết sản phẩm
                    </p>
                </div>
            </div>

            {/* Filter Section */}
            <div className="bg-white p-4 rounded-2xl border border-gray-100 shadow-sm flex items-center justify-between gap-4">
                <div className="flex-1 max-w-md relative">
                    <Search className="w-5 h-5 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                    <input 
                        type="text" 
                        placeholder="Tìm kiếm sản phẩm, danh mục..." 
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
                        <option value="PENDING">Chờ duyệt</option>
                        <option value="APPROVED">Đã duyệt</option>
                        <option value="CANCELLED">Bị từ chối</option>
                        <option value="IN_AUCTION">Đang đấu giá</option>
                        <option value="SOLD">Đã bán</option>
                    </select>
                </div>
            </div>

            {/* Auctions Table */}
            <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
                <div className="overflow-x-auto">
                    <table className="w-full">
                        <thead>
                            <tr className="bg-gray-50/50 border-b border-gray-100">
                                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Sản phẩm</th>
                                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Danh mục</th>
                                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Trạng thái</th>
                                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Giá hiện tại</th>
                                <th className="px-6 py-4 text-right text-xs font-semibold text-gray-500 uppercase tracking-wider">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-100">
                            {loading ? (
                                <tr>
                                    <td colSpan="5" className="px-6 py-12 text-center text-gray-500">
                                        <div className="flex flex-col items-center justify-center">
                                            <div className="w-8 h-8 border-4 border-primary/20 border-t-primary rounded-full animate-spin mb-4"></div>
                                            Đang tải dữ liệu...
                                        </div>
                                    </td>
                                </tr>
                            ) : filteredAuctions.length === 0 ? (
                                <tr>
                                    <td colSpan="5" className="px-6 py-12 text-center text-gray-500">
                                        <Scale className="w-12 h-12 text-gray-300 mx-auto mb-3" />
                                        <p className="text-base font-medium text-gray-900">Không có dữ liệu</p>
                                    </td>
                                </tr>
                            ) : (
                                filteredAuctions.map(auction => (
                                    <tr key={auction.id} className="hover:bg-gray-50/50 transition-colors">
                                        <td className="px-6 py-4">
                                            <div className="flex items-center gap-3">
                                                <div className="w-10 h-10 rounded-xl bg-gray-100 overflow-hidden flex-shrink-0">
                                                    {auction.coverImage ? (
                                                        <img src={auction.coverImage} alt={auction.productName} className="w-full h-full object-cover" />
                                                    ) : (
                                                        <div className="w-full h-full flex items-center justify-center text-gray-400">
                                                            <ShoppingBag className="w-5 h-5" />
                                                        </div>
                                                    )}
                                                </div>
                                                <span className="font-semibold text-gray-900 line-clamp-1">{auction.productName}</span>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4">
                                            <span className="text-sm text-gray-600">{auction.categoryName}</span>
                                        </td>
                                        <td className="px-6 py-4">
                                            {getStatusBadge(auction.status)}
                                        </td>
                                        <td className="px-6 py-4">
                                            <span className="font-semibold text-primary">{new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(auction.currentPrice)}</span>
                                        </td>
                                        <td className="px-6 py-4 text-right">
                                            <div className="flex items-center justify-end gap-2">
                                                {auction.status === 'PENDING' && (
                                                    <>
                                                        <button 
                                                            onClick={() => handleStatusChange(auction.id, 'APPROVED')}
                                                            className="p-2 text-gray-400 hover:text-green-600 hover:bg-green-50 rounded-lg transition-colors"
                                                            title="Duyệt"
                                                        >
                                                            <CheckCircle className="w-5 h-5" />
                                                        </button>
                                                        <button 
                                                            onClick={() => handleStatusChange(auction.id, 'CANCELLED')}
                                                            className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                                                            title="Từ chối"
                                                        >
                                                            <XCircle className="w-5 h-5" />
                                                        </button>
                                                    </>
                                                )}
                                                <button 
                                                    onClick={() => handleEdit(auction)}
                                                    className="p-2 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                                                    title="Sửa"
                                                >
                                                    <Edit2 className="w-5 h-5" />
                                                </button>
                                                <button 
                                                    onClick={() => handleDelete(auction.id)}
                                                    className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                                                    title="Xóa"
                                                >
                                                    <Trash2 className="w-5 h-5" />
                                                </button>
                                                <button 
                                                    onClick={() => handleViewDetail(auction.id)}
                                                    className="p-2 text-gray-400 hover:text-primary hover:bg-primary/5 rounded-lg transition-colors"
                                                    title="Xem chi tiết"
                                                >
                                                    <Eye className="w-5 h-5" />
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
                <div className="flex items-center justify-center gap-2">
                    <button
                        onClick={() => setPage(p => Math.max(0, p - 1))}
                        disabled={page === 0}
                        className="px-4 py-2 border border-gray-200 rounded-xl hover:bg-gray-50 disabled:opacity-50 transition-colors text-sm font-medium"
                    >
                        Trước
                    </button>
                    <span className="text-sm text-gray-600">
                        Trang {page + 1} / {totalPages}
                    </span>
                    <button
                        onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                        disabled={page === totalPages - 1}
                        className="px-4 py-2 border border-gray-200 rounded-xl hover:bg-gray-50 disabled:opacity-50 transition-colors text-sm font-medium"
                    >
                        Sau
                    </button>
                </div>
            )}

            {/* Detail Modal */}
            {isEditModalOpen && selectedAuctionForEdit && (
                <AdminEditAuctionModal 
                    auctionDetail={selectedAuctionForEdit}
                    onClose={() => setIsEditModalOpen(false)}
                    onSuccess={() => {
                        setIsEditModalOpen(false);
                        fetchAuctions();
                    }}
                />
            )}

            {isDetailModalOpen && selectedAuctionId && (
                <AdminAuctionDetailModal 
                    auctionId={selectedAuctionId} 
                    onClose={() => setIsDetailModalOpen(false)} 
                    onStatusChange={handleStatusChange}
                />
            )}
        </div>
    );
}
