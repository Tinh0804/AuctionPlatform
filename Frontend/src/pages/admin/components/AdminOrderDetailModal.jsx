import { useState } from 'react';
import { X, ShoppingBag, CreditCard, Truck, AlertTriangle, CheckCircle, PackageX } from 'lucide-react';
import { adminApi } from '@/features/admin/api';
import toast from 'react-hot-toast';

const STATUS_COLORS = {
    'PENDING_PAYMENT': 'bg-yellow-100 text-yellow-800 border-yellow-200',
    'PAID': 'bg-blue-100 text-blue-800 border-blue-200',
    'SHIPPING': 'bg-purple-100 text-purple-800 border-purple-200',
    'MEETING_SCHEDULED': 'bg-indigo-100 text-indigo-800 border-indigo-200',
    'COMPLETED': 'bg-green-100 text-green-800 border-green-200',
    'CANCELLED': 'bg-red-100 text-red-800 border-red-200',
    'DISPUTED': 'bg-orange-100 text-orange-800 border-orange-200'
};

const STATUS_LABELS = {
    'PENDING_PAYMENT': 'Chờ thanh toán',
    'PAID': 'Đã thanh toán',
    'SHIPPING': 'Đang giao hàng',
    'MEETING_SCHEDULED': 'Đã hẹn gặp',
    'COMPLETED': 'Hoàn thành',
    'CANCELLED': 'Đã hủy',
    'DISPUTED': 'Đang khiếu nại'
};

