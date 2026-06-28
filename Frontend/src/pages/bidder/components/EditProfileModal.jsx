import React, { useState } from 'react';
import { updateMyInfo, updateMyPhone, uploadAvatar, addAddress, deleteAddress } from '@/features/auth/api';
import { X, CheckCircle, AlertCircle, Upload, Trash2, Plus, MapPin } from 'lucide-react';
import { auth } from '@/services/firebase';
import { RecaptchaVerifier, signInWithPhoneNumber } from 'firebase/auth';

const EditProfileModal = ({ profile, onClose, onSuccess }) => {
    const [activeTab, setActiveTab] = useState('basic'); // 'basic', 'phone', 'address'
    
    // Basic Info States
    const [formData, setFormData] = useState({
        name: profile?.name || '',
        email: profile?.email || '',
        dob: profile?.dob || '',
        gender: profile?.gender !== undefined ? profile.gender : true,
    });
    const [avatarFile, setAvatarFile] = useState(null);
    const [avatarPreview, setAvatarPreview] = useState(profile?.avatarImage || '');
    
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    // Phone update states
    const [newPhone, setNewPhone] = useState('');
    const [otp, setOtp] = useState('');
    const [otpSent, setOtpSent] = useState(false);
    const [confirmationResult, setConfirmationResult] = useState(null);

    // Address states
    const [newAddress, setNewAddress] = useState({ addressLine: '', city: '', district: '', ward: '', isDefault: false });
    const [showAddAddress, setShowAddAddress] = useState(false);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleAvatarChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            setAvatarFile(file);
            setAvatarPreview(URL.createObjectURL(file));
        }
    };

    const handleSaveBasicInfo = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);
        try {
            if (avatarFile) {
                await uploadAvatar(avatarFile);
            }
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

    const handleAddAddress = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);
        try {
            await addAddress(newAddress);
            await onSuccess(); // refresh profile data
            setNewAddress({ addressLine: '', city: '', district: '', ward: '', isDefault: false });
            setShowAddAddress(false);
        } catch (err) {
            setError(err?.response?.data?.message || 'Không thể thêm địa chỉ.');
        } finally {
            setLoading(false);
        }
    };

    const handleDeleteAddress = async (id) => {
        if (!window.confirm("Bạn có chắc chắn muốn xóa địa chỉ này?")) return;
        setLoading(true);
        setError(null);
        try {
            await deleteAddress(id);
            await onSuccess();
        } catch (err) {
            setError(err?.response?.data?.message || 'Không thể xóa địa chỉ.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-md z-[100] flex items-center justify-center p-4">
            <div className="bg-[#FFF8ED] max-w-md w-full p-8 border border-[#9A6A2F]/25 shadow-[0_40px_120px_rgba(47,36,24,0.18)] animate-fade-in relative max-h-[90vh] overflow-y-auto overflow-x-hidden">
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

                <div className="flex gap-4 mb-6 border-b border-[#9A6A2F]/20 pb-2 overflow-x-auto whitespace-nowrap">
                    <button 
                        className={`font-semibold text-sm pb-2 ${activeTab === 'basic' ? 'text-[#9A6A2F] border-b-2 border-[#9A6A2F]' : 'text-[#2F2418]/50 hover:text-[#2F2418]'}`}
                        onClick={() => setActiveTab('basic')}
                    >
                        Thông tin
                    </button>
                    <button 
                        className={`font-semibold text-sm pb-2 ${activeTab === 'phone' ? 'text-[#9A6A2F] border-b-2 border-[#9A6A2F]' : 'text-[#2F2418]/50 hover:text-[#2F2418]'}`}
                        onClick={() => setActiveTab('phone')}
                    >
                        SĐT
                    </button>
                    <button 
                        className={`font-semibold text-sm pb-2 ${activeTab === 'address' ? 'text-[#9A6A2F] border-b-2 border-[#9A6A2F]' : 'text-[#2F2418]/50 hover:text-[#2F2418]'}`}
                        onClick={() => setActiveTab('address')}
                    >
                        Địa chỉ
                    </button>
                </div>

                {activeTab === 'basic' && (
                    <form onSubmit={handleSaveBasicInfo} className="space-y-4">
                        <div className="flex flex-col items-center mb-4">
                            <div className="w-20 h-20 bg-[#F8F1E6] border border-[#9A6A2F]/45 mb-2 overflow-hidden flex items-center justify-center relative group">
                                {avatarPreview ? (
                                    <img src={avatarPreview} alt="Avatar" className="w-full h-full object-cover" />
                                ) : (
                                    <span className="text-[#9A6A2F]/50 text-xs">No img</span>
                                )}
                                <label className="absolute inset-0 bg-black/40 flex items-center justify-center opacity-0 group-hover:opacity-100 cursor-pointer transition-opacity">
                                    <Upload className="w-5 h-5 text-white" />
                                    <input type="file" className="hidden" accept="image/*" onChange={handleAvatarChange} />
                                </label>
                            </div>
                            <span className="text-xs text-[#2F2418]/50">Bấm vào ảnh để đổi</span>
                        </div>

                        <div>
                            <label className="text-sm font-semibold text-[#9A6A2F] mb-1 block">Họ và tên</label>
                            <input 
                                type="text" name="name" value={formData.name} onChange={handleInputChange} 
                                className="w-full bg-[#F8F1E6] border border-[#9A6A2F]/25 px-4 py-2.5 text-sm text-[#2F2418] focus:outline-none focus:border-[#9A6A2F]/60"
                                required
                            />
                        </div>
                        <div>
                            <label className="text-sm font-semibold text-[#9A6A2F] mb-1 block">Email</label>
                            <input 
                                type="email" name="email" value={formData.email} onChange={handleInputChange} 
                                className="w-full bg-[#F8F1E6] border border-[#9A6A2F]/25 px-4 py-2.5 text-sm text-[#2F2418] focus:outline-none focus:border-[#9A6A2F]/60"
                                required
                            />
                        </div>
                        <div>
                            <label className="text-sm font-semibold text-[#9A6A2F] mb-1 block">Ngày sinh</label>
                            <input 
                                type="date" name="dob" value={formData.dob} onChange={handleInputChange} 
                                className="w-full bg-[#F8F1E6] border border-[#9A6A2F]/25 px-4 py-2.5 text-sm text-[#2F2418] focus:outline-none focus:border-[#9A6A2F]/60"
                            />
                        </div>
                        <div>
                            <label className="text-sm font-semibold text-[#9A6A2F] mb-1 block">Giới tính</label>
                            <select 
                                name="gender" value={formData.gender} onChange={handleInputChange} 
                                className="w-full bg-[#F8F1E6] border border-[#9A6A2F]/25 px-4 py-2.5 text-sm text-[#2F2418] focus:outline-none focus:border-[#9A6A2F]/60"
                            >
                                <option value={true}>Nam</option>
                                <option value={false}>Nữ</option>
                            </select>
                        </div>
                        <button type="submit" disabled={loading} className="w-full py-3 mt-4 bg-[#9A6A2F] text-[#F8F1E6] font-bold disabled:opacity-50">
                            {loading ? 'Đang lưu...' : 'Lưu Thay Đổi'}
                        </button>
                    </form>
                )}

                {activeTab === 'phone' && (
                    <div className="space-y-4">
                        <div id="recaptcha-phone-container"></div>
                        {!otpSent ? (
                            <>
                                <div>
                                    <label className="text-sm font-semibold text-[#9A6A2F] mb-1 block">Số điện thoại mới</label>
                                    <input 
                                        type="tel" value={newPhone} onChange={(e) => setNewPhone(e.target.value)} 
                                        placeholder="Ví dụ: 0912345678"
                                        className="w-full bg-[#F8F1E6] border border-[#9A6A2F]/25 px-4 py-2.5 text-sm text-[#2F2418] focus:outline-none focus:border-[#9A6A2F]/60"
                                    />
                                </div>
                                <button onClick={handleSendOtp} disabled={loading} className="w-full py-3 mt-2 bg-[#9A6A2F] text-[#F8F1E6] font-bold disabled:opacity-50">
                                    {loading ? 'Đang gửi OTP...' : 'Gửi Mã OTP'}
                                </button>
                            </>
                        ) : (
                            <>
                                <div>
                                    <label className="text-sm font-semibold text-[#9A6A2F] mb-1 block">Nhập mã OTP</label>
                                    <input 
                                        type="text" value={otp} onChange={(e) => setOtp(e.target.value)} 
                                        placeholder="6 số OTP"
                                        className="w-full bg-[#F8F1E6] border border-[#9A6A2F]/25 px-4 py-2.5 text-sm text-[#2F2418] tracking-[0.5em] text-center font-bold focus:outline-none focus:border-[#9A6A2F]/60"
                                    />
                                </div>
                                <button onClick={handleVerifyOtp} disabled={loading || !otp} className="w-full py-3 mt-2 bg-[#9A6A2F] text-[#F8F1E6] font-bold disabled:opacity-50">
                                    {loading ? 'Đang xác thực...' : 'Xác Nhận Đổi Số'}
                                </button>
                                <button onClick={() => { setOtpSent(false); setOtp(''); }} className="w-full py-2.5 border border-[#9A6A2F]/25 text-sm font-semibold text-[#2F2418]/70 hover:bg-white/[0.04] mt-2">
                                    Quay lại
                                </button>
                            </>
                        )}
                    </div>
                )}

                {activeTab === 'address' && (
                    <div className="space-y-4">
                        {profile?.addresses?.length > 0 ? (
                            <div className="space-y-3 max-h-60 overflow-y-auto pr-2 custom-scrollbar">
                                {profile.addresses.map(addr => (
                                    <div key={addr.id} className="p-3 border border-[#9A6A2F]/20 bg-[#F8F1E6] flex items-start justify-between">
                                        <div>
                                            <p className="text-sm text-[#2F2418] flex items-center gap-1"><MapPin className="w-3.5 h-3.5 text-[#9A6A2F]" /> {addr.addressLine}</p>
                                            <p className="text-xs text-[#2F2418]/60 mt-1">{addr.ward}, {addr.district}, {addr.city}</p>
                                            {addr.isDefault && <span className="inline-block mt-2 text-[10px] bg-emerald-500/10 text-emerald-600 px-2 py-0.5 border border-emerald-500/20">Mặc định</span>}
                                        </div>
                                        <button onClick={() => handleDeleteAddress(addr.id)} className="text-red-500/70 hover:text-red-500 p-1">
                                            <Trash2 className="w-4 h-4" />
                                        </button>
                                    </div>
                                ))}
                            </div>
                        ) : (
                            <p className="text-sm text-[#2F2418]/50 text-center py-4">Chưa có địa chỉ nào.</p>
                        )}

                        {!showAddAddress ? (
                            <button onClick={() => setShowAddAddress(true)} className="w-full py-2.5 border border-[#9A6A2F] text-sm font-semibold text-[#9A6A2F] hover:bg-[#9A6A2F]/5 flex items-center justify-center gap-2">
                                <Plus className="w-4 h-4" /> Thêm địa chỉ mới
                            </button>
                        ) : (
                            <form onSubmit={handleAddAddress} className="space-y-3 p-4 border border-[#9A6A2F]/20 bg-white/40 mt-4">
                                <div>
                                    <input type="text" placeholder="Địa chỉ cụ thể (Số nhà, đường...)" required
                                        value={newAddress.addressLine} onChange={e => setNewAddress({...newAddress, addressLine: e.target.value})}
                                        className="w-full bg-[#F8F1E6] border border-[#9A6A2F]/25 px-3 py-2 text-sm text-[#2F2418] focus:outline-none focus:border-[#9A6A2F]/60" />
                                </div>
                                <div className="grid grid-cols-2 gap-3">
                                    <input type="text" placeholder="Phường/Xã" required
                                        value={newAddress.ward} onChange={e => setNewAddress({...newAddress, ward: e.target.value})}
                                        className="w-full bg-[#F8F1E6] border border-[#9A6A2F]/25 px-3 py-2 text-sm text-[#2F2418] focus:outline-none focus:border-[#9A6A2F]/60" />
                                    <input type="text" placeholder="Quận/Huyện" required
                                        value={newAddress.district} onChange={e => setNewAddress({...newAddress, district: e.target.value})}
                                        className="w-full bg-[#F8F1E6] border border-[#9A6A2F]/25 px-3 py-2 text-sm text-[#2F2418] focus:outline-none focus:border-[#9A6A2F]/60" />
                                </div>
                                <div>
                                    <input type="text" placeholder="Tỉnh/Thành phố" required
                                        value={newAddress.city} onChange={e => setNewAddress({...newAddress, city: e.target.value})}
                                        className="w-full bg-[#F8F1E6] border border-[#9A6A2F]/25 px-3 py-2 text-sm text-[#2F2418] focus:outline-none focus:border-[#9A6A2F]/60" />
                                </div>
                                <label className="flex items-center gap-2 text-sm text-[#2F2418] mt-2 cursor-pointer">
                                    <input type="checkbox" checked={newAddress.isDefault} onChange={e => setNewAddress({...newAddress, isDefault: e.target.checked})} className="accent-[#9A6A2F]" />
                                    Đặt làm địa chỉ mặc định
                                </label>
                                <div className="flex gap-2 mt-4">
                                    <button type="button" onClick={() => setShowAddAddress(false)} className="flex-1 py-2 border border-[#9A6A2F]/25 text-sm font-semibold text-[#2F2418]/70 hover:bg-white/[0.04]">Hủy</button>
                                    <button type="submit" disabled={loading} className="flex-1 bg-[#9A6A2F] text-[#F8F1E6] font-bold py-2 disabled:opacity-50">Lưu</button>
                                </div>
                            </form>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};

export default EditProfileModal;
