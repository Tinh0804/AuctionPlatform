import React, { useState } from 'react';
import { updateMyInfo, updateMyPhone } from '@/features/auth/api';
import { X, CheckCircle, AlertCircle } from 'lucide-react';
import { auth } from '@/config/firebaseConfig';
import { RecaptchaVerifier, signInWithPhoneNumber } from 'firebase/auth';

const EditProfileModal = ({ profile, onClose, onSuccess }) => {
    const [formData, setFormData] = useState({
        name: profile?.name || '',
        email: profile?.email || '',
        dob: profile?.dob || '',
        gender: profile?.gender !== undefined ? profile.gender : true,
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    // Phone update states
    const [isUpdatingPhone, setIsUpdatingPhone] = useState(false);
    const [newPhone, setNewPhone] = useState('');
    const [otp, setOtp] = useState('');
    const [otpSent, setOtpSent] = useState(false);
    const [confirmationResult, setConfirmationResult] = useState(null);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSaveBasicInfo = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);
        try {
            await updateMyInfo({
                name: formData.name,
                email: formData.email,
                dob: formData.dob,
                gender: formData.gender === 'true' || formData.gender === true
            });
            onSuccess();
            onClose();
        } catch (err) {
            setError(err?.response?.data?.message || 'Có lỗi xảy ra khi cập nhật thông tin.');
        } finally {
            setLoading(false);
        }
    };

    const formatPhoneForFirebase = (phone) => {
        const digitsOnly = phone.replace(/[^\d+]/g, '');
        if (digitsOnly.startsWith('0')) {
            return '+84' + digitsOnly.slice(1);
        }
        if (!digitsOnly.startsWith('+')) {
            return '+' + digitsOnly;
        }
        return digitsOnly;
    };

    const handleSendOtp = async () => {
        if (!newPhone) {
            setError('Vui lòng nhập số điện thoại mới');
            return;
        }
        setLoading(true);
        setError(null);
        try {
            if (!window.recaptchaVerifierPhone) {
                window.recaptchaVerifierPhone = new RecaptchaVerifier(auth, 'recaptcha-phone-container', {
                    'size': 'invisible'
                });
            }
            const formattedPhone = formatPhoneForFirebase(newPhone);
            const confirmation = await signInWithPhoneNumber(auth, formattedPhone, window.recaptchaVerifierPhone);
            setConfirmationResult(confirmation);
            setOtpSent(true);
        } catch (err) {
            console.error('Lỗi gửi OTP', err);
            setError('Không thể gửi OTP. Vui lòng kiểm tra lại số điện thoại.');
        } finally {
            setLoading(false);
        }
    };

    const handleVerifyOtp = async () => {
        if (!otp) return;
        setLoading(true);
        setError(null);
        try {
            const result = await confirmationResult.confirm(otp);
            const token = await result.user.getIdToken();
            await updateMyPhone(token);
            onSuccess();
            onClose();
        } catch (err) {
            console.error('Lỗi xác minh OTP', err);
            setError('Mã OTP không chính xác hoặc đã hết hạn.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-md z-[100] flex items-center justify-center p-4">
            <div className="bg-[#FFF8ED] max-w-md w-full p-8 border border-[#9A6A2F]/25 shadow-[0_40px_120px_rgba(47,36,24,0.18)] animate-fade-in relative max-h-[90vh] overflow-y-auto">
                <button onClick={onClose} className="absolute top-4 right-4 text-[#2F2418]/50 hover:text-[#2F2418]">
                    <X className="w-5 h-5" />
                </button>
                
                <h3 className="font-serif text-2xl text-[#2F2418] mb-6">Cập Nhật Thông Tin</h3>

                {error && (
                    <div className="mb-5 flex items-start gap-3 border px-4 py-3 text-sm bg-red-500/10 border-red-400/25 text-red-700">
                        <AlertCircle className="w-4 h-4 mt-0.5 shrink-0" />
                        <p>{error}</p>
                    </div>
                )}

                <div className="flex gap-4 mb-6 border-b border-[#9A6A2F]/20 pb-2">
                    <button 
                        className={`font-semibold text-sm pb-2 ${!isUpdatingPhone ? 'text-[#9A6A2F] border-b-2 border-[#9A6A2F]' : 'text-[#2F2418]/50 hover:text-[#2F2418]'}`}
                        onClick={() => setIsUpdatingPhone(false)}
                    >
                        Thông tin cơ bản
                    </button>
                    <button 
                        className={`font-semibold text-sm pb-2 ${isUpdatingPhone ? 'text-[#9A6A2F] border-b-2 border-[#9A6A2F]' : 'text-[#2F2418]/50 hover:text-[#2F2418]'}`}
                        onClick={() => setIsUpdatingPhone(true)}
                    >
                        Đổi số điện thoại
                    </button>
                </div>

                {!isUpdatingPhone ? (
                    <form onSubmit={handleSaveBasicInfo} className="space-y-4">
                        <div>
                            <label className="text-sm font-semibold text-[#9A6A2F] mb-1 block">Họ và tên</label>
                            <input 
                                type="text" 
                                name="name" 
                                value={formData.name} 
                                onChange={handleInputChange} 
                                className="w-full bg-[#F8F1E6] border border-[#9A6A2F]/25 px-4 py-2.5 text-sm text-[#2F2418] focus:outline-none focus:border-[#9A6A2F]/60"
                                required
                            />
                        </div>
                        <div>
                            <label className="text-sm font-semibold text-[#9A6A2F] mb-1 block">Email</label>
                            <input 
                                type="email" 
                                name="email" 
                                value={formData.email} 
                                onChange={handleInputChange} 
                                className="w-full bg-[#F8F1E6] border border-[#9A6A2F]/25 px-4 py-2.5 text-sm text-[#2F2418] focus:outline-none focus:border-[#9A6A2F]/60"
                                required
                            />
                        </div>
                        <div>
                            <label className="text-sm font-semibold text-[#9A6A2F] mb-1 block">Ngày sinh</label>
                            <input 
                                type="date" 
                                name="dob" 
                                value={formData.dob} 
                                onChange={handleInputChange} 
                                className="w-full bg-[#F8F1E6] border border-[#9A6A2F]/25 px-4 py-2.5 text-sm text-[#2F2418] focus:outline-none focus:border-[#9A6A2F]/60"
                            />
                        </div>
                        <div>
                            <label className="text-sm font-semibold text-[#9A6A2F] mb-1 block">Giới tính</label>
                            <select 
                                name="gender" 
                                value={formData.gender} 
                                onChange={handleInputChange} 
                                className="w-full bg-[#F8F1E6] border border-[#9A6A2F]/25 px-4 py-2.5 text-sm text-[#2F2418] focus:outline-none focus:border-[#9A6A2F]/60"
                            >
                                <option value={true}>Nam</option>
                                <option value={false}>Nữ</option>
                            </select>
                        </div>
                        <button 
                            type="submit" 
                            disabled={loading}
                            className="w-full py-3 mt-4 bg-[#9A6A2F] text-[#F8F1E6] font-bold disabled:opacity-50"
                        >
                            {loading ? 'Đang lưu...' : 'Lưu Thay Đổi'}
                        </button>
                    </form>
                ) : (
                    <div className="space-y-4">
                        <div id="recaptcha-phone-container"></div>
                        {!otpSent ? (
                            <>
                                <div>
                                    <label className="text-sm font-semibold text-[#9A6A2F] mb-1 block">Số điện thoại mới</label>
                                    <input 
                                        type="tel" 
                                        value={newPhone} 
                                        onChange={(e) => setNewPhone(e.target.value)} 
                                        placeholder="Ví dụ: 0912345678"
                                        className="w-full bg-[#F8F1E6] border border-[#9A6A2F]/25 px-4 py-2.5 text-sm text-[#2F2418] focus:outline-none focus:border-[#9A6A2F]/60"
                                    />
                                </div>
                                <button 
                                    onClick={handleSendOtp} 
                                    disabled={loading}
                                    className="w-full py-3 mt-2 bg-[#9A6A2F] text-[#F8F1E6] font-bold disabled:opacity-50"
                                >
                                    {loading ? 'Đang gửi OTP...' : 'Gửi Mã OTP'}
                                </button>
                            </>
                        ) : (
                            <>
                                <div>
                                    <label className="text-sm font-semibold text-[#9A6A2F] mb-1 block">Nhập mã OTP</label>
                                    <input 
                                        type="text" 
                                        value={otp} 
                                        onChange={(e) => setOtp(e.target.value)} 
                                        placeholder="6 số OTP"
                                        className="w-full bg-[#F8F1E6] border border-[#9A6A2F]/25 px-4 py-2.5 text-sm text-[#2F2418] tracking-[0.5em] text-center font-bold focus:outline-none focus:border-[#9A6A2F]/60"
                                    />
                                </div>
                                <button 
                                    onClick={handleVerifyOtp} 
                                    disabled={loading || !otp}
                                    className="w-full py-3 mt-2 bg-[#9A6A2F] text-[#F8F1E6] font-bold disabled:opacity-50"
                                >
                                    {loading ? 'Đang xác thực...' : 'Xác Nhận Đổi Số'}
                                </button>
                                <button 
                                    onClick={() => { setOtpSent(false); setOtp(''); }} 
                                    className="w-full py-2.5 border border-[#9A6A2F]/25 text-sm font-semibold text-[#2F2418]/70 hover:bg-white/[0.04] mt-2"
                                >
                                    Quay lại
                                </button>
                            </>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};

export default EditProfileModal;