export default function AdminOrderDetailModal({ order, onClose, onRefresh }) {
    const [submitting, setSubmitting] = useState(false);
    const [showCancelConfirm, setShowCancelConfirm] = useState(false);
    const [showPayConfirm, setShowPayConfirm] = useState(false);

    if (!order) return null;

    const handleCancelOrder = async () => {
        try {
            setSubmitting(true);
            await adminApi.cancelOrder(order.id);
            toast.success('Đã hủy đơn hàng thành công');
            onRefresh();
            onClose();
        } catch (error) {
            toast.error(error.response?.data?.message || 'Lỗi khi hủy đơn hàng');
        } finally {
            setSubmitting(false);
        }
    };

    const handleForcePay = async () => {
        try {
            setSubmitting(true);
            await adminApi.forcePayOrder(order.id);
            toast.success('Đã ép cập nhật trạng thái Đã Thanh Toán thành công');
            onRefresh();
            onClose();
        } catch (error) {
            toast.error(error.response?.data?.message || 'Lỗi khi cập nhật thanh toán');
        } finally {
            setSubmitting(false);
        }
    };

    const canCancel = order.status !== 'COMPLETED' && order.status !== 'CANCELLED';
    const canForcePay = order.status === 'PENDING_PAYMENT';

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-6">
            <div className="absolute inset-0 bg-gray-900/60 backdrop-blur-sm" onClick={onClose}></div>
            
            <div className="relative w-full max-w-4xl bg-white rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
                {/* Header */}
                <div className="px-6 py-4 border-b border-gray-100 flex items-center justify-between bg-gray-50/50">
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-xl bg-primary/10 flex items-center justify-center">
                            <ShoppingBag className="w-5 h-5 text-primary" />
                        </div>
                        <div>
                            <h3 className="text-xl font-bold text-gray-900">Chi tiết đơn hàng</h3>
                            <p className="text-sm text-gray-500 font-mono mt-0.5">ID: {order.id}</p>
                        </div>
                    </div>
                    <button 
                        onClick={onClose}
                        className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-xl transition-colors"
                    >
                        <X className="w-6 h-6" />
                    </button>
                </div>

                {/* Content */}
                <div className="flex-1 overflow-y-auto p-6">
                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                        
                        {/* Left Column: Product & Info */}
                        <div className="space-y-6">
                            {/* Status Banner */}
                            <div className={`px-4 py-3 rounded-xl border flex items-center gap-3 ${STATUS_COLORS[order.status]}`}>
                                {order.status === 'COMPLETED' ? <CheckCircle className="w-5 h-5" /> : 
                                 order.status === 'CANCELLED' ? <PackageX className="w-5 h-5" /> : 
                                 <AlertTriangle className="w-5 h-5" />}
                                <div>
                                    <p className="text-sm font-bold uppercase tracking-wider">{STATUS_LABELS[order.status]}</p>
                                    <p className="text-xs opacity-80 mt-0.5">
                                        Cập nhật lần cuối: {new Date(order.updatedAt).toLocaleString('vi-VN')}
                                    </p>
                                </div>
                            </div>

                            {/* Product Info */}
                            <div className="bg-gray-50 rounded-xl p-5 border border-gray-100">
                                <h4 className="text-sm font-bold text-gray-900 uppercase tracking-wider mb-4 flex items-center gap-2">
                                    <ShoppingBag className="w-4 h-4 text-gray-500" /> Sản phẩm
                                </h4>
                                <div className="flex gap-4">
                                    {order.productImageUrl ? (
                                        <img src={order.productImageUrl} alt={order.productName} className="w-20 h-20 rounded-lg object-cover border border-gray-200" />
                                    ) : (
                                        <div className="w-20 h-20 rounded-lg bg-gray-200 flex items-center justify-center border border-gray-300">
                                            <ShoppingBag className="w-8 h-8 text-gray-400" />
                                        </div>
                                    )}
                                    <div>
                                        <h5 className="font-semibold text-gray-900">{order.productName}</h5>
                                        <p className="text-sm text-gray-500 mt-1">Auction ID: <span className="font-mono">{order.auctionId?.substring(0, 8)}...</span></p>
                                    </div>
                                </div>
                            </div>

                            {/* Users Info */}
                            <div className="grid grid-cols-2 gap-4">
                                <div className="bg-gray-50 rounded-xl p-4 border border-gray-100">
                                    <p className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-1">Người bán</p>
                                    <p className="font-semibold text-gray-900">{order.sellerName}</p>
                                    <p className="text-xs text-gray-500 mt-1" title={order.sellerId}>
                                        ID: <span className="font-mono">{order.sellerId?.substring(0, 8)}...</span>
                                    </p>
                                </div>
                                <div className="bg-gray-50 rounded-xl p-4 border border-gray-100">
                                    <p className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-1">Người mua</p>
                                    <p className="font-semibold text-gray-900">{order.buyerName}</p>
                                    <p className="text-xs text-gray-500 mt-1" title={order.buyerId}>
                                        ID: <span className="font-mono">{order.buyerId?.substring(0, 8)}...</span>
                                    </p>
                                </div>
                            </div>
                        </div>

                        {/* Right Column: Financials & Action */}
                        <div className="space-y-6">
                            {/* Financials */}
                            <div className="bg-white rounded-xl p-5 border border-gray-200 shadow-sm">
                                <h4 className="text-sm font-bold text-gray-900 uppercase tracking-wider mb-4 flex items-center gap-2">
                                    <CreditCard className="w-4 h-4 text-gray-500" /> Tài chính
                                </h4>
                                <div className="space-y-3">
                                    <div className="flex justify-between items-center py-2 border-b border-gray-50">
                                        <span className="text-gray-600">Tổng giá trị đơn hàng</span>
                                        <span className="font-bold text-gray-900">{order.totalAmount?.toLocaleString('vi-VN')} đ</span>
                                    </div>
                                    <div className="flex justify-between items-center py-2 border-b border-gray-50">
                                        <span className="text-gray-600">Đã cọc (Deposit)</span>
                                        <span className="font-semibold text-primary">{order.depositAmount?.toLocaleString('vi-VN')} đ</span>
                                    </div>
                                    <div className="flex justify-between items-center py-2">
                                        <span className="text-gray-600">Ngày tạo</span>
                                        <span className="text-gray-900">{new Date(order.createdAt).toLocaleString('vi-VN')}</span>
                                    </div>
                                    {order.paymentDeadline && (
                                        <div className="flex justify-between items-center py-2 bg-red-50 px-3 rounded-lg mt-2">
                                            <span className="text-red-700 font-medium">Hạn thanh toán</span>
                                            <span className="text-red-700 font-bold">{new Date(order.paymentDeadline).toLocaleString('vi-VN')}</span>
                                        </div>
                                    )}
                                </div>
                            </div>

                            {/* Shipping info if any */}
                            {order.trackingCode && (
                                <div className="bg-white rounded-xl p-5 border border-gray-200 shadow-sm">
                                    <h4 className="text-sm font-bold text-gray-900 uppercase tracking-wider mb-3 flex items-center gap-2">
                                        <Truck className="w-4 h-4 text-gray-500" /> Giao hàng
                                    </h4>
                                    <div className="space-y-2">
                                        <div className="flex justify-between">
                                            <span className="text-gray-500">Đơn vị vận chuyển:</span>
                                            <span className="font-medium text-gray-900">{order.shippingProvider}</span>
                                        </div>
                                        <div className="flex justify-between">
                                            <span className="text-gray-500">Mã vận đơn:</span>
                                            <span className="font-mono font-medium text-blue-600 bg-blue-50 px-2 py-0.5 rounded">{order.trackingCode}</span>
                                        </div>
                                    </div>
                                </div>
                            )}

                            {/* Admin Actions */}
                            {canForcePay && (
                                <div className="bg-blue-50 rounded-xl p-5 border border-blue-100 mb-4">
                                    <h4 className="text-sm font-bold text-blue-900 uppercase tracking-wider mb-2">Thao tác Quản trị (Tài chính)</h4>
                                    <p className="text-sm text-blue-700 mb-4">
                                        Tính năng này sẽ ép buộc cập nhật trạng thái đơn hàng thành Đã Thanh Toán. Chỉ sử dụng trong trường hợp cổng thanh toán bị lỗi nhưng khách đã CK thành công.
                                    </p>
                                    
                                    {!showPayConfirm ? (
                                        <button 
                                            onClick={() => setShowPayConfirm(true)}
                                            className="w-full py-2.5 bg-white border-2 border-blue-200 text-blue-600 font-bold rounded-xl hover:bg-blue-600 hover:text-white hover:border-blue-600 transition-colors"
                                        >
                                            Xác nhận Đã thanh toán
                                        </button>
                                    ) : (
                                        <div className="bg-white p-4 rounded-xl border border-blue-200">
                                            <p className="text-sm font-medium text-gray-900 mb-3 text-center">Bạn có chắc chắn khách đã thanh toán?</p>
                                            <div className="flex gap-2">
                                                <button 
                                                    onClick={() => setShowPayConfirm(false)}
                                                    className="flex-1 py-2 px-3 bg-gray-100 hover:bg-gray-200 text-gray-700 font-semibold rounded-lg transition-colors"
                                                    disabled={submitting}
                                                >
                                                    Quay lại
                                                </button>
                                                <button 
                                                    onClick={handleForcePay}
                                                    disabled={submitting}
                                                    className="flex-1 py-2 px-3 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-lg transition-colors flex justify-center items-center"
                                                >
                                                    {submitting ? 'Đang xử lý...' : 'Xác nhận Đã TT'}
                                                </button>
                                            </div>
                                        </div>
                                    )}
                                </div>
                            )}

                            {canCancel && (
                                <div className="bg-red-50 rounded-xl p-5 border border-red-100">
                                    <h4 className="text-sm font-bold text-red-900 uppercase tracking-wider mb-2">Thao tác Quản trị (Xử lý sự cố)</h4>
                                    <p className="text-sm text-red-700 mb-4">
                                        Tính năng này sẽ ép buộc Hủy đơn hàng. Chỉ sử dụng trong trường hợp phát hiện gian lận hoặc lỗi hệ thống không thể tự phục hồi.
                                    </p>
                                    
                                    {!showCancelConfirm ? (
                                        <button 
                                            onClick={() => setShowCancelConfirm(true)}
                                            className="w-full py-2.5 bg-white border-2 border-red-200 text-red-600 font-bold rounded-xl hover:bg-red-600 hover:text-white hover:border-red-600 transition-colors"
                                        >
                                            Hủy đơn hàng (Force Cancel)
                                        </button>
                                    ) : (
                                        <div className="bg-white p-4 rounded-xl border border-red-200">
                                            <p className="text-sm font-medium text-gray-900 mb-3 text-center">Bạn có chắc chắn muốn hủy đơn hàng này?</p>
                                            <div className="flex gap-2">
                                                <button 
                                                    onClick={() => setShowCancelConfirm(false)}
                                                    className="flex-1 py-2 px-3 bg-gray-100 hover:bg-gray-200 text-gray-700 font-semibold rounded-lg transition-colors"
                                                    disabled={submitting}
                                                >
                                                    Quay lại
                                                </button>
                                                <button 
                                                    onClick={handleCancelOrder}
                                                    disabled={submitting}
                                                    className="flex-1 py-2 px-3 bg-red-600 hover:bg-red-700 text-white font-semibold rounded-lg transition-colors flex justify-center items-center"
                                                >
                                                    {submitting ? 'Đang xử lý...' : 'Xác nhận Hủy'}
                                                </button>
                                            </div>
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
