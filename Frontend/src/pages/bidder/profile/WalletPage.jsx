import { useState, useEffect } from 'react';
import { useOutletContext, useLocation, useNavigate } from 'react-router-dom';
import { Wallet as WalletIcon, ArrowDownLeft, ArrowUpRight, Lock, CheckCircle, AlertCircle, Loader } from 'lucide-react';
import { RecaptchaVerifier, signInWithPhoneNumber } from 'firebase/auth';
import { auth } from '@/services/firebase';
import { verifyVnpayReturn } from '@/features/payment/api';
import { requestDeposit, requestWithdraw, setupPin as setupPinApi } from '@/features/wallet/api';

export default function WalletPage() {
    const { profile, fetchProfile } = useOutletContext();
    const location = useLocation();
    const navigate = useNavigate();

    const [showDepositModal, setShowDepositModal] = useState(false);
    const [depositAmount, setDepositAmount] = useState(50000);
    const [showWithdrawModal, setShowWithdrawModal] = useState(false);
    const [withdrawInfo, setWithdrawInfo] = useState({ bank: '', account_number: '', amount: '' });
    
    const [showSuccessPopup, setShowSuccessPopup] = useState(false);
    const [successPopup, setSuccessPopup] = useState({ title: '', message: '' });

    const [showPinModal, setShowPinModal] = useState(false);
    const [pinStep, setPinStep] = useState(1);
    const [otp, setOtp] = useState('');
    const [newPin, setNewPin] = useState('');
    const [confirmationResult, setConfirmationResult] = useState(null);
    const [countdown, setCountdown] = useState(0);
    const [pinLoading, setPinLoading] = useState(false);
    const [pinMessage, setPinMessage] = useState(null);

    useEffect(() => {
        const handlePaymentCallback = async () => {
            const queryParams = new URLSearchParams(location.search);
            
            if (queryParams.has('vnp_SecureHash')) {
                try {
                    const res = await verifyVnpayReturn(location.search);
                    if (res.paymentStatus === 'SUCCESS') {
                        setSuccessPopup({ title: 'Nạp tiền thành công!', message: 'Số dư ví đã được cập nhật qua VNPay.' });
                        setShowSuccessPopup(true);
                        fetchProfile();
                    } else {
                        alert(res.message || 'Thanh toán VNPay thất bại');
                    }
                } catch (error) {
                    alert('Lỗi xác thực thanh toán VNPay');
                }
                navigate('/profile/wallet', { replace: true });
            } else if (queryParams.has('resultCode') && queryParams.has('partnerCode')) {
                if (queryParams.get('resultCode') === '0') {
                     setSuccessPopup({ title: 'Nạp tiền thành công!', message: 'Số dư ví đã được cập nhật qua MoMo.' });
                     setShowSuccessPopup(true);
                     fetchProfile();
                } else {
                     alert(queryParams.get('message') || 'Thanh toán MoMo thất bại');
                }
                navigate('/profile/wallet', { replace: true });
            }
        };

        if (location.search) {
            handlePaymentCallback();
        }
    }, [location, navigate, fetchProfile]);

    useEffect(() => {
        let timer;
        if (countdown > 0) {
            timer = setTimeout(() => setCountdown(countdown - 1), 1000);
        }
        return () => clearTimeout(timer);
    }, [countdown]);

    const handleDeposit = async (provider) => {
        if (!depositAmount || depositAmount < 10000) {
            alert("Vui lòng nhập số tiền hợp lệ (tối thiểu 10.000đ)");
            return;
        }
        try {
            const res = await requestDeposit(depositAmount, provider);
            if (res.result?.payment_url) {
                window.location.href = res.result.payment_url;
            } else if (res.result?.message) {
                alert(res.result.message);
            }
        } catch (error) {
            alert(error.response?.data?.detail || "Lỗi tạo yêu cầu nạp tiền");
        }
    };

    const handleWithdraw = async () => {
        if (!withdrawInfo.bank || !withdrawInfo.account_number || !withdrawInfo.amount || withdrawInfo.amount < 50000) {
            alert("Vui lòng điền đủ thông tin và rút tối thiểu 50.000đ");
            return;
        }
        try {
            await requestWithdraw(withdrawInfo);
            setSuccessPopup({ title: 'Yêu cầu rút tiền thành công!', message: 'Hệ thống đang xử lý giao dịch của bạn.' });
            setShowSuccessPopup(true);
            setShowWithdrawModal(false);
            fetchProfile();
        } catch (error) {
            alert(error.response?.data?.detail || "Lỗi tạo yêu cầu rút tiền. Kiểm tra lại số dư.");
        }
    };

    const setupRecaptcha = async () => {
        if (window.recaptchaVerifier) {
            window.recaptchaVerifier.clear();
            window.recaptchaVerifier = null;
        }
        window.recaptchaVerifier = new RecaptchaVerifier(auth, 'recaptcha-container', { size: 'invisible' });
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
        const phoneToUse = profile?.phone || profile?.phone_number;
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
            await setupPinApi({ firebase_id_token: idToken, new_pin: newPin });
            fetchProfile(); // refresh to update has_pin
            setSuccessPopup({ title: 'Thiết lập PIN thành công!', message: 'Ví của bạn đã được bảo vệ bằng mã PIN mới.' });
            setShowSuccessPopup(true);
            closePinModal();
        } catch (error) {
            setPinMessage({ type: 'error', text: error.response?.data?.detail || 'Không thể thiết lập mã PIN. Vui lòng thử lại.' });
        } finally { setPinLoading(false); }
    };

    const closePinModal = () => {
        setShowPinModal(false); setPinStep(1); setOtp(''); setNewPin('');
        setPinMessage(null);
        if (window.recaptchaVerifier) { window.recaptchaVerifier.clear(); window.recaptchaVerifier = null; }
    };

    return (
        <div className="space-y-6 animate-fade-in">
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
                        <h3 className="font-serif text-2xl text-[#2F2418] mb-6 flex items-center gap-2"><ArrowDownLeft className="w-6 h-6 text-[#9A6A2F]" /> Nạp Tiền Vào Ví</h3>
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

            {/* Withdraw Modal */}
            {showWithdrawModal && (
                <div className="fixed inset-0 bg-black/80 backdrop-blur-md z-[100] flex items-center justify-center p-4">
                    <div className="bg-[#FFF8ED] max-w-md w-full p-8 border border-[#9A6A2F]/25 shadow-[0_40px_120px_rgba(47,36,24,0.18)] animate-fade-in">
                        <h3 className="font-serif text-2xl text-[#2F2418] mb-6 flex items-center gap-2"><ArrowUpRight className="w-6 h-6 text-[#9A6A2F]" /> Rút Tiền</h3>
                        <div className="space-y-4">
                            <div>
                                <label className="text-sm font-semibold text-[#9A6A2F] mb-1.5 block">Ngân hàng hưởng thụ</label>
                                <select 
                                    value={withdrawInfo.bank} 
                                    onChange={(e) => setWithdrawInfo({...withdrawInfo, bank: e.target.value})}
                                    className="w-full bg-[#F8F1E6] border border-[#9A6A2F]/25 px-4 py-2.5 text-sm text-[#2F2418] focus:outline-none focus:ring-2 focus:ring-[#9A6A2F]/20 focus:border-[#9A6A2F]/60"
                                >
                                    <option value="">Chọn ngân hàng</option>
                                    <option value="Vietcombank">Vietcombank</option>
                                    <option value="Techcombank">Techcombank</option>
                                    <option value="MBBank">MB Bank</option>
                                    <option value="BIDV">BIDV</option>
                                    <option value="VietinBank">VietinBank</option>
                                    <option value="ACB">ACB</option>
                                </select>
                            </div>
                            <div>
                                <label className="text-sm font-semibold text-[#9A6A2F] mb-1.5 block">Số tài khoản</label>
                                <input type="text" placeholder="Nhập số tài khoản" value={withdrawInfo.account_number}
                                    onChange={(e) => setWithdrawInfo({...withdrawInfo, account_number: e.target.value})}
                                    className="w-full bg-[#F8F1E6] border border-[#9A6A2F]/25 px-4 py-2.5 text-sm text-[#2F2418] placeholder:text-[#2F2418]/30 focus:outline-none focus:ring-2 focus:ring-[#9A6A2F]/20 focus:border-[#9A6A2F]/60" />
                            </div>
                            <div>
                                <label className="text-sm font-semibold text-[#9A6A2F] mb-1.5 block">Số tiền rút (VNĐ)</label>
                                <input type="number" min="50000" step="10000" placeholder="Tối thiểu 50.000đ" value={withdrawInfo.amount}
                                    onChange={(e) => setWithdrawInfo({...withdrawInfo, amount: e.target.value})}
                                    className="w-full bg-[#F8F1E6] border border-[#9A6A2F]/25 px-4 py-2.5 text-sm text-[#2F2418] placeholder:text-[#2F2418]/30 focus:outline-none focus:ring-2 focus:ring-[#9A6A2F]/20 focus:border-[#9A6A2F]/60" />
                                <p className="text-xs text-[#2F2418]/50 mt-1">Số dư khả dụng: {(profile.wallet?.available_balance || 0).toLocaleString('vi-VN')} đ</p>
                            </div>
                            <div className="flex gap-3 pt-2">
                                <button onClick={() => setShowWithdrawModal(false)} className="flex-1 py-2.5 border border-[#9A6A2F]/25 text-sm font-semibold text-[#2F2418]/70 hover:bg-white/[0.04] hover:text-[#2F2418]">Hủy</button>
                                <button onClick={handleWithdraw} className="flex-1 bg-[#9A6A2F] text-[#F8F1E6] font-bold py-2.5 justify-center inline-flex items-center">Rút tiền</button>
                            </div>
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
                                    <div className="w-full bg-[#F8F1E6] border border-[#9A6A2F]/25 px-4 py-2.5 text-sm text-[#2F2418]/70">{profile?.phone || profile?.phone_number}</div>
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

            <div className="bg-[#FFF8ED] border border-[#9A6A2F]/20 p-8 shadow-[0_28px_90px_rgba(47,36,24,0.10)]">
                <h3 className="font-serif text-2xl text-[#2F2418] mb-6 flex items-center gap-2 border-b border-[#9A6A2F]/15 pb-4">
                    <WalletIcon className="w-6 h-6 text-[#9A6A2F]" /> Quản Lý Ví Điện Tử
                </h3>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-8">
                    <div className="p-6 bg-[#F8F1E6] border border-[#9A6A2F]/25 text-center">
                        <span className="text-sm text-[#2F2418]/60 font-semibold block mb-2">Số dư khả dụng</span>
                        <span className="text-3xl font-bold text-[#9A6A2F]">{(profile.wallet?.available_balance || 0).toLocaleString('vi-VN')} đ</span>
                    </div>
                    <div className="p-6 bg-[#F8F1E6] border border-[#9A6A2F]/25 text-center">
                        <span className="text-sm text-[#2F2418]/60 font-semibold block mb-2">Số dư đóng băng (Đang đấu giá)</span>
                        <span className="text-3xl font-bold text-red-400">{(profile.wallet?.frozen_balance || 0).toLocaleString('vi-VN')} đ</span>
                    </div>
                </div>

                <div className="flex flex-col md:flex-row gap-4 mb-10">
                    <button onClick={() => setShowDepositModal(true)} className="flex-1 py-3.5 flex justify-center items-center gap-2 bg-[#9A6A2F] hover:bg-[#2F2418] text-[#F8F1E6] font-bold transition-colors">
                        <ArrowDownLeft className="w-5 h-5" /> Nạp tiền vào ví
                    </button>
                    <button onClick={() => setShowWithdrawModal(true)} className="flex-1 py-3.5 flex justify-center items-center gap-2 border border-[#9A6A2F] text-[#9A6A2F] hover:bg-[#9A6A2F]/10 font-bold transition-colors">
                        <ArrowUpRight className="w-5 h-5" /> Rút tiền về ngân hàng
                    </button>
                </div>

                <div>
                    <h4 className="font-serif text-xl text-[#2F2418] mb-4 flex items-center gap-2">
                        <Lock className="w-5 h-5 text-[#9A6A2F]" /> Bảo mật ví (Mã PIN)
                    </h4>
                    <p className="text-sm text-[#2F2418]/60 mb-4">Mã PIN được yêu cầu mỗi khi bạn thực hiện nạp, rút hoặc thanh toán từ ví.</p>
                    {profile.wallet?.has_pin ? (
                        <div className="flex gap-4 items-center">
                            <div className="flex items-center gap-2 text-sm bg-emerald-500/10 border border-emerald-400/20 px-4 py-2 text-emerald-600 font-bold">
                                <CheckCircle className="w-4 h-4" /> Đã thiết lập PIN
                            </div>
                            <button onClick={() => setShowPinModal(true)} className="text-sm font-semibold text-[#9A6A2F] hover:underline">Đổi mã PIN</button>
                        </div>
                    ) : (
                        <button onClick={() => setShowPinModal(true)} className="py-2.5 px-6 border border-[#9A6A2F]/25 text-sm font-bold text-[#9A6A2F] hover:bg-[#9A6A2F]/10 inline-flex items-center gap-2">
                            Cài đặt PIN ngay
                        </button>
                    )}
                </div>
            </div>
        </div>
    );
}
