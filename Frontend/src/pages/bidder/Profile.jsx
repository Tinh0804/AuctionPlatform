import { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import apiClient from '@/services/apiClient';
import { LogOut, ShieldCheck, User as UserIcon, Award, Wallet as WalletIcon, Lock, Loader, Star, Package, Truck, CreditCard, Settings, CheckCircle, AlertCircle } from 'lucide-react';
import { RecaptchaVerifier, signInWithPhoneNumber } from 'firebase/auth';
import { auth } from '@/services/firebase';

export default function Profile() {
    const navigate = useNavigate();
    const location = useLocation();
    const [profile, setProfile] = useState(null);
    const [purchases, setPurchases] = useState([]);
    const [sales, setSales] = useState([]);
    const [showShippingModal, setShowShippingModal] = useState(null);
    const [shippingInfo, setShippingInfo] = useState({ carrier_name: '', tracking_number: '', estimated_delivery: '' });
    const [showReviewModal, setShowReviewModal] = useState(null); 
    const [reviewInfo, setReviewInfo] = useState({ rating: 5, comment: '' });
    const [showDepositModal, setShowDepositModal] = useState(false);
    const [depositAmount, setDepositAmount] = useState(50000);
    const [showSuccessPopup, setShowSuccessPopup] = useState(false);
    const [successPopup, setSuccessPopup] = useState({ title: 'Nạp tiền thành công!', message: 'Số dư ví đã được cập nhật.' });
    
    const [showPinModal, setShowPinModal] = useState(false);
    const [pinStep, setPinStep] = useState(1);
    const [phoneNumber, setPhoneNumber] = useState('');
    const [otp, setOtp] = useState('');
    const [newPin, setNewPin] = useState('');
    const [confirmationResult, setConfirmationResult] = useState(null);
    const [countdown, setCountdown] = useState(0);
    const [pinLoading, setPinLoading] = useState(false);
    const [pinMessage, setPinMessage] = useState(null);
    const [activeTab, setActiveTab] = useState('purchases');

    useEffect(() => {
        const queryParams = new URLSearchParams(location.search);
        if (queryParams.get('payment_success') === 'true') {
            setSuccessPopup({ title: 'Nạp tiền thành công!', message: 'Số dư ví đã được cập nhật.' });
            setShowSuccessPopup(true);
            navigate('/profile', { replace: true });
        }
    }, [location, navigate]);

    useEffect(() => {
        apiClient.get('/auth/me')
            .then(res => setProfile(res.data))
            .catch(() => { navigate('/login'); });
            
        apiClient.get('/orders/me/purchases')
            .then(res => setPurchases(res.data))
            .catch(console.error);

        apiClient.get('/orders/me/sales')
            .then(res => setSales(res.data))
            .catch(console.error);
    }, [navigate]);

    const handleUpdateShipping = async () => {
        if(!shippingInfo.carrier_name || !shippingInfo.tracking_number || !shippingInfo.estimated_delivery) {
            alert("Vui lòng điền đầy đủ thông tin vận chuyển và ngày dự kiến");
            return;
        }
        try {
            await apiClient.post(`/orders/${showShippingModal}/shipping`, shippingInfo);
            alert("Cập nhật vận đơn thành công!");
            setShowShippingModal(null);
            apiClient.get('/orders/me/sales').then(res => setSales(res.data));
        } catch (error) {
            alert(error.response?.data?.detail || "Lỗi cập nhật vận đơn");
        }
    };

    const handleConfirmReceipt = async (orderId) => {
        if(!window.confirm("Bạn xác nhận đã nhận được hàng và hài lòng với sản phẩm?")) return;
        try {
            await apiClient.post(`/orders/${orderId}/confirm-receipt`);
            alert("Xác nhận đã nhận hàng!");
            apiClient.get('/orders/me/purchases').then(res => setPurchases(res.data));
        } catch (error) {
            alert(error.response?.data?.detail || "Lỗi xác nhận");
        }
    };

    const handleReview = async () => {
        try {
            await apiClient.post(`/orders/${showReviewModal}/complete`, reviewInfo);
            alert("Đã gửi đánh giá thành công!");
            setShowReviewModal(null);
            apiClient.get('/orders/me/purchases').then(res => setPurchases(res.data));
        } catch (error) {
            alert(error.response?.data?.detail || "Lỗi gửi đánh giá");
        }
    };

    const handleLogout = () => {
        localStorage.removeItem('token');
        navigate('/login');
    };

    const handleDeposit = async (provider) => {
        if (!depositAmount || depositAmount < 10000) {
            alert("Vui lòng nhập số tiền hợp lệ (tối thiểu 10.000đ)");
            return;
        }
        try {
            const res = await apiClient.post(`/wallets/deposit/request?amount=${depositAmount}&provider=${provider}`);
            if (res.data.payment_url) {
                window.location.href = res.data.payment_url;
            }
        } catch (error) {
            alert(error.response?.data?.detail || "Lỗi tạo yêu cầu nạp tiền");
        }
    };

    useEffect(() => {
        let timer;
        if (countdown > 0) {
            timer = setTimeout(() => setCountdown(countdown - 1), 1000);
        }
        return () => clearTimeout(timer);
    }, [countdown]);

    const setupRecaptcha = async () => {
        if (window.recaptchaVerifier) {
            window.recaptchaVerifier.clear();
            window.recaptchaVerifier = null;
        }

        window.recaptchaVerifier = new RecaptchaVerifier(auth, 'recaptcha-container', {
            size: 'invisible',
        });

        // Firebase needs a fresh rendered verifier for each phone-auth attempt.
        await window.recaptchaVerifier.render();
        return window.recaptchaVerifier;
    };

    const formatPhoneForFirebase = (phone) => {
        const digitsOnly = phone.replace(/[^\d+]/g, '');
        if (digitsOnly.startsWith('+')) return digitsOnly;
        if (digitsOnly.startsWith('0')) return `+84${digitsOnly.slice(1)}`;
        if (digitsOnly.startsWith('84')) return `+${digitsOnly}`;
        return digitsOnly;
    };

    const handleSendOTP = async () => {
        const phoneToUse = profile?.phone_number;
        setPinMessage(null);
        if (!phoneToUse) {
            setPinMessage({ type: 'error', text: 'Tài khoản chưa có số điện thoại để nhận OTP.' });
            return;
        }
        setPinLoading(true);
        try {
            const appVerifier = await setupRecaptcha();
            const formattedPhone = formatPhoneForFirebase(phoneToUse);
            const confirmation = await signInWithPhoneNumber(auth, formattedPhone, appVerifier);
            setConfirmationResult(confirmation);
            setPinStep(2);
            setCountdown(60);
            setPinMessage({ type: 'success', text: 'Mã OTP đã được gửi. Vui lòng kiểm tra SMS.' });
        } catch (error) {
            console.error(error);
            if (window.recaptchaVerifier) {
                window.recaptchaVerifier.clear();
                window.recaptchaVerifier = null;
            }
            setPinMessage({ type: 'error', text: `Không gửi được OTP: ${error.message}` });
        } finally { setPinLoading(false); }
    };

    const handleVerifyOTP = async () => {
        setPinMessage(null);
        if (!otp || otp.length < 6) {
            setPinMessage({ type: 'error', text: 'Vui lòng nhập đủ 6 chữ số OTP.' });
            return;
        }
        setPinLoading(true);
        try {
            await confirmationResult.confirm(otp);
            setPinStep(3);
            setPinMessage({ type: 'success', text: 'Xác thực OTP thành công. Hãy tạo mã PIN mới.' });
        } catch (error) {
            console.error(error);
            setPinMessage({ type: 'error', text: 'Mã OTP không đúng hoặc đã hết hạn.' });
        } finally { setPinLoading(false); }
    };

    const handleSetupPIN = async () => {
        setPinMessage(null);
        if (!/^\d{6}$/.test(newPin)) {
            setPinMessage({ type: 'error', text: 'Mã PIN phải gồm đúng 6 chữ số.' });
            return;
        }
        setPinLoading(true);
        try {
            const idToken = await auth.currentUser.getIdToken();
            await apiClient.post('/wallets/pin/setup', { firebase_id_token: idToken, new_pin: newPin });
            if (profile && profile.wallet) {
                setProfile({...profile, wallet: {...profile.wallet, has_pin: true}});
            }
            setSuccessPopup({ title: 'Thiết lập PIN thành công!', message: 'Ví của bạn đã được bảo vệ bằng mã PIN mới.' });
            setShowSuccessPopup(true);
            closePinModal();
        } catch (error) {
            setPinMessage({ type: 'error', text: error.response?.data?.detail || 'Không thể thiết lập mã PIN. Vui lòng thử lại.' });
        } finally { setPinLoading(false); }
    };

    const closePinModal = () => {
        setShowPinModal(false); setPinStep(1); setOtp(''); setNewPin(''); setPhoneNumber('');
        setPinMessage(null);
        if (window.recaptchaVerifier) { window.recaptchaVerifier.clear(); window.recaptchaVerifier = null; }
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

    if(!profile) return (
        <div className="min-h-screen flex items-center justify-center">
            <Loader className="w-8 h-8 text-primary animate-spin" />
        </div>
    );

    return (
        <div className="min-h-screen bg-[#F8F1E6] -mx-4 md:-mx-6 px-4 md:px-6 py-10 animate-fade-in relative">
            <div className="max-w-7xl mx-auto">
            {/* Success Popup */}
            {showSuccessPopup && (
                <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-[100] flex items-center justify-center p-4">
                    <div className="bg-[#FFF8ED] max-w-sm w-full p-8 border border-[#9A6A2F]/25 shadow-[0_40px_120px_rgba(47,36,24,0.18)] text-center animate-fade-in">
                        <div className="w-14 h-14 bg-emerald-500/10 border border-emerald-400/20 flex items-center justify-center mx-auto mb-4">
                            <CheckCircle className="w-7 h-7 text-emerald-300" />
                        </div>
                        <h3 className="font-serif text-2xl text-[#2F2418] mb-2">{successPopup.title}</h3>
                        <p className="text-sm text-[#2F2418]/55 mb-6">{successPopup.message}</p>
                        <button onClick={() => setShowSuccessPopup(false)} className="w-full py-3 justify-center inline-flex items-center bg-[#9A6A2F] text-[#F8F1E6] font-bold">Hoàn tất</button>
                    </div>
                </div>
            )}

            {/* Deposit Modal */}
            {showDepositModal && (
                <div className="fixed inset-0 bg-black/80 backdrop-blur-md z-[100] flex items-center justify-center p-4">
                    <div className="bg-[#FFF8ED] max-w-md w-full p-8 border border-[#9A6A2F]/25 shadow-[0_40px_120px_rgba(47,36,24,0.18)] animate-fade-in">
                        <h3 className="font-serif text-2xl text-[#2F2418] mb-6">Nạp Tiền Vào Ví</h3>
                        <div className="space-y-5">
                            <div>
                                <label className="text-sm font-semibold text-[#9A6A2F] mb-2 block">Số tiền (VNĐ)</label>
                                <input type="number" min="10000" step="10000" placeholder="100000" 
                                    className="w-full bg-[#F8F1E6] border border-[#9A6A2F]/25 px-4 py-2.5 text-sm text-[#2F2418] placeholder:text-[#2F2418]/30 focus:outline-none focus:ring-2 focus:ring-[#9A6A2F]/20 focus:border-[#9A6A2F]/60"
                                    value={depositAmount} onChange={(e) => setDepositAmount(e.target.value)} />
                            </div>
                            <div>
                                <label className="text-sm font-semibold text-[#9A6A2F] mb-3 block text-center">Chọn phương thức</label>
                                <div className="flex gap-3">
                                    <button onClick={() => handleDeposit('vnpay')} className="flex-1 border border-[#9A6A2F]/25 py-3 flex flex-col items-center gap-1 bg-[#F8F1E6] hover:bg-[#9A6A2F]/10 hover:border-[#9A6A2F]/50 transition-all">
                                        <span className="font-bold text-[#2F2418] text-sm">VNPay</span>
                                    </button>
                                    <button onClick={() => handleDeposit('momo')} className="flex-1 border border-[#9A6A2F]/25 py-3 flex flex-col items-center gap-1 bg-[#F8F1E6] hover:bg-[#9A6A2F]/10 hover:border-[#9A6A2F]/50 transition-all">
                                        <span className="font-bold text-[#2F2418] text-sm">MoMo</span>
                                    </button>
                                </div>
                            </div>
                            <button onClick={() => setShowDepositModal(false)} className="w-full py-2.5 border border-[#9A6A2F]/25 text-sm font-semibold text-[#2F2418]/70 hover:bg-white/[0.04] hover:text-[#2F2418]">Hủy</button>
                        </div>
                    </div>
                </div>
            )}

            {/* PIN Modal */}
            {showPinModal && (
                <div className="fixed inset-0 bg-black/80 backdrop-blur-md z-[100] flex items-center justify-center p-4">
                    <div className="bg-[#FFF8ED] max-w-md w-full p-8 border border-[#9A6A2F]/25 shadow-[0_40px_120px_rgba(47,36,24,0.18)] animate-fade-in">
                        <h3 className="font-serif text-2xl text-[#2F2418] mb-6">Cài đặt Mã PIN</h3>
                        <div id="recaptcha-container" className="mb-4"></div>

                        {pinMessage && (
                            <div className={`mb-5 flex items-start gap-3 border px-4 py-3 text-sm animate-fade-in ${pinMessage.type === 'success' ? 'bg-emerald-500/10 border-emerald-400/25 text-emerald-700' : 'bg-red-500/10 border-red-400/25 text-red-700'}`}>
                                {pinMessage.type === 'success' ? <CheckCircle className="w-4 h-4 mt-0.5 shrink-0" /> : <AlertCircle className="w-4 h-4 mt-0.5 shrink-0" />}
                                <span>{pinMessage.text}</span>
                            </div>
                        )}

                        {pinStep === 1 && (
                            <div className="space-y-5">
                                <div>
                                    <label className="text-sm font-semibold text-[#9A6A2F] mb-2 block">Số điện thoại xác thực</label>
                                    <div className="w-full bg-[#F8F1E6] border border-[#9A6A2F]/25 px-4 py-2.5 text-sm text-[#2F2418]/70">{profile?.phone_number}</div>
                                    <p className="text-xs text-[#2F2418]/45 mt-2">OTP sẽ gửi qua SMS đến số đăng ký.</p>
                                </div>
                                <div className="flex gap-3">
                                    <button onClick={closePinModal} className="flex-1 py-2.5 border border-[#9A6A2F]/25 text-sm font-semibold text-[#2F2418]/70 hover:bg-white/[0.04] hover:text-[#2F2418]">Hủy</button>
                                    <button onClick={handleSendOTP} disabled={pinLoading} className="flex-1 bg-[#9A6A2F] text-[#F8F1E6] font-bold py-2.5 justify-center disabled:opacity-50 inline-flex items-center">
                                        {pinLoading ? <Loader className="w-4 h-4 animate-spin" /> : 'Gửi OTP'}
                                    </button>
                                </div>
                            </div>
                        )}
                        {pinStep === 2 && (
                            <div className="space-y-5">
                                <div>
                                    <label className="text-sm font-semibold text-[#9A6A2F] mb-2 block">Nhập mã OTP</label>
                                    <input type="text" placeholder="6 chữ số" maxLength={6} value={otp} onChange={(e) => setOtp(e.target.value)}
                                        className="w-full bg-[#F8F1E6] border border-[#9A6A2F]/25 px-4 py-2.5 text-sm text-center tracking-[0.5em] font-bold text-[#2F2418] focus:outline-none focus:ring-2 focus:ring-[#9A6A2F]/20 focus:border-[#9A6A2F]/60" />
                                    <div className="text-center mt-3">
                                        {countdown > 0 ? <span className="text-xs text-[#2F2418]/45">Gửi lại sau {countdown}s</span>
                                            : <button onClick={handleSendOTP} disabled={pinLoading} className="text-xs text-[#9A6A2F] font-bold hover:underline">Gửi lại OTP</button>}
                                    </div>
                                </div>
                                <div className="flex gap-3">
                                    <button onClick={closePinModal} className="flex-1 py-2.5 border border-[#9A6A2F]/25 text-sm font-semibold text-[#2F2418]/70 hover:bg-white/[0.04] hover:text-[#2F2418]">Hủy</button>
                                    <button onClick={handleVerifyOTP} disabled={pinLoading} className="flex-1 bg-[#9A6A2F] text-[#F8F1E6] font-bold py-2.5 justify-center disabled:opacity-50 inline-flex items-center">
                                        {pinLoading ? <Loader className="w-4 h-4 animate-spin" /> : 'Xác thực'}
                                    </button>
                                </div>
                            </div>
                        )}
                        {pinStep === 3 && (
                            <div className="space-y-5">
                                <div>
                                    <label className="text-sm font-semibold text-[#9A6A2F] mb-2 block">Mã PIN mới (6 số)</label>
                                    <input type="password" placeholder="••••••" maxLength={6} value={newPin} onChange={(e) => setNewPin(e.target.value)}
                                        className="w-full bg-[#F8F1E6] border border-[#9A6A2F]/25 px-4 py-3 text-center tracking-[0.5em] text-2xl font-bold text-[#2F2418] focus:outline-none focus:ring-2 focus:ring-[#9A6A2F]/20 focus:border-[#9A6A2F]/60" />
                                </div>
                                <div className="flex gap-3">
                                    <button onClick={closePinModal} className="flex-1 py-2.5 border border-[#9A6A2F]/25 text-sm font-semibold text-[#2F2418]/70 hover:bg-white/[0.04] hover:text-[#2F2418]">Hủy</button>
                                    <button onClick={handleSetupPIN} disabled={pinLoading} className="flex-1 bg-[#9A6A2F] text-[#F8F1E6] font-bold py-2.5 justify-center disabled:opacity-50 inline-flex items-center">
                                        {pinLoading ? <Loader className="w-4 h-4 animate-spin" /> : 'Lưu PIN'}
                                    </button>
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            )}

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

            {/* Page Header */}
            <div className="mb-10 border-l border-[#9A6A2F]/40 pl-5">
                <p className="text-[#9A6A2F] text-xs font-semibold tracking-[0.28em] uppercase mb-3">Private Account</p>
                <h1 className="font-serif text-4xl md:text-5xl text-[#2F2418]">Hồ Sơ Của Tôi</h1>
            </div>

            <div className="flex flex-col md:flex-row gap-8">
                {/* Left Sidebar */}
                <div className="w-full md:w-80 shrink-0 space-y-6">
                    {/* Profile Card */}
                    <div className="bg-[#FFF8ED] border border-[#9A6A2F]/20 p-6 text-center shadow-[0_28px_90px_rgba(47,36,24,0.10)]">
                        <div className="w-20 h-20 border border-[#9A6A2F]/45 bg-[#F8F1E6] flex items-center justify-center mx-auto mb-4 text-[#9A6A2F]">
                            <UserIcon className="w-8 h-8" />
                        </div>
                        <h2 className="font-serif text-xl text-[#2F2418]">{profile.full_name}</h2>
                        <p className="text-sm text-[#2F2418]/55 mb-4">{profile.email}</p>
                        <div className="inline-flex items-center gap-1.5 bg-[#9A6A2F]/10 text-[#9A6A2F] border border-[#9A6A2F]/25 px-3 py-1.5 text-xs font-bold">
                            <Award className="w-3.5 h-3.5" /> Uy tín: {profile.reputation_score}/100
                        </div>
                    </div>

                    {/* Wallet Card */}
                    <div className="bg-[#FFF8ED] border border-[#9A6A2F]/20 p-6 shadow-[0_28px_90px_rgba(47,36,24,0.10)]">
                        <h3 className="font-serif text-xl text-[#2F2418] mb-5 flex items-center gap-2">
                            <WalletIcon className="w-5 h-5 text-[#9A6A2F]" /> Ví của tôi
                        </h3>
                        <div className="space-y-3 mb-5">
                            <div className="flex justify-between text-sm">
                                <span className="text-[#2F2418]/55">Khả dụng</span>
                                <span className="font-bold text-[#2F2418]">{(profile.wallet?.available_balance || 0).toLocaleString('vi-VN')} đ</span>
                            </div>
                            <div className="flex justify-between text-sm">
                                <span className="text-[#2F2418]/55">Đóng băng</span>
                                <span className="font-bold text-red-300">{(profile.wallet?.frozen_balance || 0).toLocaleString('vi-VN')} đ</span>
                            </div>
                        </div>
                        <button onClick={() => setShowDepositModal(true)} className="w-full py-3 justify-center mb-3 text-sm inline-flex items-center gap-2 bg-[#9A6A2F] hover:bg-[#2F2418] text-[#F8F1E6] font-bold transition-colors">
                            <CreditCard className="w-4 h-4" /> Nạp tiền
                        </button>
                        {profile.wallet?.has_pin ? (
                            <div className="space-y-2">
                                <div className="flex items-center justify-between text-sm bg-emerald-500/10 border border-emerald-400/20 px-3 py-2">
                                    <span className="flex items-center gap-1.5 text-emerald-300"><Lock className="w-3.5 h-3.5" /> PIN</span>
                                    <span className="text-xs font-bold text-emerald-300">Đã thiết lập</span>
                                </div>
                                <div className="flex gap-2">
                                    <button onClick={() => setShowPinModal(true)} className="flex-1 py-2 border border-[#9A6A2F]/25 text-xs font-semibold text-[#2F2418]/65 hover:text-[#2F2418] hover:bg-white/[0.04]">Đổi PIN</button>
                                    <button onClick={() => setShowPinModal(true)} className="flex-1 py-2 border border-[#9A6A2F]/25 text-xs font-semibold text-[#2F2418]/65 hover:text-[#2F2418] hover:bg-white/[0.04]">Quên PIN</button>
                                </div>
                            </div>
                        ) : (
                            <button onClick={() => setShowPinModal(true)} className="w-full py-2.5 border border-[#9A6A2F]/25 text-sm font-semibold text-[#2F2418]/70 hover:bg-white/[0.04] hover:text-[#2F2418] flex items-center justify-center gap-2">
                                <Lock className="w-4 h-4" /> Cài đặt PIN
                            </button>
                        )}
                    </div>

                    {/* eKYC Card */}
                    <div className="bg-[#FFF8ED] border border-[#9A6A2F]/20 p-6">
                        <div className="flex items-center justify-between mb-3">
                            <h3 className="font-serif text-xl text-[#2F2418] flex items-center gap-2">
                                <ShieldCheck className="w-5 h-5 text-[#9A6A2F]" /> eKYC
                            </h3>
                            <StatusBadge status={profile.verification_status === 'VERIFIED' ? 'COMPLETED' : 'PENDING'} />
                        </div>
                        {profile.verification_status !== 'VERIFIED' && (
                            <button onClick={() => navigate('/ekyc')} className="w-full py-2.5 justify-center text-sm mt-2 inline-flex items-center bg-[#9A6A2F] text-[#F8F1E6] font-bold">
                                Xác thực ngay
                            </button>
                        )}
                    </div>

                    {/* Admin Card */}
                    {profile.is_admin && (
                        <div className="card-luxury p-6 bg-gradient-to-br from-slate-900 to-slate-800 text-white border-0">
                            <h3 className="font-bold mb-2 flex items-center gap-2">
                                <Settings className="w-5 h-5 text-primary" /> Quản trị viên
                            </h3>
                            <p className="text-xs text-slate-300 mb-4">Quản lý hệ thống đấu giá</p>
                            <button onClick={() => navigate('/admin/disputes')} className="btn-primary w-full py-2.5 justify-center text-sm">
                                Xử lý tranh chấp
                            </button>
                        </div>
                    )}

                    {/* Logout */}
                    <button onClick={handleLogout} className="w-full flex items-center justify-center gap-2 py-3 text-red-300 hover:text-red-200 hover:bg-red-500/10 text-sm font-semibold transition-colors border border-red-400/15">
                        <LogOut className="w-4 h-4" /> Đăng xuất
                    </button>
                </div>

                {/* Right Content */}
                <div className="flex-1 space-y-6">
                    {/* Tabs */}
                    <div className="flex gap-1 bg-[#FFF8ED] border border-[#9A6A2F]/20 p-1">
                        <button onClick={() => setActiveTab('purchases')}
                            className={`flex-1 py-3 text-sm font-semibold transition-all flex items-center justify-center gap-2 ${activeTab === 'purchases' ? 'bg-[#9A6A2F] text-[#F8F1E6]' : 'text-[#2F2418]/55 hover:text-[#2F2418] hover:bg-white/[0.04]'}`}>
                            <Package className="w-4 h-4" /> Đơn mua ({purchases.length})
                        </button>
                        <button onClick={() => setActiveTab('sales')}
                            className={`flex-1 py-3 text-sm font-semibold transition-all flex items-center justify-center gap-2 ${activeTab === 'sales' ? 'bg-[#9A6A2F] text-[#F8F1E6]' : 'text-[#2F2418]/55 hover:text-[#2F2418] hover:bg-white/[0.04]'}`}>
                            <Truck className="w-4 h-4" /> Đơn bán ({sales.length})
                        </button>
                    </div>

                    {/* Purchases Tab */}
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
                                            <tr key={p.invoice_id} className="border-b border-white/5 hover:bg-white/[0.03] transition-colors">
                                                <td className="px-5 py-5 text-sm font-semibold text-[#2F2418]">{p.auction_name}</td>
                                                <td className="px-5 py-5 text-xs text-[#2F2418]/50">{p.created_at}</td>
                                                <td className="px-5 py-5 text-sm text-right font-bold text-[#9A6A2F]">{p.total_amount.toLocaleString('vi-VN')} đ</td>
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

                    {/* Sales Tab */}
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
            </div>
        </div>
    );
}
