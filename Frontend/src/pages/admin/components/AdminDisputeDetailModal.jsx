import React, { useState, useEffect } from 'react';
import { X, ShieldAlert, CheckCircle, Clock, Save, Image as ImageIcon, ExternalLink, Gavel } from 'lucide-react';
import toast from 'react-hot-toast';
import { adminApi } from '@/features/admin/api';

export default function AdminDisputeDetailModal({ disputeId, onClose, onSuccess }) {
    const [dispute, setDispute] = useState(null);
    const [loading, setLoading] = useState(true);
    
    // Resolution form states
    const [outcome, setOutcome] = useState('BUYER_WIN');
    const [resolution, setResolution] = useState('');
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        const fetchDetail = async () => {
            try {
                const res = await adminApi.getDisputeDetail(disputeId);
                if (res.result) {
                    setDispute(res.result);
                }
            } catch (error) {
                toast.error("Không thể tải thông tin khiếu nại");
                onClose();
            } finally {
                setLoading(false);
            }
        };
        fetchDetail();
    }, [disputeId, onClose]);

    const handleResolve = async (e) => {
        e.preventDefault();
        if (!resolution.trim()) {
            toast.error("Vui lòng nhập lý do giải quyết");
            return;
        }

        if (!window.confirm("Sau khi phán quyết, tiền sẽ được hoàn lại hoặc chuyển cho người bán tương ứng. Bạn có chắc chắn?")) {
            return;
        }

        try {
            setSubmitting(true);
            await adminApi.resolveDispute(disputeId, { outcome, resolution });
            toast.success("Đã chốt phương án giải quyết!");
            onSuccess();
        } catch (error) {
            toast.error(error.response?.data?.message || "Lỗi khi giải quyết khiếu nại");
        } finally {
            setSubmitting(false);
        }
    };

    if (loading) {
        return (
            <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-gray-900/50 backdrop-blur-sm">
                <div className="bg-white rounded-2xl p-8 flex flex-col items-center">
                    <div className="w-8 h-8 border-4 border-primary/20 border-t-primary rounded-full animate-spin mb-4"></div>
                    <p className="text-gray-500">Đang tải chi tiết...</p>
                </div>
            </div>
        );
    }

    if (!dispute) return null;

    const isResolved = dispute.status === 'RESOLVED' || dispute.status === 'CLOSED';

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-6 bg-gray-900/50 backdrop-blur-sm overflow-y-auto">
            <div className="bg-white rounded-2xl shadow-xl w-full max-w-4xl max-h-[90vh] flex flex-col animate-fade-in relative my-auto">
                {/* Header */}
                <div className="px-6 py-4 border-b border-gray-100 flex items-center justify-between sticky top-0 bg-white/95 backdrop-blur z-10 rounded-t-2xl">
                    <div className="flex items-center gap-3">
                        <div className={`w-10 h-10 rounded-xl flex items-center justify-center ${isResolved ? 'bg-green-100 text-green-600' : 'bg-red-100 text-red-600'}`}>
                            {isResolved ? <CheckCircle className="w-5 h-5" /> : <ShieldAlert className="w-5 h-5" />}
                        </div>
                        <div>
                            <h3 className="text-lg font-bold text-gray-900">Chi tiết Khiếu nại</h3>
                            <p className="text-xs text-gray-500 font-mono">ID: {dispute.id}</p>
                        </div>
                    </div>
                    <button 
                        onClick={onClose}
                        className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-xl transition-colors"
                    >
                        <X className="w-5 h-5" />
                    </button>
                </div>

                {/* Content */}
                <div className="p-6 overflow-y-auto custom-scrollbar">
                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                        {/* Cột trái: Thông tin khiếu nại */}
                        <div className="space-y-6">
                            {/* Product Info */}
                            <div>
                                <h4 className="text-sm font-semibold text-gray-900 mb-3 uppercase tracking-wider">Thông tin Sản phẩm</h4>
                                <div className="bg-gray-50 rounded-xl p-4 flex gap-4 items-start border border-gray-100">
                                    <div className="w-16 h-16 rounded-lg bg-gray-200 overflow-hidden shrink-0">
                                        {dispute.productImageUrl ? (
                                            <img src={dispute.productImageUrl} alt={dispute.productName} className="w-full h-full object-cover" />
                                        ) : (
                                            <ImageIcon className="w-full h-full p-4 text-gray-400" />
                                        )}
                                    
                                    </div>
                                    <div>
                                        <p className="font-semibold text-gray-900 line-clamp-2">{dispute.productName}</p>
                                        <p className="text-sm text-gray-500 mt-1">Mã đơn: <span className="font-mono text-gray-900">{dispute.orderId}</span></p>
                                        <p className="text-sm text-gray-500 mt-1">Giá trị: <span className="font-semibold text-primary">{new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(dispute.orderAmount || 0)}</span></p>
                                    </div>
                                </div>
                            </div>

                            {/* Users Info */}
                            <div className="grid grid-cols-2 gap-4">
                                <div className="bg-blue-50/50 rounded-xl p-4 border border-blue-100 overflow-hidden">
                                    <p className="text-xs font-semibold text-blue-600 uppercase tracking-wider mb-1 whitespace-nowrap">Người khiếu nại</p>
                                    <p className="font-medium text-gray-900 truncate" title={dispute.claimantName}>{dispute.claimantName}</p>
                                </div>
                                <div className="bg-orange-50/50 rounded-xl p-4 border border-orange-100 overflow-hidden">
                                    <p className="text-xs font-semibold text-orange-600 uppercase tracking-wider mb-1 whitespace-nowrap">Người bị khiếu nại</p>
                                    <p className="font-medium text-gray-900 truncate" title={dispute.claimantName === dispute.buyerName ? dispute.sellerName : dispute.buyerName}>
                                        {dispute.claimantName === dispute.buyerName ? dispute.sellerName : dispute.buyerName} 
                                        <span className="text-xs text-gray-500 ml-1 whitespace-nowrap">({dispute.claimantName === dispute.buyerName ? 'Người bán' : 'Người mua'})</span>
                                    </p>
                                </div>
                            </div>

                            {/* Complaint Details */}
                            <div>
                                <h4 className="text-sm font-semibold text-gray-900 mb-3 uppercase tracking-wider">Nội dung khiếu nại</h4>
                                <div className="bg-white rounded-xl p-4 border border-gray-200 shadow-sm">
                                    <p className="font-medium text-gray-900 mb-2 border-b border-gray-100 pb-2">Lý do: <span className="text-red-600">{dispute.reason}</span></p>
                                    <p className="text-sm text-gray-600 whitespace-pre-wrap">{dispute.description || 'Không có mô tả chi tiết.'}</p>
                                </div>
                            </div>

                            {/* Evidences */}
                            <div>
                                <h4 className="text-sm font-semibold text-gray-900 mb-3 uppercase tracking-wider flex items-center gap-2">
                                    Bằng chứng đính kèm 
                                    <span className="bg-gray-100 text-gray-600 py-0.5 px-2 rounded-full text-xs">{dispute.evidences?.length || 0}</span>
                                </h4>
                                {dispute.evidences && dispute.evidences.length > 0 ? (
                                    <div className="grid grid-cols-3 gap-2">
                                        {dispute.evidences.map((img, idx) => (
                                            <a key={idx} href={img.url} target="_blank" rel="noreferrer" className="block aspect-square rounded-lg overflow-hidden border border-gray-200 hover:border-primary transition-colors group relative">
                                                <img src={img.url} alt="Bằng chứng" className="w-full h-full object-cover" />
                                                <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 flex items-center justify-center transition-opacity">
                                                    <ExternalLink className="w-5 h-5 text-white" />
                                                </div>
                                            </a>
                                        ))}
                                    </div>
                                ) : (
                                    <div className="bg-gray-50 rounded-xl p-6 flex flex-col items-center justify-center border border-gray-200 border-dashed">
                                        <ImageIcon className="w-8 h-8 text-gray-300 mb-2" />
                                        <p className="text-sm text-gray-500">Không có hình ảnh bằng chứng</p>
                                    </div>
                                )}
                            </div>
                        </div>

                        {/* Cột phải: Form giải quyết */}
                        <div>
                            {isResolved ? (
                                <div className="bg-green-50 rounded-2xl p-6 border border-green-100 h-full">
                                    <div className="flex items-center gap-2 mb-4">
                                        <CheckCircle className="w-6 h-6 text-green-600" />
                                        <h4 className="text-lg font-bold text-green-900">Đã giải quyết</h4>
                                    </div>
                                    <div className="space-y-4">
                                        <div>
                                            <p className="text-sm text-green-800 font-medium mb-1">Người xử lý:</p>
                                            <p className="text-gray-900 font-semibold">{dispute.resolvedByName || 'Admin'}</p>
                                        </div>
                                        <div>
                                            <p className="text-sm text-green-800 font-medium mb-1">Thời gian xử lý:</p>
                                            <p className="text-gray-900 font-semibold">{dispute.resolvedAt ? new Date(dispute.resolvedAt).toLocaleString('vi-VN') : '-'}</p>
                                        </div>
                                        <div>
                                            <p className="text-sm text-green-800 font-medium mb-1">Ghi chú phán quyết:</p>
                                            <div className="bg-white p-4 rounded-xl border border-green-200 text-gray-700 text-sm whitespace-pre-wrap shadow-sm">
                                                {dispute.resolution}
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            ) : (
                                <div className="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden flex flex-col h-full">
                                    <div className="bg-gray-50 px-6 py-4 border-b border-gray-200">
                                        <h4 className="text-base font-bold text-gray-900 flex items-center gap-2">
                                            <Gavel className="w-5 h-5 text-primary" /> Phán quyết của Admin
                                        </h4>
                                    </div>
                                    <form onSubmit={handleResolve} className="p-6 flex-1 flex flex-col space-y-6">
                                        <div>
                                            <label className="block text-sm font-medium text-gray-700 mb-3">Kết quả giải quyết</label>
                                            <div className="grid grid-cols-2 gap-3">
                                                <label className={`cursor-pointer rounded-xl p-4 border-2 transition-all ${outcome === 'BUYER_WIN' ? 'border-primary bg-primary/5' : 'border-gray-200 hover:border-primary/50'}`}>
                                                    <input 
                                                        type="radio" 
                                                        name="outcome" 
                                                        value="BUYER_WIN"
                                                        checked={outcome === 'BUYER_WIN'}
                                                        onChange={(e) => setOutcome(e.target.value)}
                                                        className="sr-only"
                                                    />
                                                    <div className="font-semibold text-gray-900 mb-1 whitespace-nowrap">Bên Mua thắng</div>
                                                    <p className="text-xs text-gray-500 line-clamp-2">Hoàn tiền lại cho người mua</p>
                                                </label>
                                                <label className={`cursor-pointer rounded-xl p-4 border-2 transition-all ${outcome === 'SELLER_WIN' ? 'border-primary bg-primary/5' : 'border-gray-200 hover:border-primary/50'}`}>
                                                    <input 
                                                        type="radio" 
                                                        name="outcome" 
                                                        value="SELLER_WIN"
                                                        checked={outcome === 'SELLER_WIN'}
                                                        onChange={(e) => setOutcome(e.target.value)}
                                                        className="sr-only"
                                                    />
                                                    <div className="font-semibold text-gray-900 mb-1 whitespace-nowrap">Bên Bán thắng</div>
                                                    <p className="text-xs text-gray-500 line-clamp-2">Chuyển tiền cho người bán</p>
                                                </label>
                                            </div>
                                        </div>
                                        
                                        <div className="flex-1 flex flex-col">
                                            <label className="block text-sm font-medium text-gray-700 mb-2">Lời khai / Ghi chú giải quyết (Bắt buộc)</label>
                                            <textarea 
                                                value={resolution}
                                                onChange={(e) => setResolution(e.target.value)}
                                                placeholder="Nhập lý do chi tiết cho phán quyết này để các bên liên quan được rõ..."
                                                className="w-full flex-1 min-h-[150px] rounded-xl border border-gray-300 p-4 text-sm focus:ring-2 focus:ring-primary/20 focus:border-primary transition-colors resize-none"
                                                required
                                            ></textarea>
                                        </div>

                                        <div className="pt-4 border-t border-gray-100 flex gap-3">
                                            <button 
                                                type="button"
                                                onClick={onClose}
                                                className="flex-1 py-2.5 px-4 rounded-xl border border-gray-300 text-gray-700 font-semibold hover:bg-gray-50 transition-colors"
                                            >
                                                Hủy
                                            </button>
                                            <button 
                                                type="submit"
                                                disabled={submitting}
                                                className="flex-1 py-2.5 px-4 rounded-xl bg-primary text-white font-semibold hover:bg-primary-dark transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 shadow-sm shadow-primary/20"
                                            >
                                                {submitting ? (
                                                    <div className="w-5 h-5 border-2 border-white/20 border-t-white rounded-full animate-spin"></div>
                                                ) : (
                                                    <>
                                                        <Save className="w-5 h-5" /> Chốt phán quyết
                                                    </>
                                                )}
                                            </button>
                                        </div>
                                    </form>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
