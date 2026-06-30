import { useState } from 'react';
import { useOutletContext, useNavigate } from 'react-router-dom';
import { User as UserIcon, ShieldCheck, Edit3, MapPin, Plus } from 'lucide-react';
import EditProfileModal from '../components/EditProfileModal';

export default function PersonalPage() {
    const { profile, fetchProfile } = useOutletContext();
    const navigate = useNavigate();
    const [showEditProfileModal, setShowEditProfileModal] = useState(false);

    const StatusBadge = ({ status }) => {
        const colors = {
            'PENDING': 'bg-white/5 text-[#2F2418]/60 border-white/10',
            'COMPLETED': 'bg-emerald-500/10 text-emerald-300 border-emerald-400/20',
        };
        const label = status === 'COMPLETED' ? 'Đã xác thực' : 'Chưa xác thực';
        return (
            <span className={`text-xs font-bold px-2.5 py-1 border ${colors[status] || colors.PENDING}`}>
                {label}
            </span>
        );
    };

    return (
        <div className="space-y-6 animate-fade-in">
            <div className="bg-[#FFF8ED] border border-[#9A6A2F]/20 p-8 shadow-[0_28px_90px_rgba(47,36,24,0.10)]">
                <div className="flex items-center justify-between mb-6 border-b border-[#9A6A2F]/15 pb-4">
                    <h3 className="font-serif text-2xl text-[#2F2418] flex items-center gap-2">
                        <UserIcon className="w-6 h-6 text-[#9A6A2F]" /> Thông Tin Cá Nhân
                    </h3>
                    <button onClick={() => setShowEditProfileModal(true)} className="py-2 px-4 flex items-center justify-center gap-2 bg-[#9A6A2F] text-[#F8F1E6] text-sm font-semibold hover:bg-[#2F2418] transition-colors">
                        <Edit3 className="w-4 h-4" /> Cập nhật
                    </button>
                </div>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div>
                        <p className="text-xs text-[#9A6A2F] font-bold uppercase tracking-wider mb-1">Họ và tên</p>
                        <p className="text-[#2F2418] font-medium">{profile.name || profile.full_name}</p>
                    </div>
                    <div>
                        <p className="text-xs text-[#9A6A2F] font-bold uppercase tracking-wider mb-1">Email</p>
                        <p className="text-[#2F2418] font-medium">{profile.email}</p>
                    </div>
                    <div>
                        <p className="text-xs text-[#9A6A2F] font-bold uppercase tracking-wider mb-1">Số điện thoại</p>
                        <p className="text-[#2F2418] font-medium">{profile.phone || profile.phone_number || 'Chưa cập nhật'}</p>
                    </div>
                    <div>
                        <p className="text-xs text-[#9A6A2F] font-bold uppercase tracking-wider mb-1">Ngày sinh</p>
                        <p className="text-[#2F2418] font-medium">{profile.dob || 'Chưa cập nhật'}</p>
                    </div>
                </div>
            </div>



            <div className="bg-[#FFF8ED] border border-[#9A6A2F]/20 p-8 shadow-[0_28px_90px_rgba(47,36,24,0.10)]">
                <div className="flex items-center justify-between mb-6 border-b border-[#9A6A2F]/15 pb-4">
                    <h3 className="font-serif text-2xl text-[#2F2418] flex items-center gap-2">
                        <MapPin className="w-6 h-6 text-[#9A6A2F]" /> Sổ Địa Chỉ
                    </h3>
                    <button onClick={() => setShowEditProfileModal(true)} className="py-2 px-4 flex items-center justify-center gap-2 bg-[#9A6A2F] text-[#F8F1E6] text-sm font-semibold hover:bg-[#2F2418] transition-colors">
                        <Plus className="w-4 h-4" /> Thêm địa chỉ
                    </button>
                </div>
                
                {profile?.addresses?.length > 0 ? (
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        {profile.addresses.map((addr) => (
                            <div key={addr.id} className={`p-5 border bg-[#F8F1E6] relative ${addr.isDefault ? 'border-[#9A6A2F] shadow-sm' : 'border-[#9A6A2F]/20'}`}>
                                {addr.isDefault && (
                                    <span className="absolute top-4 right-4 bg-emerald-500/10 text-emerald-600 border border-emerald-500/20 px-2 py-0.5 text-[10px] font-bold uppercase tracking-wider">
                                        Mặc định
                                    </span>
                                )}
                                <p className="font-semibold text-[#2F2418] text-sm mb-2">{addr.addressLine}</p>
                                <p className="text-sm text-[#2F2418]/60 leading-relaxed">
                                    {addr.ward}, {addr.district}, {addr.city}
                                </p>
                            </div>
                        ))}
                    </div>
                ) : (
                    <div className="text-center py-8">
                        <p className="text-sm text-[#2F2418]/50 mb-4">Bạn chưa thêm địa chỉ nào.</p>
                        <button onClick={() => setShowEditProfileModal(true)} className="text-sm font-semibold text-[#9A6A2F] hover:underline">
                            Thêm địa chỉ ngay
                        </button>
                    </div>
                )}
            </div>
            <div className="bg-[#FFF8ED] border border-[#9A6A2F]/20 p-8 shadow-[0_28px_90px_rgba(47,36,24,0.10)]">
                <div className="flex items-center justify-between mb-4">
                    <h3 className="font-serif text-2xl text-[#2F2418] flex items-center gap-2">
                        <ShieldCheck className="w-6 h-6 text-[#9A6A2F]" /> Xác thực danh tính (eKYC)
                    </h3>
                    <StatusBadge status={(profile.verificationStatus || profile.verification_status) === 'VERIFIED' ? 'COMPLETED' : 'PENDING'} />
                </div>
                <p className="text-sm text-[#2F2418]/60 mb-6">Xác thực danh tính bằng CCCD để có thể tạo phiên đấu giá và đặt giá.</p>
                
                {(profile.verificationStatus || profile.verification_status) !== 'VERIFIED' && (
                    <button onClick={() => navigate('/ekyc')} className="w-full md:w-auto px-8 py-3 justify-center text-sm inline-flex items-center bg-[#9A6A2F] text-[#F8F1E6] font-bold hover:bg-[#2F2418] transition-colors">
                        Tiến hành xác thực ngay
                    </button>
                )}
            </div>

            {showEditProfileModal && (
                <EditProfileModal 
                    profile={profile} 
                    onClose={() => setShowEditProfileModal(false)} 
                    onSuccess={fetchProfile}
                />
            )}
        </div>
    );
}
