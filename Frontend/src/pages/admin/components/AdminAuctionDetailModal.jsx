import React, { useState, useEffect } from 'react';
import { X, CheckCircle, XCircle, ShoppingBag, Info, Clock, Check, ChevronLeft, ChevronRight, Scale } from 'lucide-react';
import toast from 'react-hot-toast';
import { adminApi } from '@/features/admin/api';

export default function AdminAuctionDetailModal({ auctionId, onClose, onStatusChange }) {
    const [loading, setLoading] = useState(true);
    const [detail, setDetail] = useState(null);
    const [currentImageIndex, setCurrentImageIndex] = useState(0);

    useEffect(() => {
        const fetchDetail = async () => {
            try {
                setLoading(true);
                const res = await adminApi.getAuctionDetail(auctionId);
                if (res.result) {
                    setDetail(res.result);
                }
            } catch (error) {
                toast.error("Không thể tải chi tiết phiên đấu giá");
                onClose();
            } finally {
                setLoading(false);
            }
        };
        fetchDetail();
    }, [auctionId, onClose]);

    const formatCurrency = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val || 0);

    if (loading) {
        return (
            <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm">
                <div className="bg-white p-6 rounded-2xl flex flex-col items-center">
                    <div className="w-10 h-10 border-4 border-primary/20 border-t-primary rounded-full animate-spin mb-4"></div>
                    <p className="font-medium text-gray-700">Đang tải dữ liệu...</p>
                </div>
            </div>
        );
    }

    if (!detail) return null;

    const images = detail.productImages || [];

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" onClick={onClose}></div>
            
            <div className="bg-white rounded-2xl shadow-2xl w-full max-w-4xl max-h-[90vh] overflow-hidden flex flex-col relative animate-fade-in-up">
                {/* Header */}
                <div className="px-6 py-4 border-b border-gray-100 flex items-center justify-between bg-gray-50/50">
                    <h3 className="text-lg font-bold text-gray-900">Chi tiết Phiên đấu giá</h3>
                    <button onClick={onClose} className="text-gray-400 hover:text-gray-600 transition-colors">
                        ✕
                    </button>
                </div>

                {/* Body */}
                <div className="p-6 overflow-y-auto flex-1 bg-white">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                        {/* Cột trái: Hình ảnh */}
                        <div className="space-y-4">
                            <div className="relative aspect-square rounded-2xl overflow-hidden bg-gray-100 border border-gray-200">
                                {images.length > 0 ? (
                                    <>
                                        <img 
                                            src={images[currentImageIndex].url} 
                                            alt="Product" 
                                            className="w-full h-full object-cover"
                                        />
                                        {images.length > 1 && (
                                            <>
                                                <button 
                                                    onClick={() => setCurrentImageIndex(prev => (prev === 0 ? images.length - 1 : prev - 1))}
                                                    className="absolute left-2 top-1/2 -translate-y-1/2 w-8 h-8 rounded-full bg-white/80 hover:bg-white flex items-center justify-center shadow-sm text-gray-800 transition-colors"
                                                >
                                                    <ChevronLeft className="w-5 h-5" />
                                                </button>
                                                <button 
                                                    onClick={() => setCurrentImageIndex(prev => (prev === images.length - 1 ? 0 : prev + 1))}
                                                    className="absolute right-2 top-1/2 -translate-y-1/2 w-8 h-8 rounded-full bg-white/80 hover:bg-white flex items-center justify-center shadow-sm text-gray-800 transition-colors"
                                                >
                                                    <ChevronRight className="w-5 h-5" />
                                                </button>
                                                <div className="absolute bottom-3 left-1/2 -translate-x-1/2 flex gap-1.5">
                                                    {images.map((_, idx) => (
                                                        <div 
                                                            key={idx}
                                                            className={`w-2 h-2 rounded-full transition-all ${idx === currentImageIndex ? 'bg-primary w-4' : 'bg-white/60'}`}
                                                        />
                                                    ))}
                                                </div>
                                            </>
                                        )}
                                    </>
                                ) : (
                                    <div className="w-full h-full flex items-center justify-center text-gray-400">
                                        <ShoppingBag className="w-12 h-12" />
                                    </div>
                                )}
                            </div>
                        </div>

                        {/* Cột phải: Thông tin */}
                        <div className="space-y-6">
                            {/* Thông tin Phiên đấu giá */}
                            <div className="bg-gray-50 p-5 rounded-2xl border border-gray-100 space-y-4">
                                <h4 className="text-sm font-bold text-gray-900 uppercase tracking-wider flex items-center gap-2">
                                    <Scale className="w-4 h-4 text-primary" /> Thông tin Đấu giá
                                </h4>
                                
                                <div className="space-y-2">
                                    <div className="flex justify-between items-center text-sm">
                                        <span className="text-gray-500">Trạng thái:</span>
                                        <span className="font-semibold text-gray-900">{detail.status}</span>
                                    </div>
                                    <div className="flex justify-between items-center text-sm">
                                        <span className="text-gray-500">Giá khởi điểm:</span>
                                        <span className="font-semibold text-primary">{formatCurrency(detail.startPrice)}</span>
                                    </div>
                                    <div className="flex justify-between items-center text-sm">
                                        <span className="text-gray-500">Bước giá:</span>
                                        <span className="font-medium text-gray-900">{formatCurrency(detail.stepPrice)}</span>
                                    </div>
                                    <div className="flex justify-between items-center text-sm">
                                        <span className="text-gray-500">Tiền cọc:</span>
                                        <span className="font-medium text-gray-900">{formatCurrency(detail.depositAmount)}</span>
                                    </div>
                                    <div className="pt-2 border-t border-gray-200 flex justify-between items-center text-sm">
                                        <span className="text-gray-500">Thời gian bắt đầu:</span>
                                        <span className="font-medium text-gray-900">{detail.startTime}</span>
                                    </div>
                                    <div className="flex justify-between items-center text-sm">
                                        <span className="text-gray-500">Thời gian kết thúc:</span>
                                        <span className="font-medium text-gray-900">{detail.endTime}</span>
                                    </div>
                                </div>
                            </div>

                            {/* Thông tin Sản phẩm */}
                            <div className="space-y-4">
                                <h4 className="text-sm font-bold text-gray-900 uppercase tracking-wider flex items-center gap-2">
                                    <Info className="w-4 h-4 text-primary" /> Thông tin Sản phẩm
                                </h4>
                                
                                <div className="space-y-3">
                                    <div>
                                        <span className="text-sm text-gray-500 block mb-1">Tên sản phẩm</span>
                                        <span className="text-base font-semibold text-gray-900 block">{detail.productName}</span>
                                    </div>
                                    
                                    <div className="grid grid-cols-2 gap-4">
                                        <div className="bg-gray-50 p-3 rounded-xl border border-gray-100">
                                            <span className="text-xs text-gray-500 block">Tình trạng</span>
                                            <span className="text-sm font-medium text-gray-900">{detail.productCondition === 'NEW' ? 'Mới' : detail.productCondition === 'USED' ? 'Đã sử dụng' : detail.productCondition || 'Chưa cập nhật'}</span>
                                        </div>
                                        <div className="bg-gray-50 p-3 rounded-xl border border-gray-100">
                                            <span className="text-xs text-gray-500 block">Năm sản xuất</span>
                                            <span className="text-sm font-medium text-gray-900">{detail.productManufactureYear || 'Chưa cập nhật'}</span>
                                        </div>
                                    </div>

                                    <div>
                                        <span className="text-sm text-gray-500 block mb-1">Nguồn gốc / Xuất xứ</span>
                                        <span className="text-sm font-medium text-gray-900 block">{detail.productOrigin || 'Chưa cập nhật'}</span>
                                    </div>

                                    {detail.hasCertificate && detail.provenanceFileUrl && (
                                        <div className="bg-blue-50 text-blue-800 p-3 rounded-xl border border-blue-100 text-sm flex items-center justify-between">
                                            <div className="flex items-center gap-2">
                                                <CheckCircle className="w-4 h-4" />
                                                <span className="font-medium">Có tài liệu chứng minh</span>
                                            </div>
                                            <a href={detail.provenanceFileUrl} target="_blank" rel="noreferrer" className="text-blue-600 hover:underline font-medium">Xem tài liệu</a>
                                        </div>
                                    )}

                                    <div>
                                        <span className="text-sm text-gray-500 block mb-1">Mô tả</span>
                                        <div className="text-sm text-gray-700 bg-gray-50 p-3 rounded-xl border border-gray-100 max-h-32 overflow-y-auto whitespace-pre-wrap">
                                            {detail.productDescription || 'Không có mô tả.'}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Footer / Actions */}
                {detail.status === 'PENDING' && (
                    <div className="px-6 py-4 border-t border-gray-100 flex items-center justify-end gap-3 bg-gray-50/50">
                        <button 
                            onClick={() => onStatusChange(detail.id, 'CANCELLED')}
                            className="px-6 py-2.5 text-sm font-medium text-red-600 hover:bg-red-50 hover:text-red-700 bg-white border border-red-200 rounded-xl transition-colors flex items-center gap-2"
                        >
                            <XCircle className="w-4 h-4" />
                            Từ chối Phiên Đấu giá
                        </button>
                        <button 
                            onClick={() => onStatusChange(detail.id, 'APPROVED')}
                            className="px-6 py-2.5 text-sm font-medium text-white bg-green-600 hover:bg-green-700 rounded-xl transition-colors flex items-center gap-2 shadow-sm shadow-green-600/20"
                        >
                            <CheckCircle className="w-4 h-4" />
                            Duyệt Phiên Đấu giá
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
}
