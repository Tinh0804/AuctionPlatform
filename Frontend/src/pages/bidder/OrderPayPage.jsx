import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import apiClient from '@/services/apiClient';
import { useToast } from '@/components/Elements/Toast';
import {
    Wallet as WalletIcon, Lock, CheckCircle,
    AlertTriangle, Loader, ArrowLeft, Package, CreditCard, ShieldCheck, Smartphone
} from 'lucide-react';

export default function OrderPayPage() {
    const { orderId } = useParams();
    const navigate = useNavigate();
    const { toast } = useToast();

    const [order, setOrder] = useState(null);
    const [loading, setLoading] = useState(true);
    const [paying, setPaying] = useState(false);

    const [showSuccessPopup, setShowSuccessPopup] = useState(false);
    const [showInsufficientPopup, setShowInsufficientPopup] = useState(false);
    const [showPinModal, setShowPinModal] = useState(false);
    const [pinCode, setPinCode] = useState('');
    const [paymentMethod, setPaymentMethod] = useState('WALLET');

    useEffect(() => {
        setLoading(true);
        apiClient.get(`/orders/${orderId}`)
            .then(res => {
                const data = res.data?.result || res.data;
                setOrder(data);
                setLoading(false);
            })
            .catch(err => {
                toast.error('Không thể tải thông tin đơn hàng');
                navigate('/profile/orders');
            });
    }, [orderId, navigate]);

    const handlePaymentClick = () => {
        if (paymentMethod === 'WALLET') {
            setShowPinModal(true);
        } else {
            submitGatewayPayment(paymentMethod);
        }
    };

    const submitGatewayPayment = async (method) => {
        setPaying(true);
        try {
            const returnUrl = `${window.location.origin}/payment/pending?type=order&orderId=${orderId}`;
            const response = await apiClient.post(`/orders/${orderId}/initiate-payment`, {
                paymentMethod: method,
                returnUrl
            });
            const result = response.data?.result || response.data;
            if (result.paymentUrl) {
                window.location.href = result.paymentUrl;
            } else {
                toast.error('Không tìm thấy link thanh toán');
            }
        } catch (error) {
            toast.error(error.response?.data?.message || 'Lỗi khởi tạo thanh toán');
        } finally {
            setPaying(false);
        }
    };

    const submitWalletPayment = async () => {
        if (pinCode.length !== 6) {
            toast.warning('Vui lòng nhập đủ 6 chữ số PIN.');
            return;
        }
        setPaying(true);
        try {
            const response = await apiClient.post(`/orders/${orderId}/initiate-payment`, {
                paymentMethod: 'WALLET',
                pinCode
            });
            setShowPinModal(false);
            setShowSuccessPopup(true);
        } catch (error) {
            const errCode = error.response?.data?.code;
            if (errCode === 1014) { // INSUFFICIENT_BALANCE
                setShowPinModal(false);
                setShowInsufficientPopup(true);
            } else if (errCode === 1022) { // INVALID_PIN
                toast.error('Mã PIN không chính xác.');
            } else {
                toast.error(error.response?.data?.message || 'Có lỗi xảy ra khi thanh toán.');
            }
        } finally {
            setPaying(false);
        }
    };

    if (loading) {
        return (
            <div className="flex h-[60vh] items-center justify-center">
                <Loader className="h-8 w-8 animate-spin text-[#9A6A2F]" />
            </div>
        );
    }

    if (!order) return null;

    const depositAmount = order.depositAmount || 0;
    const amountToPay = Math.max(0, order.totalAmount - depositAmount);

    return (
        <section className="min-h-screen bg-[#F8F1E6] py-12 px-4 font-inter text-[#2F2418]">
            <div className="mx-auto max-w-5xl">
                {/* Header */}
                <div className="mb-8 flex items-center justify-between">
                    <div>
                        <button
                            onClick={() => navigate(-1)}
                            className="group mb-4 flex items-center gap-2 text-sm font-semibold tracking-wider text-[#9A6A2F] transition-all hover:text-[#2F2418]"
                        >
                            <ArrowLeft className="h-4 w-4 transition-transform group-hover:-translate-x-1" />
                            QUAY LẠI
                        </button>
                        <h1 className="font-serif text-3xl font-medium tracking-tight md:text-4xl">
                            Thanh Toán Đơn Hàng
                        </h1>
                    </div>
                </div>

                <div className="grid grid-cols-1 gap-8 lg:grid-cols-12">
                    {/* Left Column: Order Info */}
                    <div className="lg:col-span-7">
                        <div className="border border-[#2F2418]/10 bg-white p-6 md:p-8 shadow-sm">
                            <h2 className="mb-6 flex items-center gap-2 font-serif text-xl font-medium">
                                <Package className="h-5 w-5 text-[#9A6A2F]" />
                                Thông tin đơn hàng
                            </h2>

                            <div className="flex items-center gap-4 mb-6">
                                {order.productImageUrl && (
                                    <div className="h-20 w-20 flex-shrink-0 overflow-hidden border border-[#2F2418]/10 bg-gray-50">
                                        <img src={order.productImageUrl} alt={order.productName} className="h-full w-full object-cover" />
                                    </div>
                                )}
                                <div>
                                    <h3 className="text-lg font-semibold">{order.productName}</h3>
                                    <p className="text-sm text-[#2F2418]/60 mt-1">Người bán: {order.sellerName}</p>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Right Column: Payment Options */}
                    <div className="lg:col-span-5">
                        <div className="border border-[#2F2418]/10 bg-white p-6 shadow-sm">
                            <h3 className="mb-4 font-serif text-lg font-medium border-b border-[#2F2418]/10 pb-4">Tóm tắt thanh toán</h3>
                            
                            <div className="mb-6 space-y-3 text-sm">
                                <div className="flex justify-between">
                                    <span className="text-[#2F2418]/70">Tổng giá trị</span>
                                    <span className="font-medium">{order.totalAmount?.toLocaleString('vi-VN')} đ</span>
                                </div>
                                <div className="flex justify-between">
                                    <span className="text-[#2F2418]/70">Đã cọc</span>
                                    <span className="font-medium text-emerald-600">
                                        -{depositAmount.toLocaleString('vi-VN')} đ
                                    </span>
                                </div>
                                <div className="flex justify-between border-t border-[#2F2418]/10 pt-3">
                                    <span className="font-semibold text-[#9A6A2F]">Cần thanh toán</span>
                                    <span className="text-lg font-bold text-[#9A6A2F]">{amountToPay.toLocaleString('vi-VN')} đ</span>
                                </div>
                            </div>

                            <h3 className="mb-4 font-serif text-lg font-medium border-b border-[#2F2418]/10 pb-4">Phương thức thanh toán</h3>
                            
                            <div className="space-y-3 mb-8">
                                <label className={`flex cursor-pointer items-center gap-3 border p-4 transition-all ${paymentMethod === 'WALLET' ? 'border-[#9A6A2F] bg-[#9A6A2F]/5' : 'border-[#2F2418]/10 hover:border-[#2F2418]/30'}`}>
                                    <input type="radio" name="payment" value="WALLET" checked={paymentMethod === 'WALLET'} onChange={() => setPaymentMethod('WALLET')} className="h-4 w-4 text-[#9A6A2F] focus:ring-[#9A6A2F]" />
                                    <WalletIcon className="h-5 w-5 text-[#9A6A2F]" />
                                    <span className="font-medium">Ví nội bộ</span>
                                </label>
                                
                                <label className={`flex cursor-pointer items-center gap-3 border p-4 transition-all ${paymentMethod === 'MOMO' ? 'border-[#9A6A2F] bg-[#9A6A2F]/5' : 'border-[#2F2418]/10 hover:border-[#2F2418]/30'}`}>
                                    <input type="radio" name="payment" value="MOMO" checked={paymentMethod === 'MOMO'} onChange={() => setPaymentMethod('MOMO')} className="h-4 w-4 text-[#9A6A2F] focus:ring-[#9A6A2F]" />
                                    <Smartphone className="h-5 w-5 text-[#A50064]" />
                                    <span className="font-medium">Ví MoMo</span>
                                </label>

                                <label className={`flex cursor-pointer items-center gap-3 border p-4 transition-all ${paymentMethod === 'VNPAY' ? 'border-[#9A6A2F] bg-[#9A6A2F]/5' : 'border-[#2F2418]/10 hover:border-[#2F2418]/30'}`}>
                                    <input type="radio" name="payment" value="VNPAY" checked={paymentMethod === 'VNPAY'} onChange={() => setPaymentMethod('VNPAY')} className="h-4 w-4 text-[#9A6A2F] focus:ring-[#9A6A2F]" />
                                    <CreditCard className="h-5 w-5 text-[#005BAA]" />
                                    <span className="font-medium">VNPay</span>
                                </label>
                            </div>

                            <button
                                onClick={handlePaymentClick}
                                disabled={paying}
                                className="flex w-full items-center justify-center gap-2 bg-[#2F2418] px-6 py-4 text-sm font-bold tracking-wider text-white transition-all hover:bg-[#9A6A2F] disabled:cursor-not-allowed disabled:opacity-70"
                            >
                                {paying ? <Loader className="h-5 w-5 animate-spin" /> : <ShieldCheck className="h-5 w-5" />}
                                {paying ? 'ĐANG XỬ LÝ...' : 'XÁC NHẬN THANH TOÁN'}
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            {/* PIN Modal */}
            {showPinModal && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-sm">
                    <div className="w-full max-w-md bg-white p-8 shadow-2xl relative">
                        <button
                            onClick={() => setShowPinModal(false)}
                            className="absolute right-4 top-4 text-[#2F2418]/40 hover:text-[#2F2418]"
                        >
                            ✕
                        </button>
                        <div className="mb-6 text-center">
                            <div className="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-[#9A6A2F]/10">
                                <Lock className="h-6 w-6 text-[#9A6A2F]" />
                            </div>
                            <h3 className="font-serif text-2xl font-medium text-[#2F2418]">Xác thực PIN</h3>
                            <p className="mt-2 text-sm text-[#2F2418]/60">
                                Vui lòng nhập mã PIN 6 số để xác nhận thanh toán <span className="font-semibold text-[#9A6A2F]">{amountToPay.toLocaleString('vi-VN')} đ</span>
                            </p>
                        </div>

                        <div className="mb-6 flex justify-center gap-2">
                            {[...Array(6)].map((_, i) => (
                                <input
                                    key={i}
                                    type="password"
                                    maxLength={1}
                                    value={pinCode[i] || ''}
                                    onChange={(e) => {
                                        const val = e.target.value.replace(/[^0-9]/g, '');
                                        const newPin = pinCode.split('');
                                        newPin[i] = val;
                                        setPinCode(newPin.join(''));
                                        if (val && i < 5) {
                                            e.target.nextElementSibling?.focus();
                                        }
                                    }}
                                    onKeyDown={(e) => {
                                        if (e.key === 'Backspace' && !pinCode[i] && i > 0) {
                                            e.target.previousElementSibling?.focus();
                                        }
                                    }}
                                    className="h-12 w-12 border-b-2 border-[#2F2418]/20 bg-transparent text-center text-2xl font-bold text-[#2F2418] focus:border-[#9A6A2F] focus:outline-none"
                                />
                            ))}
                        </div>

                        <button
                            onClick={submitWalletPayment}
                            disabled={paying || pinCode.length !== 6}
                            className="flex w-full items-center justify-center gap-2 bg-[#9A6A2F] py-3 text-sm font-bold text-white transition-colors hover:bg-[#2F2418] disabled:opacity-50"
                        >
                            {paying ? <Loader className="h-4 w-4 animate-spin" /> : 'XÁC NHẬN'}
                        </button>
                    </div>
                </div>
            )}

            {/* Success Popup */}
            {showSuccessPopup && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-sm">
                    <div className="w-full max-w-sm bg-white p-8 text-center shadow-2xl">
                        <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-emerald-100">
                            <CheckCircle className="h-8 w-8 text-emerald-600" />
                        </div>
                        <h3 className="mb-2 font-serif text-2xl font-medium text-[#2F2418]">Thanh toán thành công!</h3>
                        <p className="mb-6 text-sm text-[#2F2418]/60">
                            Bạn đã hoàn tất thanh toán cho đơn hàng này.
                        </p>
                        <button
                            onClick={() => navigate('/profile/orders?sub=purchases')}
                            className="w-full bg-[#9A6A2F] py-3 text-sm font-bold text-white hover:bg-[#2F2418]"
                        >
                            XEM ĐƠN HÀNG
                        </button>
                    </div>
                </div>
            )}

            {/* Insufficient Balance Popup */}
            {showInsufficientPopup && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-sm">
                    <div className="w-full max-w-sm bg-white p-8 text-center shadow-2xl">
                        <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-rose-100">
                            <AlertTriangle className="h-8 w-8 text-rose-600" />
                        </div>
                        <h3 className="mb-2 font-serif text-2xl font-medium text-[#2F2418]">Số dư không đủ</h3>
                        <p className="mb-6 text-sm text-[#2F2418]/60">
                            Ví của bạn không đủ số dư để thanh toán khoản tiền này. Vui lòng nạp thêm tiền vào ví.
                        </p>
                        <div className="flex gap-3">
                            <button
                                onClick={() => setShowInsufficientPopup(false)}
                                className="flex-1 border border-[#2F2418]/20 bg-transparent py-3 text-sm font-bold text-[#2F2418] hover:bg-gray-50"
                            >
                                ĐÓNG
                            </button>
                            <button
                                onClick={() => navigate('/profile/wallet')}
                                className="flex-1 bg-[#9A6A2F] py-3 text-sm font-bold text-white hover:bg-[#2F2418]"
                            >
                                NẠP TIỀN
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </section>
    );
}
