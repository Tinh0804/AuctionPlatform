import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import apiClient from '@/services/apiClient';
import { useToast } from '@/components/Elements/Toast';
import {
    Wallet as WalletIcon, Lock, MapPin, CheckCircle,
    AlertTriangle, Loader, Edit3, User, Phone, ShieldCheck, ArrowLeft, Package
} from 'lucide-react';

export default function Checkout() {
    const { invoice_id } = useParams();
    const navigate = useNavigate();
    const { toast } = useToast();

    const [invoice, setInvoice] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [paying, setPaying] = useState(false);

    const [editingAddress, setEditingAddress] = useState(false);
    const [shippingName, setShippingName] = useState('');
    const [shippingPhone, setShippingPhone] = useState('');
    const [shippingAddress, setShippingAddress] = useState('');

    const [showSuccessPopup, setShowSuccessPopup] = useState(false);
    const [showInsufficientPopup, setShowInsufficientPopup] = useState(false);
    const [showPinModal, setShowPinModal] = useState(false);
    const [pinCode, setPinCode] = useState('');

    useEffect(() => {
        setLoading(true);
        apiClient.get(`/invoices/${invoice_id}`)
            .then(res => {
                setInvoice(res.data);
                setShippingName(res.data.buyer_name || '');
                setShippingPhone(res.data.buyer_phone || '');
                setShippingAddress(res.data.buyer_address || '');
                setLoading(false);
            })
            .catch(err => {
                setError(err.response?.data?.detail || 'Không thể tải biên bản. Vui lòng thử lại.');
                setLoading(false);
            });
    }, [invoice_id]);

    const handlePaymentClick = () => {
        if (!shippingAddress.trim()) {
            toast.warning('Vui lòng nhập địa chỉ giao hàng.');
            return;
        }
        setShowPinModal(true);
    };

    const submitPayment = async () => {
        if (pinCode.length !== 6) {
            toast.warning('Vui lòng nhập đủ 6 chữ số PIN.');
            return;
        }
        setPaying(true);
        try {
            const fullAddress = `${shippingName} — ${shippingPhone}\n${shippingAddress}`;
            await apiClient.post(`/invoices/${invoice_id}/checkout`, {
                dia_chi_giao_hang: fullAddress,
                ghi_chu: '',
                pin_code: pinCode
            });
            setShowPinModal(false);
            setShowSuccessPopup(true);
        } catch (e) {
            const detail = e.response?.data?.detail || '';
            const status = e.response?.status;
            const isInsufficientError = 
                detail.toLowerCase().includes('không đủ tiền') ||
                detail.toLowerCase().includes('insufficient') ||
                detail.toLowerCase().includes('đủ tiền') ||
                detail.toLowerCase().includes('số dư');
            
            if (isInsufficientError) {
                setShowInsufficientPopup(true);
            } else {
                toast.error(`Lỗi ${status || ''}: ${detail || 'Lỗi hệ thống khi thanh toán. Vui lòng thử lại.'}`);
            }
        } finally {
            setPaying(false);
        }
    };

    if (loading) return (
        <div className="min-h-screen flex items-center justify-center bg-[#F8F1E6]">
            <div className="text-center">
                <Loader className="w-10 h-10 text-[#9A6A2F] animate-spin mx-auto mb-4" />
                <p className="text-[#2F2418]/60 text-sm">Đang tải thông tin thanh toán...</p>
            </div>
        </div>
    );

    if (error) return (
        <div className="min-h-screen flex items-center justify-center bg-[#F8F1E6]">
            <div className="text-center max-w-md bg-[#FFF8ED] border border-[#9A6A2F]/30 shadow-[0_20px_60px_rgba(47,36,24,0.12)] p-10">
                <AlertTriangle className="w-12 h-12 text-red-500 mx-auto mb-4" />
                <h2 className="text-2xl font-bold text-[#2F2418] mb-3">Không thể tải biên bản</h2>
                <p className="text-[#2F2418]/60 text-sm mb-6">{error}</p>
                <button onClick={() => navigate('/profile')} className="bg-[#9A6A2F] hover:bg-[#2F2418] text-[#F8F1E6] font-bold py-3 px-8 transition-colors inline-flex items-center justify-center">Quay về Hồ sơ</button>
            </div>
        </div>
    );

    if (invoice && invoice.status !== 'PENDING') {
        const statusMessages = {
            'SUCCESS': { icon: <CheckCircle className="w-12 h-12 text-emerald-500 mx-auto mb-4" />, title: 'Đã thanh toán', msg: 'Biên bản này đã được thanh toán thành công.' },
            'FAILED': { icon: <AlertTriangle className="w-12 h-12 text-red-500 mx-auto mb-4" />, title: 'Đã hết hạn', msg: 'Biên bản này đã quá hạn hoặc bị hủy bỏ.' },
        };
        const info = statusMessages[invoice.status] || { icon: <AlertTriangle className="w-12 h-12 text-slate-400 mx-auto mb-4" />, title: `Trạng thái: ${invoice.status}`, msg: 'Biên bản không còn hiệu lực.' };
        return (
            <div className="min-h-screen flex items-center justify-center bg-[#F8F1E6]">
                <div className="text-center max-w-md bg-[#FFF8ED] border border-[#9A6A2F]/30 shadow-[0_20px_60px_rgba(47,36,24,0.12)] p-10">
                    {info.icon}
                    <h2 className="text-2xl font-bold text-[#2F2418] mb-3">{info.title}</h2>
                    <p className="text-[#2F2418]/60 text-sm mb-6">{info.msg}</p>
                    <button onClick={() => navigate('/profile')} className="bg-[#9A6A2F] hover:bg-[#2F2418] text-[#F8F1E6] font-bold py-3 px-8 transition-colors inline-flex items-center justify-center">Xem Hồ Sơ</button>
                </div>
            </div>
        );
    }

    const depositPaid = invoice.deposit_already_paid || 0;
    const winningPrice = invoice.winning_price || 0;
    const platformFeePct = invoice.platform_fee_pct || 0;
    const platformFeeAmount = invoice.platform_fee_amount || 0;
    const totalToPay = winningPrice - depositPaid;
    const walletBalance = invoice.wallet_balance || 0;
    const isEnough = walletBalance >= totalToPay;

    return (
        <div className="max-w-7xl mx-auto px-4 md:px-6 py-8 animate-fade-in bg-[#F8F1E6] min-h-screen">
            {/* Success Popup */}
            {showSuccessPopup && (
                <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-[100] flex items-center justify-center p-4">
                    <div className="bg-[#FFF8ED] max-w-md w-full p-10 text-center border-2 border-[#9A6A2F]/30 shadow-2xl animate-fade-in">
                        <div className="w-16 h-16 bg-emerald-100 flex items-center justify-center mx-auto mb-6">
                            <CheckCircle className="w-8 h-8 text-emerald-600" />
                        </div>
                        <h2 className="text-2xl font-bold text-[#2F2418] mb-3">Thanh toán thành công!</h2>
                        <p className="text-[#2F2418]/70 text-sm mb-2">
                            Số tiền <strong className="text-[#9A6A2F]">{totalToPay.toLocaleString('vi-VN')} đ</strong> đã được chuyển vào Ví Escrow.
                        </p>
                        <p className="text-xs text-[#2F2418]/50 mb-8">Giao dịch được bảo vệ bởi hệ thống Escrow bảo mật.</p>
                        <button onClick={() => navigate('/profile')} className="bg-[#9A6A2F] hover:bg-[#2F2418] text-[#F8F1E6] font-bold py-3 w-full transition-colors inline-flex items-center justify-center">Xem Đơn Hàng</button>
                    </div>
                </div>
            )}

            {/* Insufficient Popup */}
            {showInsufficientPopup && (
                <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-[100] flex items-center justify-center p-4">
                    <div className="bg-[#FFF8ED] max-w-md w-full p-10 text-center border-2 border-[#9A6A2F]/30 shadow-2xl animate-fade-in">
                        <div className="w-16 h-16 bg-red-100 flex items-center justify-center mx-auto mb-6">
                            <AlertTriangle className="w-8 h-8 text-red-600" />
                        </div>
                        <h2 className="text-2xl font-bold text-[#2F2418] mb-3">Số dư không đủ</h2>
                        <p className="text-[#2F2418]/70 text-sm mb-2">
                            Ví hiện có <strong>{walletBalance.toLocaleString('vi-VN')} đ</strong>, cần <strong className="text-red-600">{totalToPay.toLocaleString('vi-VN')} đ</strong>.
                        </p>
                        <p className="text-xs text-[#2F2418]/50 mb-8">Vui lòng nạp thêm tiền để thanh toán.</p>
                        <div className="flex gap-3">
                            <button onClick={() => setShowInsufficientPopup(false)} className="flex-1 py-3 border border-[#9A6A2F]/30 text-sm font-semibold text-[#2F2418] hover:bg-[#9A6A2F]/10 transition-colors">Để sau</button>
                            <button onClick={() => navigate('/profile')} className="flex-1 bg-[#9A6A2F] hover:bg-[#2F2418] text-[#F8F1E6] font-bold py-3 transition-colors inline-flex items-center justify-center">Nạp tiền</button>
                        </div>
                    </div>
                </div>
            )}

            {/* Back */}
            <button onClick={() => navigate('/profile')} className="flex items-center gap-2 text-sm text-[#2F2418]/60 hover:text-[#9A6A2F] transition-colors mb-6">
                <ArrowLeft className="w-4 h-4" /> Quay lại hồ sơ
            </button>

            {/* Header */}
            <div className="mb-8">
                <p className="text-xs font-bold text-[#9A6A2F] uppercase tracking-wider mb-1">Biên bản #{invoice_id?.substring(0, 8).toUpperCase()}</p>
                <h1 className="text-3xl md:text-4xl font-extrabold text-[#2F2418] mb-1">Xác Nhận Thanh Toán</h1>
                <p className="text-[#2F2418]/60">{invoice.product_name}</p>
            </div>

            <div className="flex flex-col lg:flex-row gap-8">
                {/* Left Column */}
                <div className="w-full lg:w-3/5 space-y-6">
                    {/* Shipping Info */}
                    <div className="bg-[#FFF8ED] border border-[#9A6A2F]/30 shadow-[0_20px_60px_rgba(47,36,24,0.12)] p-6">
                        <div className="flex justify-between items-center mb-5">
                            <h3 className="text-lg font-bold text-[#2F2418] flex items-center gap-2">
                                <MapPin className="w-5 h-5 text-[#9A6A2F]" /> Thông tin giao hàng
                            </h3>
                            <button onClick={() => setEditingAddress(!editingAddress)} className="text-xs font-semibold text-[#9A6A2F] hover:text-[#2F2418] flex items-center gap-1 transition-colors">
                                <Edit3 className="w-3.5 h-3.5" /> {editingAddress ? 'Lưu' : 'Thay đổi'}
                            </button>
                        </div>

                        {!editingAddress ? (
                            <div className="bg-[#F8F1E6] p-5 space-y-3">
                                <div className="flex items-center gap-3 text-sm text-[#2F2418]">
                                    <User className="w-4 h-4 text-[#9A6A2F]/60" />
                                    <span className="font-medium">{shippingName || <span className="text-[#2F2418]/40 italic">Chưa có tên</span>}</span>
                                </div>
                                <div className="flex items-center gap-3 text-sm text-[#2F2418]">
                                    <Phone className="w-4 h-4 text-[#9A6A2F]/60" />
                                    <span>{shippingPhone || <span className="text-[#2F2418]/40 italic">Chưa có SĐT</span>}</span>
                                </div>
                                <div className="flex items-start gap-3 text-sm text-[#2F2418]">
                                    <MapPin className="w-4 h-4 text-[#9A6A2F]/60 shrink-0 mt-0.5" />
                                    <span>{shippingAddress || <span className="text-red-500 italic">Chưa có địa chỉ — bấm "Thay đổi"</span>}</span>
                                </div>
                            </div>
                        ) : (
                            <div className="space-y-4">
                                <div>
                                    <label className="text-xs font-semibold text-[#2F2418]/70 mb-1.5 block">Họ và Tên</label>
                                    <input value={shippingName} onChange={e => setShippingName(e.target.value)}
                                        className="w-full bg-[#F8F1E6] border border-[#9A6A2F]/30 px-4 py-2.5 text-sm text-[#2F2418] focus:outline-none focus:ring-2 focus:ring-[#9A6A2F]/20 focus:border-[#9A6A2F] transition-all"
                                        placeholder="Nguyễn Văn A" />
                                </div>
                                <div>
                                    <label className="text-xs font-semibold text-[#2F2418]/70 mb-1.5 block">Số điện thoại</label>
                                    <input value={shippingPhone} onChange={e => setShippingPhone(e.target.value)}
                                        className="w-full bg-[#F8F1E6] border border-[#9A6A2F]/30 px-4 py-2.5 text-sm text-[#2F2418] focus:outline-none focus:ring-2 focus:ring-[#9A6A2F]/20 focus:border-[#9A6A2F] transition-all"
                                        placeholder="+84 90 000 0000" />
                                </div>
                                <div>
                                    <label className="text-xs font-semibold text-[#2F2418]/70 mb-1.5 block">Địa chỉ nhận hàng</label>
                                    <textarea value={shippingAddress} onChange={e => setShippingAddress(e.target.value)} rows={3}
                                        className="w-full bg-[#F8F1E6] border border-[#9A6A2F]/30 px-4 py-2.5 text-sm text-[#2F2418] focus:outline-none focus:ring-2 focus:ring-[#9A6A2F]/20 focus:border-[#9A6A2F] transition-all resize-none"
                                        placeholder="Số nhà, Đường, Phường/Xã, Quận/Huyện, Tỉnh/TP" />
                                </div>
                            </div>
                        )}
                    </div>

                    {/* Payment Method */}
                    <div className="bg-[#FFF8ED] border border-[#9A6A2F]/30 shadow-[0_20px_60px_rgba(47,36,24,0.12)] p-6">
                        <h3 className="text-lg font-bold text-[#2F2418] mb-5 flex items-center gap-2">
                            <WalletIcon className="w-5 h-5 text-[#9A6A2F]" /> Phương thức thanh toán
                        </h3>
                        <div className={`border-2 p-5 flex gap-4 items-center ${isEnough ? 'border-[#9A6A2F] bg-[#9A6A2F]/10' : 'border-red-200 bg-red-50/30'}`}>
                            <div className="w-12 h-12 bg-[#FFF8ED] shadow-[0_8px_24px_rgba(47,36,24,0.08)] flex items-center justify-center">
                                <WalletIcon className="w-6 h-6 text-[#9A6A2F]" />
                            </div>
                            <div className="flex-1">
                                <h4 className="font-bold text-sm text-[#2F2418] mb-0.5">Ví Đấu Giá</h4>
                                <p className="text-xs text-[#2F2418]/70">
                                    Số dư: <span className={`font-bold ${isEnough ? 'text-emerald-600' : 'text-red-600'}`}>
                                        {walletBalance.toLocaleString('vi-VN')} đ
                                    </span>
                                    {!isEnough && <span className="text-red-500 ml-1">(Không đủ)</span>}
                                </p>
                            </div>
                            <div className={`w-5 h-5 rounded-full border-4 ${isEnough ? 'border-[#9A6A2F]' : 'border-red-400'}`} />
                        </div>
                        <p className="text-xs text-[#2F2418]/50 mt-4 flex items-center gap-1.5">
                            <Lock className="w-3.5 h-3.5" /> Bảo mật bởi Escrow Protocol
                        </p>
                    </div>
                </div>

                {/* Right Column - Invoice Summary */}
                <div className="w-full lg:w-2/5">
                    <div className="bg-[#FFF8ED] border border-[#9A6A2F]/30 shadow-[0_20px_60px_rgba(47,36,24,0.12)] p-6 sticky top-24">
                        <h3 className="text-lg font-bold text-[#2F2418] mb-6 flex items-center gap-2">
                            <Package className="w-5 h-5 text-[#9A6A2F]" /> Tóm tắt đơn
                        </h3>

                        <div className="flex gap-4 mb-6 pb-6 border-b border-[#9A6A2F]/20">
                            <div className="w-20 h-24 bg-[#F8F1E6] shrink-0 overflow-hidden">
                                {invoice.cover_image ? (
                                    <img src={invoice.cover_image} alt={invoice.product_name} className="w-full h-full object-cover" />
                                ) : (
                                    <div className="w-full h-full flex items-center justify-center">
                                        <Package className="w-6 h-6 text-[#9A6A2F]/30" />
                                    </div>
                                )}
                            </div>
                            <div className="flex-1 min-w-0">
                                <p className="text-xs font-bold text-[#9A6A2F] uppercase mb-1">Vật phẩm đấu giá</p>
                                <h4 className="font-bold text-[#2F2418] leading-snug line-clamp-2 mb-1">{invoice.product_name}</h4>
                                {invoice.product_description && (
                                    <p className="text-xs text-[#2F2418]/50 line-clamp-2">{invoice.product_description}</p>
                                )}
                            </div>
                        </div>

                        <div className="space-y-3 mb-6 pb-6 border-b border-[#9A6A2F]/20 text-sm">
                            <div className="flex justify-between">
                                <span className="text-[#2F2418]/60">Giá thắng</span>
                                <span className="font-semibold text-[#2F2418]">{winningPrice.toLocaleString('vi-VN')} đ</span>
                            </div>
                            <div className="flex justify-between">
                                <span className="text-[#2F2418]/60">Tiền cọc đã nộp</span>
                                <span className="font-semibold text-emerald-600">- {depositPaid.toLocaleString('vi-VN')} đ</span>
                            </div>
                            {platformFeePct > 0 && (
                                <div className="flex justify-between">
                                    <span className="text-[#2F2418]/60">Phí nền tảng ({platformFeePct}%)</span>
                                    <span className="font-semibold text-[#2F2418]">{platformFeeAmount.toLocaleString('vi-VN')} đ</span>
                                </div>
                            )}
                        </div>

                        <div className="bg-[#9A6A2F]/10 border border-[#9A6A2F]/30 p-4 mb-6">
                            <p className="text-xs font-bold text-[#9A6A2F] uppercase mb-1">Cần thanh toán thêm</p>
                            <p className="text-3xl font-extrabold text-[#2F2418]">{totalToPay.toLocaleString('vi-VN')} đ</p>
                        </div>

                        <button
                            onClick={handlePaymentClick}
                            disabled={paying}
                            className={`bg-[#9A6A2F] hover:bg-[#2F2418] text-[#F8F1E6] font-bold w-full py-3.5 transition-colors inline-flex items-center justify-center text-base gap-2 ${paying ? 'opacity-60 cursor-wait' : ''}`}
                        >
                            {paying
                                ? <><Loader className="w-5 h-5 animate-spin" /> Đang xử lý...</>
                                : <><Lock className="w-5 h-5" /> Xác nhận thanh toán</>
                            }
                        </button>

                        <p className="text-xs text-[#2F2418]/50 text-center mt-4 flex items-center justify-center gap-1.5">
                            <ShieldCheck className="w-3.5 h-3.5" /> Tiền được bảo vệ trong Escrow cho đến khi bạn nhận hàng
                        </p>
                    </div>
                </div>
            </div>

            {/* PIN Modal */}
            {showPinModal && (
                <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-[100] flex items-center justify-center p-4">
                    <div className="bg-[#FFF8ED] max-w-sm w-full p-8 border-2 border-[#9A6A2F]/30 shadow-2xl animate-fade-in">
                        <div className="w-14 h-14 bg-[#9A6A2F]/10 flex items-center justify-center mx-auto mb-4">
                            <Lock className="w-7 h-7 text-[#9A6A2F]" />
                        </div>
                        <h3 className="text-xl font-bold text-[#2F2418] mb-2 text-center">Xác thực giao dịch</h3>
                        <p className="text-sm text-[#2F2418]/60 mb-6 text-center">Nhập mã PIN bảo mật 6 số</p>
                        
                        <input 
                            type="password" 
                            placeholder="••••••" 
                            className="w-full bg-[#F8F1E6] border border-[#9A6A2F]/30 px-4 py-3 text-center tracking-[0.5em] text-2xl font-bold text-[#2F2418] focus:outline-none focus:ring-2 focus:ring-[#9A6A2F]/20 focus:border-[#9A6A2F] mb-6"
                            value={pinCode}
                            onChange={(e) => setPinCode(e.target.value)}
                            maxLength={6}
                            autoFocus
                        />

                        <div className="flex gap-3">
                            <button 
                                onClick={() => { setShowPinModal(false); setPinCode(''); }} 
                                disabled={paying}
                                className="flex-1 py-3 border border-[#9A6A2F]/30 text-sm font-semibold text-[#2F2418] hover:bg-[#9A6A2F]/10 transition-colors"
                            >
                                Hủy
                            </button>
                            <button 
                                onClick={submitPayment} 
                                disabled={paying || pinCode.length !== 6} 
                                className="flex-1 bg-[#9A6A2F] hover:bg-[#2F2418] text-[#F8F1E6] font-bold py-3 transition-colors inline-flex items-center justify-center disabled:opacity-50"
                            >
                                {paying ? <Loader className="w-4 h-4 animate-spin" /> : 'Xác nhận'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
