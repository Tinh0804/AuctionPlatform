import React, { useEffect, useState, useCallback } from 'react';
import { Search, MoreVertical, Filter, UserX, UserCheck, Eye, Lock, Unlock, ShieldAlert, ShieldCheck, Edit2, Trash2 } from 'lucide-react';
import { adminApi } from '@/features/admin/api';
import { useToast } from '@/components/Elements/Toast';
import AdminEditUserModal from './components/AdminEditUserModal';

export default function AdminUsersPage() {
    const { toast } = useToast();
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [keyword, setKeyword] = useState('');
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const [selectedUser, setSelectedUser] = useState(null);
    const [detailLoading, setDetailLoading] = useState(false);
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [isViewModalOpen, setIsViewModalOpen] = useState(false);

    const fetchUsers = useCallback(async () => {
        try {
            setLoading(true);
            const res = await adminApi.getAllUsers({ keyword, page, size: 10 });
            if (res.result) {
                setUsers(res.result.content);
                setTotalPages(res.result.totalPages);
            }
        } catch (error) {
            toast.error("Không thể tải danh sách người dùng");
        } finally {
            setLoading(false);
        }
    }, [keyword, page]);

    useEffect(() => {
        const timeout = setTimeout(() => {
            fetchUsers();
        }, 500);
        return () => clearTimeout(timeout);
    }, [fetchUsers]);

    const handleViewDetail = async (id) => {
        try {
            setDetailLoading(true);
            setIsViewModalOpen(true);
            const res = await adminApi.getUserDetail(id);
            if (res.result) {
                setSelectedUser(res.result);
            }
        } catch (error) {
            toast({ title: "Lỗi", description: "Không thể tải chi tiết người dùng", type: "error" });
            setIsViewModalOpen(false);
        } finally {
            setDetailLoading(false);
        }
    };

    const handleToggleAccount = async (id) => {
        if (!window.confirm("Bạn có chắc chắn muốn thay đổi trạng thái đăng nhập của người dùng này?")) return;
        try {
            await adminApi.toggleUserStatus(id);
            toast.success("Đã thay đổi trạng thái tài khoản");
            if (selectedUser && selectedUser.id === id) {
                setSelectedUser(prev => ({
                    ...prev,
                    account: { ...prev.account, isActive: !prev.account.isActive }
                }));
            }
            fetchUsers();
        } catch (error) {
            toast.error(error.response?.data?.message || "Có lỗi xảy ra");
        }
    };

    const handleToggleWallet = async (id) => {
        if (!window.confirm("Bạn có chắc chắn muốn thay đổi trạng thái ví của người dùng này?")) return;
        try {
            await adminApi.toggleWalletStatus(id);
            toast.success("Đã thay đổi trạng thái ví");
            if (selectedUser && selectedUser.id === id) {
                setSelectedUser(prev => ({
                    ...prev,
                    wallet: { 
                        ...prev.wallet, 
                        status: prev.wallet.status === 'ACTIVE' ? 'FROZEN' : 'ACTIVE' 
                    }
                }));
            }
        } catch (error) {
            toast.error(error.response?.data?.message || "Có lỗi xảy ra");
        }
    };

    const handleVerification = async (id, status) => {
        try {
            await adminApi.updateVerificationStatus(id, status);
            toast({ title: "Thành công", description: "Cập nhật trạng thái định danh thành công", type: "success" });
            fetchUsers();
            if (selectedUser?.id === id) {
                handleViewDetail(id);
            }
        } catch (error) {
            toast({ title: "Lỗi", description: "Có lỗi xảy ra", type: "error" });
        }
    };

    const handleDeleteUser = async (id) => {
        if (!window.confirm("Bạn có chắc chắn muốn xóa người dùng này? Hành động này không thể hoàn tác.")) {
            return;
        }
        try {
            await adminApi.deleteUser(id);
            toast({ title: "Thành công", description: "Đã xóa người dùng thành công", type: "success" });
            setSelectedUser(null);
            fetchUsers();
        } catch (error) {
            toast({ 
                title: "Lỗi", 
                description: error.response?.data?.message || "Không thể xóa người dùng", 
                type: "error" 
            });
        }
    };

    return (
        <div className="space-y-6">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div>
                    <h2 className="text-2xl font-bold text-gray-800">Quản lý Người dùng</h2>
                    <p className="text-sm text-gray-500 mt-1">Quản lý tài khoản, trạng thái ví và xác minh danh tính</p>
                </div>
            </div>

            {/* Filters */}
            <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-100 flex flex-col sm:flex-row gap-4">
                <div className="relative flex-1">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                    <input 
                        type="text"
                        placeholder="Tìm kiếm theo tên, email, sđt, username..."
                        value={keyword}
                        onChange={(e) => {
                            setKeyword(e.target.value);
                            setPage(0);
                        }}
                        className="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all"
                    />
                </div>
            </div>

            {/* Table */}
            <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
                <div className="overflow-x-auto">
                    <table className="w-full text-left border-collapse">
                        <thead>
                            <tr className="bg-gray-50 border-b border-gray-100 text-sm font-medium text-gray-500">
                                <th className="px-6 py-4">Khách hàng</th>
                                <th className="px-6 py-4">Định danh (EKyc)</th>
                                <th className="px-6 py-4">Độ uy tín</th>
                                <th className="px-6 py-4">Trạng thái</th>
                                <th className="px-6 py-4 text-right">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-100">
                            {loading ? (
                                <tr>
                                    <td colSpan="5" className="px-6 py-8 text-center text-gray-400">
                                        <div className="flex justify-center items-center gap-2">
                                            <div className="w-5 h-5 border-2 border-primary border-t-transparent rounded-full animate-spin"></div>
                                            Đang tải dữ liệu...
                                        </div>
                                    </td>
                                </tr>
                            ) : users.length === 0 ? (
                                <tr>
                                    <td colSpan="5" className="px-6 py-8 text-center text-gray-500">
                                        Không tìm thấy người dùng nào
                                    </td>
                                </tr>
                            ) : (
                                users.map(user => (
                                    <tr key={user.id} className="hover:bg-gray-50/50 transition-colors">
                                        <td className="px-6 py-4">
                                            <div className="flex items-center gap-3">
                                                <div className="w-10 h-10 rounded-full bg-gray-200 flex-shrink-0 overflow-hidden">
                                                    {user.avatarImage ? (
                                                        <img src={user.avatarImage} alt={user.name} className="w-full h-full object-cover" />
                                                    ) : (
                                                        <div className="w-full h-full flex items-center justify-center font-bold text-gray-500 bg-gray-100">
                                                            {user.name?.charAt(0) || 'U'}
                                                        </div>
                                                    )}
                                                </div>
                                                <div>
                                                    <div className="font-medium text-gray-900">{user.name}</div>
                                                    <div className="text-sm text-gray-500">{user.email || user.phone}</div>
                                                </div>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4">
                                            {user.verificationStatus === 'VERIFIED' ? (
                                                <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-green-50 text-green-700 border border-green-200/50">
                                                    <ShieldCheck className="w-3.5 h-3.5" /> Đã xác minh
                                                </span>
                                            ) : user.verificationStatus === 'PENDING' ? (
                                                <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-amber-50 text-amber-700 border border-amber-200/50">
                                                    <ShieldAlert className="w-3.5 h-3.5" /> Chờ duyệt
                                                </span>
                                            ) : (
                                                <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-gray-50 text-gray-600 border border-gray-200/50">
                                                    Chưa xác minh
                                                </span>
                                            )}
                                        </td>
                                        <td className="px-6 py-4">
                                            <div className="flex items-center gap-2">
                                                <div className="w-16 h-2 bg-gray-100 rounded-full overflow-hidden">
                                                    <div 
                                                        className={`h-full rounded-full ${user.reputationScore >= 80 ? 'bg-green-500' : user.reputationScore >= 50 ? 'bg-amber-500' : 'bg-red-500'}`}
                                                        style={{ width: `${Math.min(user.reputationScore, 100)}%` }}
                                                    ></div>
                                                </div>
                                                <span className="text-sm font-medium text-gray-700">{user.reputationScore}</span>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4">
                                            {user.account?.isActive ? (
                                                <span className="inline-flex items-center gap-1 text-sm text-green-600 font-medium">
                                                    <Unlock className="w-4 h-4" /> Hoạt động
                                                </span>
                                            ) : (
                                                <span className="inline-flex items-center gap-1 text-sm text-red-600 font-medium">
                                                    <Lock className="w-4 h-4" /> Đã khoá
                                                </span>
                                            )}
                                        </td>
                                        <td className="px-6 py-4 text-right">
                                            <div className="flex items-center justify-end gap-2">
                                                <button 
                                                    onClick={() => handleViewDetail(user.id)}
                                                    className="p-2 text-gray-400 hover:text-primary hover:bg-primary/5 rounded-lg transition-colors"
                                                    title="Xem chi tiết"
                                                >
                                                    <Eye className="w-5 h-5" />
                                                </button>
                                                <button 
                                                    onClick={() => {
                                                        setSelectedUser(user);
                                                        setIsEditModalOpen(true);
                                                    }}
                                                    className="p-2 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                                                    title="Sửa thông tin"
                                                >
                                                    <Edit2 className="w-5 h-5" />
                                                </button>
                                                <button 
                                                    onClick={() => handleDeleteUser(user.id)}
                                                    className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                                                    title="Xóa người dùng"
                                                >
                                                    <Trash2 className="w-5 h-5" />
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>

                {/* Pagination */}
                {totalPages > 1 && (
                    <div className="px-6 py-4 border-t border-gray-100 flex items-center justify-between">
                        <span className="text-sm text-gray-500">
                            Trang {page + 1} / {totalPages}
                        </span>
                        <div className="flex items-center gap-2">
                            <button 
                                onClick={() => setPage(p => Math.max(0, p - 1))}
                                disabled={page === 0}
                                className="px-3 py-1.5 text-sm font-medium border border-gray-200 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                Trước
                            </button>
                            <button 
                                onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                                disabled={page === totalPages - 1}
                                className="px-3 py-1.5 text-sm font-medium border border-gray-200 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                Sau
                            </button>
                        </div>
                    </div>
                )}
            </div>

            {/* User Detail Modal */}
            {isViewModalOpen && selectedUser && (
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
                    <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" onClick={() => { setIsViewModalOpen(false); setSelectedUser(null); }}></div>
                    
                    <div className="bg-white rounded-2xl shadow-2xl w-full max-w-3xl max-h-[90vh] overflow-hidden flex flex-col relative animate-fade-in-up">
                        {/* Modal Header */}
                        <div className="px-6 py-4 border-b border-gray-100 flex items-center justify-between bg-gray-50/50">
                            <h3 className="text-lg font-bold text-gray-900">Hồ sơ người dùng</h3>
                            <button onClick={() => { setIsViewModalOpen(false); setSelectedUser(null); }} className="text-gray-400 hover:text-gray-600">✕</button>
                        </div>

                        {/* Modal Body */}
                        <div className="p-6 overflow-y-auto">
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                                {/* Cột Trái: Thông tin chung */}
                                <div className="space-y-6">
                                    <div className="flex items-center gap-4">
                                        <div className="w-16 h-16 rounded-full bg-gray-200 overflow-hidden border-2 border-white shadow-md">
                                            {selectedUser.avatarImage ? (
                                                <img src={selectedUser.avatarImage} alt="" className="w-full h-full object-cover" />
                                            ) : (
                                                <div className="w-full h-full flex items-center justify-center font-bold text-xl text-gray-500 bg-gray-100">
                                                    {selectedUser.name?.charAt(0) || 'U'}
                                                </div>
                                            )}
                                        </div>
                                        <div>
                                            <h4 className="text-xl font-bold text-gray-900">{selectedUser.name}</h4>
                                            <p className="text-sm text-gray-500">@{selectedUser.account?.username}</p>
                                        </div>
                                    </div>

                                    <div className="bg-gray-50 rounded-xl p-4 space-y-3">
                                        <div className="flex justify-between text-sm">
                                            <span className="text-gray-500">Email:</span>
                                            <span className="font-medium text-gray-900">{selectedUser.email || 'Chưa cập nhật'}</span>
                                        </div>
                                        <div className="flex justify-between text-sm">
                                            <span className="text-gray-500">Số ĐT:</span>
                                            <span className="font-medium text-gray-900">{selectedUser.phone || 'Chưa cập nhật'}</span>
                                        </div>
                                        <div className="flex justify-between text-sm">
                                            <span className="text-gray-500">CCCD:</span>
                                            <span className="font-medium text-gray-900">{selectedUser.identityCard || 'Chưa cập nhật'}</span>
                                        </div>
                                        <div className="flex justify-between text-sm">
                                            <span className="text-gray-500">Ngày sinh:</span>
                                            <span className="font-medium text-gray-900">{selectedUser.dob || 'Chưa cập nhật'}</span>
                                        </div>
                                        <div className="flex justify-between text-sm">
                                            <span className="text-gray-500">Độ uy tín:</span>
                                            <span className="font-medium text-gray-900">{selectedUser.reputationScore} điểm</span>
                                        </div>
                                    </div>

                                    {/* Action Khoá Tài khoản */}
                                    <div className="pt-2">
                                        <button 
                                            onClick={() => handleToggleAccount(selectedUser.id)}
                                            className={`w-full py-2.5 rounded-xl font-medium text-sm flex items-center justify-center gap-2 transition-all ${
                                                selectedUser.account?.isActive 
                                                    ? 'bg-red-50 text-red-600 hover:bg-red-100 border border-red-100'
                                                    : 'bg-green-50 text-green-600 hover:bg-green-100 border border-green-100'
                                            }`}
                                        >
                                            {selectedUser.account?.isActive ? (
                                                <><UserX className="w-4 h-4" /> Khoá Tài Khoản Đăng Nhập</>
                                            ) : (
                                                <><UserCheck className="w-4 h-4" /> Mở Khoá Tài Khoản</>
                                            )}
                                        </button>
                                    </div>
                                </div>

                                {/* Cột Phải: Ví & EKyc */}
                                <div className="space-y-6">
                                    {/* Ví điện tử */}
                                    <div>
                                        <h4 className="text-sm font-bold text-gray-900 uppercase tracking-wider mb-3">Ví điện tử</h4>
                                        {selectedUser.wallet ? (
                                            <div className="bg-gradient-to-br from-gray-900 to-gray-800 rounded-xl p-5 text-white shadow-lg relative overflow-hidden">
                                                <div className="absolute top-0 right-0 w-32 h-32 bg-white/5 rounded-full -translate-y-1/2 translate-x-1/3 blur-2xl"></div>
                                                
                                                <div className="flex justify-between items-start mb-4 relative z-10">
                                                    <div>
                                                        <p className="text-gray-400 text-xs mb-1">Số dư khả dụng</p>
                                                        <p className="text-2xl font-bold font-mono">
                                                            {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(selectedUser.wallet.available_balance || 0)}
                                                        </p>
                                                    </div>
                                                    <span className={`px-2.5 py-1 rounded-lg text-xs font-medium border ${
                                                        selectedUser.wallet.status === 'ACTIVE' 
                                                            ? 'bg-green-500/20 text-green-300 border-green-500/30'
                                                            : 'bg-red-500/20 text-red-300 border-red-500/30'
                                                    }`}>
                                                        {selectedUser.wallet.status === 'ACTIVE' ? 'Đang H.động' : 'Bị Đóng Băng'}
                                                    </span>
                                                </div>

                                                <div className="flex justify-between items-center text-sm relative z-10 pt-4 border-t border-gray-700/50">
                                                    <span className="text-gray-400">Đóng băng: {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(selectedUser.wallet.frozen_balance || 0)}</span>
                                                    
                                                    <button 
                                                        onClick={() => handleToggleWallet(selectedUser.id)}
                                                        className="text-xs font-medium text-white hover:text-primary transition-colors underline"
                                                    >
                                                        {selectedUser.wallet.status === 'ACTIVE' ? 'Khoá Ví' : 'Mở Khoá Ví'}
                                                    </button>
                                                </div>
                                            </div>
                                        ) : (
                                            <div className="bg-gray-50 border border-gray-100 rounded-xl p-6 text-center text-gray-500 text-sm">
                                                Người dùng chưa kích hoạt ví
                                            </div>
                                        )}
                                    </div>

                                    {/* EKyc */}
                                    <div>
                                        <div className="flex items-center justify-between mb-3">
                                            <h4 className="text-sm font-bold text-gray-900 uppercase tracking-wider">Xác minh danh tính (EKyc)</h4>
                                            <span className={`text-xs font-bold ${
                                                selectedUser.verificationStatus === 'VERIFIED' ? 'text-green-600' :
                                                selectedUser.verificationStatus === 'PENDING' ? 'text-amber-600' :
                                                'text-gray-500'
                                            }`}>
                                                {selectedUser.verificationStatus}
                                            </span>
                                        </div>
                                        
                                        <div className="bg-gray-50 rounded-xl p-4 space-y-4">
                                            {selectedUser.identityFrontImage && selectedUser.identityBackImage ? (
                                                <div className="grid grid-cols-2 gap-3">
                                                    <div>
                                                        <p className="text-xs text-gray-500 mb-1">Mặt trước</p>
                                                        <img src={selectedUser.identityFrontImage} alt="Mặt trước" className="w-full h-24 object-cover rounded-lg border border-gray-200" />
                                                    </div>
                                                    <div>
                                                        <p className="text-xs text-gray-500 mb-1">Mặt sau</p>
                                                        <img src={selectedUser.identityBackImage} alt="Mặt sau" className="w-full h-24 object-cover rounded-lg border border-gray-200" />
                                                    </div>
                                                </div>
                                            ) : (
                                                <p className="text-sm text-gray-500 italic text-center py-4">Chưa tải lên giấy tờ</p>
                                            )}

                                            {/* Action Duyệt EKyc */}
                                            {selectedUser.verificationStatus === 'PENDING' && (
                                                <div className="flex gap-2 pt-2 border-t border-gray-200">
                                                    <button 
                                                        onClick={() => handleVerification(selectedUser.id, 'VERIFIED')}
                                                        className="flex-1 py-2 bg-green-600 hover:bg-green-700 text-white text-sm font-medium rounded-lg transition-colors"
                                                    >
                                                        Duyệt
                                                    </button>
                                                    <button 
                                                        onClick={() => handleVerification(selectedUser.id, 'REJECTED')}
                                                        className="flex-1 py-2 bg-red-50 hover:bg-red-100 text-red-600 text-sm font-medium rounded-lg transition-colors"
                                                    >
                                                        Từ chối
                                                    </button>
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {isEditModalOpen && (
                <AdminEditUserModal 
                    user={selectedUser} 
                    onClose={() => {
                        setIsEditModalOpen(false);
                        setSelectedUser(null);
                    }} 
                    onSuccess={() => {
                        setIsEditModalOpen(false);
                        setSelectedUser(null);
                        fetchUsers();
                    }}
                />
            )}
        </div>
    );
}
