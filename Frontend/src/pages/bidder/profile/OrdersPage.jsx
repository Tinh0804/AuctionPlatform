import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Package, Truck, Star } from 'lucide-react';
import apiClient from '@/services/apiClient';

export default function OrdersPage() {
    const navigate = useNavigate();
    const [searchParams, setSearchParams] = useSearchParams();
    
    // Default sub-tab from URL query ?sub=purchases or default to purchases
    const initialTab = searchParams.get('sub') || 'purchases';
    const [activeTab, setActiveTab] = useState(initialTab);

    const [purchases, setPurchases] = useState([]);
    const [sales, setSales] = useState([]);
    const [showShippingModal, setShowShippingModal] = useState(null);
    const [shippingInfo, setShippingInfo] = useState({ carrier_name: '', tracking_number: '', estimated_delivery: '' });
    const [showReviewModal, setShowReviewModal] = useState(null); 
    const [reviewInfo, setReviewInfo] = useState({ rating: 5, comment: '' });

    useEffect(() => {
        // Sync state to URL when tab changes manually
        if (activeTab !== searchParams.get('sub')) {
            setSearchParams({ sub: activeTab });
        }
    }, [activeTab, setSearchParams, searchParams]);

    useEffect(() => {
        // Fetch data
        apiClient.get('/orders/me/purchases')
            .then(res => setPurchases(res.data?.result || (Array.isArray(res.data) ? res.data : [])))
            .catch(console.error);

        apiClient.get('/orders/me/sales')
            .then(res => setSales(res.data?.result || (Array.isArray(res.data) ? res.data : [])))
            .catch(console.error);
    }, []);

    const handleUpdateShipping = async () => {
        if(!shippingInfo.carrier_name || !shippingInfo.tracking_number || !shippingInfo.estimated_delivery) {
            alert("Vui lòng điền đầy đủ thông tin vận chuyển và ngày dự kiến");
            return;
        }
        try {
            await apiClient.post(`/orders/${showShippingModal}/shipping`, shippingInfo);
            alert("Cập nhật vận đơn thành công!");
            setShowShippingModal(null);
            apiClient.get('/orders/me/sales').then(res => setSales(res.data?.result || (Array.isArray(res.data) ? res.data : [])));
        } catch (error) {
            alert(error.response?.data?.detail || "Lỗi cập nhật vận đơn");
        }
    };

    const handleConfirmReceipt = async (orderId) => {
        if(!window.confirm("Bạn xác nhận đã nhận được hàng và hài lòng với sản phẩm?")) return;
        try {
            await apiClient.post(`/orders/${orderId}/confirm-receipt`);
            alert("Xác nhận đã nhận hàng!");
            apiClient.get('/orders/me/purchases').then(res => setPurchases(res.data?.result || (Array.isArray(res.data) ? res.data : [])));
        } catch (error) {
            alert(error.response?.data?.detail || "Lỗi xác nhận");
        }
    };

    const handleReview = async () => {
        try {
            await apiClient.post(`/orders/${showReviewModal}/complete`, reviewInfo);
            alert("Đã gửi đánh giá thành công!");
            setShowReviewModal(null);
            apiClient.get('/orders/me/purchases').then(res => setPurchases(res.data?.result || (Array.isArray(res.data) ? res.data : [])));
        } catch (error) {
            alert(error.response?.data?.detail || "Lỗi gửi đánh giá");
        }
    };

    const StatusBadge = ({ status, isInvoice }) => {
        const colors = {
            'PENDING': isInvoice ? 'bg-red-500/10 text-red-300 border-red-400/20' : 'bg-[#9A6A2F]/10 text-[#9A6A2F] border-[#9A6A2F]/25',
            'SUCCESS': 'bg-emerald-500/10 text-emerald-300 border-emerald-400/20',
            'SHIPPING': 'bg-sky-500/10 text-sky-300 border-sky-400/20',
            'RECEIVED': 'bg-[#2F2418]/10 text-[#2F2418] border-[#2F2418]/20',
            'COMPLETED': 'bg-emerald-500/10 text-emerald-300 border-emerald-400/20',
            'PENDING_SHIP': 'bg-[#9A6A2F]/10 text-[#9A6A2F] border-[#9A6A2F]/25',
        };
        const label = isInvoice && status === 'PENDING' ? 'Chờ thanh toán' : status;
        return (
            <span className={`text-xs font-bold px-2.5 py-1 border ${colors[status] || 'bg-white/5 text-[#2F2418]/60 border-white/10'}`}>
                {label}
            </span>
        );
    };

    return (
        <div className="space-y-6 animate-fade-in">
            {/* Shipping Modal */}
            {showShippingModal && (
                <div className="fixed inset-0 bg-black/80 backdrop-blur-md z-[100] flex items-center justify-center p-4">
                    <div className="bg-[#FFF8ED] max-w-md w-full p-8 border border-[#9A6A2F]/25 shadow-[0_40px_120px_rgba(47,36,24,0.18)] animate-fade-in">
                        <h3 className="font-serif text-2xl text-[#2F2418] mb-6 flex items-center gap-2"><Truck className="w-5 h-5 text-[#9A6A2F]" /> Cập nhật vận đơn</h3>
                        <div className="space-y-4">
                            <div>
                                <label className="text-sm font-semibold text-[#9A6A2F] mb-1.5 block">Đơn vị vận chuyển</label>
                                <input type="text" placeholder="GHTK, Viettel Post, DHL..." value={shippingInfo.carrier_name}
                                    onChange={(e) => setShippingInfo({...shippingInfo, carrier_name: e.target.value})}
                                    className="w-full bg-[#F8F1E6] border border-[#9A6A2F]/25 px-4 py-2.5 text-sm text-[#2F2418] placeholder:text-[#2F2418]/30 focus:outline-none focus:ring-2 focus:ring-[#9A6A2F]/20 focus:border-[#9A6A2F]/60" />
                            </div>
                            <div>
                                <label className="text-sm font-semibold text-[#9A6A2F] mb-1.5 block">Mã vận đơn</label>
                                <input type="text" placeholder="Tracking number" value={shippingInfo.tracking_number}
                                    onChange={(e) => setShippingInfo({...shippingInfo, tracking_number: e.target.value})}
                                    className="w-full bg-[#F8F1E6] border border-[#9A6A2F]/25 px-4 py-2.5 text-sm text-[#2F2418] placeholder:text-[#2F2418]/30 focus:outline-none focus:ring-2 focus:ring-[#9A6A2F]/20 focus:border-[#9A6A2F]/60" />
                            </div>
                            <div>
                                <label className="text-sm font-semibold text-[#9A6A2F] mb-1.5 block">Ngày dự kiến giao</label>
                                <input type="date" value={shippingInfo.estimated_delivery}
                                    onChange={(e) => setShippingInfo({...shippingInfo, estimated_delivery: e.target.value})}
                                    className="w-full bg-[#F8F1E6] border border-[#9A6A2F]/25 px-4 py-2.5 text-sm text-[#2F2418] focus:outline-none focus:ring-2 focus:ring-[#9A6A2F]/20 focus:border-[#9A6A2F]/60" />
                            </div>
                            <div className="flex gap-3 pt-2">
                                <button onClick={() => setShowShippingModal(null)} className="flex-1 py-2.5 border border-[#9A6A2F]/25 text-sm font-semibold text-[#2F2418]/70 hover:bg-white/[0.04] hover:text-[#2F2418]">Hủy</button>
                                <button onClick={handleUpdateShipping} className="flex-1 bg-[#9A6A2F] text-[#F8F1E6] font-bold py-2.5 justify-center inline-flex items-center">Xác nhận</button>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {/* Review Modal */}
            {showReviewModal && (
                <div className="fixed inset-0 bg-black/80 backdrop-blur-md z-[100] flex items-center justify-center p-4">
                    <div className="bg-[#FFF8ED] max-w-md w-full p-8 border border-[#9A6A2F]/25 shadow-[0_40px_120px_rgba(47,36,24,0.18)] animate-fade-in">
                        <h3 className="font-serif text-2xl text-[#2F2418] mb-2 flex items-center gap-2"><Star className="w-5 h-5 text-[#9A6A2F]" /> Đánh giá sản phẩm</h3>
                        <p className="text-xs text-[#2F2418]/45 mb-6">Trải nghiệm của bạn giúp cộng đồng tin cậy hơn</p>
                        <div className="space-y-5">
                            <div>
                                <label className="text-sm font-semibold text-[#9A6A2F] mb-3 block text-center">Mức độ hài lòng ({reviewInfo.rating}/5)</label>
                                <div className="flex justify-center gap-2">
                                    {[1, 2, 3, 4, 5].map(star => (
                                        <button key={star} onClick={() => setReviewInfo({...reviewInfo, rating: star})}
                                            className="text-3xl hover:scale-110 transition-transform text-amber-400">
                                            {star <= reviewInfo.rating ? '★' : '☆'}
                                        </button>
                                    ))}
                                </div>
                            </div>
                            <div>
                                <label className="text-sm font-semibold text-[#9A6A2F] mb-1.5 block">Nhận xét</label>
                                <textarea rows="3" placeholder="Sản phẩm có đúng mô tả không?..." value={reviewInfo.comment}
                                    onChange={(e) => setReviewInfo({...reviewInfo, comment: e.target.value})}
                                    className="w-full bg-[#F8F1E6] border border-[#9A6A2F]/25 px-4 py-2.5 text-sm text-[#2F2418] placeholder:text-[#2F2418]/30 focus:outline-none focus:ring-2 focus:ring-[#9A6A2F]/20 focus:border-[#9A6A2F]/60 resize-none" />
                            </div>
                            <div className="flex gap-3">
                                <button onClick={() => setShowReviewModal(null)} className="flex-1 py-2.5 border border-[#9A6A2F]/25 text-sm font-semibold text-[#2F2418]/70 hover:bg-white/[0.04] hover:text-[#2F2418]">Hủy</button>
                                <button onClick={handleReview} className="flex-1 bg-[#9A6A2F] text-[#F8F1E6] font-bold py-2.5 justify-center inline-flex items-center">Gửi đánh giá</button>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            <div className="bg-[#FFF8ED] border border-[#9A6A2F]/20 p-8 shadow-[0_28px_90px_rgba(47,36,24,0.10)]">
                <div className="flex flex-col md:flex-row md:items-center justify-between mb-8 gap-4 border-b border-[#9A6A2F]/15 pb-4">
                    <h3 className="font-serif text-2xl text-[#2F2418] flex items-center gap-2">
                        <Package className="w-6 h-6 text-[#9A6A2F]" /> Đơn Hàng Của Tôi
                    </h3>
                    <div className="flex bg-[#F8F1E6] p-1 border border-[#9A6A2F]/25 w-full md:w-auto">
                        <button onClick={() => setActiveTab('purchases')} className={`flex-1 md:w-32 py-2 text-xs font-bold transition-colors ${activeTab === 'purchases' ? 'bg-[#9A6A2F] text-[#F8F1E6]' : 'text-[#2F2418]/60 hover:text-[#2F2418]'}`}>Đơn Mua</button>
                        <button onClick={() => setActiveTab('sales')} className={`flex-1 md:w-32 py-2 text-xs font-bold transition-colors ${activeTab === 'sales' ? 'bg-[#9A6A2F] text-[#F8F1E6]' : 'text-[#2F2418]/60 hover:text-[#2F2418]'}`}>Đơn Bán</button>
                    </div>
                </div>

                {activeTab === 'purchases' && (
                    <div className="bg-[#FFF8ED] border border-[#9A6A2F]/20 overflow-hidden shadow-[0_28px_90px_rgba(47,36,24,0.10)]">
                        <div className="overflow-x-auto">
                            <table className="w-full">
                                <thead>
                                    <tr className="bg-[#F8F1E6] border-b border-[#9A6A2F]/15">
                                        <th className="text-left text-xs font-bold text-[#9A6A2F] uppercase tracking-[0.2em] px-5 py-4">Sản phẩm</th>
                                        <th className="text-left text-xs font-bold text-[#9A6A2F] uppercase tracking-[0.2em] px-5 py-4">Ngày</th>
                                        <th className="text-right text-xs font-bold text-[#9A6A2F] uppercase tracking-[0.2em] px-5 py-4">Tổng tiền</th>
                                        <th className="text-center text-xs font-bold text-[#9A6A2F] uppercase tracking-[0.2em] px-5 py-4">Trạng thái</th>
                                        <th className="text-center text-xs font-bold text-[#9A6A2F] uppercase tracking-[0.2em] px-5 py-4">Hành động</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {purchases.map(p => (
                                        <tr key={p.order_id || p.invoice_id} className="border-b border-white/5 hover:bg-white/[0.03] transition-colors">
                                            <td className="px-5 py-5 text-sm font-semibold text-[#2F2418]">{p.auction_name}</td>
                                            <td className="px-5 py-5 text-xs text-[#2F2418]/50">{p.created_at}</td>
                                            <td className="px-5 py-5 text-sm text-right font-bold text-[#9A6A2F]">{(p.total_amount || p.amount || 0).toLocaleString('vi-VN')} đ</td>
                                            <td className="px-5 py-4 text-center">
                                                <StatusBadge status={p.status} isInvoice={p.is_invoice} />
                                            </td>
                                            <td className="px-5 py-4 text-center">
                                                {p.is_invoice && p.status === 'PENDING' && (
                                                    <button onClick={() => navigate(`/invoices/${p.invoice_id}/checkout`)} className="bg-[#9A6A2F] text-[#F8F1E6] font-bold py-1.5 px-3 text-xs">Thanh toán</button>
                                                )}
                                                {!p.is_invoice && p.status === 'SHIPPING' && (
                                                    <button onClick={() => handleConfirmReceipt(p.order_id)} className="py-1.5 px-3 text-xs font-bold text-[#9A6A2F] border border-[#9A6A2F]/25 hover:bg-[#9A6A2F]/10">Đã nhận hàng</button>
                                                )}
                                                {!p.is_invoice && p.status === 'RECEIVED' && (
                                                    <button onClick={() => setShowReviewModal(p.order_id)} className="bg-[#9A6A2F] text-[#F8F1E6] font-bold py-1.5 px-3 text-xs">Đánh giá</button>
                                                )}
                                            </td>
                                        </tr>
                                    ))}
                                    {purchases.length === 0 && (
                                        <tr><td colSpan="5" className="px-5 py-16 text-center text-sm text-[#2F2418]/45">Chưa có đơn mua nào</td></tr>
                                    )}
                                </tbody>
                            </table>
                        </div>
                    </div>
                )}

                {activeTab === 'sales' && (
                    <div className="bg-[#FFF8ED] border border-[#9A6A2F]/20 overflow-hidden shadow-[0_28px_90px_rgba(47,36,24,0.10)]">
                        <div className="overflow-x-auto">
                            <table className="w-full">
                                <thead>
                                    <tr className="bg-[#F8F1E6] border-b border-[#9A6A2F]/15">
                                        <th className="text-left text-xs font-bold text-[#9A6A2F] uppercase tracking-[0.2em] px-5 py-4">Sản phẩm</th>
                                        <th className="text-left text-xs font-bold text-[#9A6A2F] uppercase tracking-[0.2em] px-5 py-4">Ngày</th>
                                        <th className="text-right text-xs font-bold text-[#9A6A2F] uppercase tracking-[0.2em] px-5 py-4">Tổng tiền</th>
                                        <th className="text-center text-xs font-bold text-[#9A6A2F] uppercase tracking-[0.2em] px-5 py-4">Trạng thái</th>
                                        <th className="text-center text-xs font-bold text-[#9A6A2F] uppercase tracking-[0.2em] px-5 py-4">Hành động</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {sales.map(s => (
                                        <tr key={s.order_id} className="border-b border-white/5 hover:bg-white/[0.03] transition-colors">
                                            <td className="px-5 py-5 text-sm font-semibold text-[#2F2418]">{s.auction_name}</td>
                                            <td className="px-5 py-5 text-xs text-[#2F2418]/50">{s.created_at}</td>
                                            <td className="px-5 py-5 text-sm text-right font-bold text-[#9A6A2F]">{s.total_amount.toLocaleString('vi-VN')} đ</td>
                                            <td className="px-5 py-4 text-center"><StatusBadge status={s.status} /></td>
                                            <td className="px-5 py-4 text-center">
                                                {s.status === 'PENDING_SHIP' && (
                                                    <button onClick={() => setShowShippingModal(s.order_id)} className="bg-[#9A6A2F] text-[#F8F1E6] font-bold py-1.5 px-3 text-xs inline-flex items-center gap-1">
                                                        <Truck className="w-3.5 h-3.5" /> Gửi hàng
                                                    </button>
                                                )}
                                            </td>
                                        </tr>
                                    ))}
                                    {sales.length === 0 && (
                                        <tr><td colSpan="5" className="px-5 py-16 text-center text-sm text-[#2F2418]/45">Chưa có đơn bán nào</td></tr>
                                    )}
                                </tbody>
                            </table>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}
