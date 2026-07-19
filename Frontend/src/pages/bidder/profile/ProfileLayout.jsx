import { useEffect, useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { LogOut, User as UserIcon, Award, Wallet as WalletIcon, Package, Headphones, Settings, Loader } from 'lucide-react';
import useAuthStore from '@/store/useAuthStore';
import { authApi } from '@/features/auth/api';

export default function ProfileLayout() {
    const navigate = useNavigate();
    const { user: profile, setUser } = useAuthStore();
    const [loading, setLoading] = useState(!profile);

    const fetchProfile = () => {
        authApi.getMyInfo()
            .then(res => {
                setUser(res.result || res);
                setLoading(false);
            })
            .catch(() => { navigate('/login'); });
    };

    useEffect(() => {
        fetchProfile();
    }, [navigate, setUser]);

    const handleLogout = () => {
        useAuthStore.getState().logout();
        navigate('/login');
    };

    if (loading || !profile) return (
        <div className="min-h-screen flex items-center justify-center">
            <Loader className="w-8 h-8 text-[#9A6A2F] animate-spin" />
        </div>
    );

    return (
        <div className="min-h-screen bg-[#F8F1E6] -mx-4 md:-mx-6 px-4 md:px-6 py-10 relative">
            <div className="max-w-7xl mx-auto">
                {/* Page Header */}
                <div className="mb-10 border-l border-[#9A6A2F]/40 pl-5">
                    <p className="text-[#9A6A2F] text-xs font-semibold tracking-[0.28em] uppercase mb-3">Private Account</p>
                    <h1 className="font-serif text-4xl md:text-5xl text-[#2F2418]">Hồ Sơ Của Tôi</h1>
                </div>

                <div className="flex flex-col md:flex-row gap-8">
                    {/* Left Sidebar Navigation */}
                    <div className="w-full md:w-72 shrink-0 flex flex-col gap-6">
                        {/* Profile Card */}
                        <div className="bg-[#FFF8ED] border border-[#9A6A2F]/20 p-6 text-center shadow-[0_28px_90px_rgba(47,36,24,0.10)]">
                            <div className="w-20 h-20 border border-[#9A6A2F]/45 bg-[#F8F1E6] flex items-center justify-center mx-auto mb-4 text-[#9A6A2F] overflow-hidden rounded-full">
                                {profile?.avatarImage ? (
                                    <img src={profile.avatarImage} alt="Avatar" className="w-full h-full object-cover" />
                                ) : (
                                    <UserIcon className="w-8 h-8" />
                                )}
                            </div>
                            <h2 className="font-serif text-xl text-[#2F2418]">{profile.name || profile.full_name}</h2>
                            <p className="text-sm text-[#2F2418]/55 mb-4">{profile.email}</p>
                            <div className="inline-flex items-center gap-1.5 bg-[#9A6A2F]/10 text-[#9A6A2F] border border-[#9A6A2F]/25 px-3 py-1.5 text-xs font-bold mb-3">
                                <Award className="w-3.5 h-3.5" /> Uy tín: {profile.reputationScore ?? profile.reputation_score ?? 100}/100
                            </div>
                        </div>

                        {/* Navigation Menu */}
                        <div className="bg-[#FFF8ED] border border-[#9A6A2F]/20 shadow-[0_28px_90px_rgba(47,36,24,0.10)] flex flex-col">
                            <NavLink to="/profile/personal" className={({ isActive }) => `flex items-center gap-3 px-6 py-4 text-sm font-semibold transition-colors border-b border-[#9A6A2F]/15 ${isActive ? 'bg-[#9A6A2F] text-[#F8F1E6]' : 'text-[#2F2418]/70 hover:bg-[#9A6A2F]/5'}`}>
                                <UserIcon className="w-5 h-5" /> Hồ sơ cá nhân
                            </NavLink>
                            <NavLink to="/profile/wallet" className={({ isActive }) => `flex items-center gap-3 px-6 py-4 text-sm font-semibold transition-colors border-b border-[#9A6A2F]/15 ${isActive ? 'bg-[#9A6A2F] text-[#F8F1E6]' : 'text-[#2F2418]/70 hover:bg-[#9A6A2F]/5'}`}>
                                <WalletIcon className="w-5 h-5" /> Ví của tôi
                            </NavLink>
                            <NavLink to="/profile/orders" className={({ isActive }) => `flex items-center gap-3 px-6 py-4 text-sm font-semibold transition-colors border-b border-[#9A6A2F]/15 ${isActive ? 'bg-[#9A6A2F] text-[#F8F1E6]' : 'text-[#2F2418]/70 hover:bg-[#9A6A2F]/5'}`}>
                                <Package className="w-5 h-5" /> Quản lý Đơn hàng
                            </NavLink>
                            <NavLink to="/profile/support" className={({ isActive }) => `flex items-center gap-3 px-6 py-4 text-sm font-semibold transition-colors border-b border-[#9A6A2F]/15 ${isActive ? 'bg-[#9A6A2F] text-[#F8F1E6]' : 'text-[#2F2418]/70 hover:bg-[#9A6A2F]/5'}`}>
                                <Headphones className="w-5 h-5" /> Trung tâm hỗ trợ
                            </NavLink>
                            <NavLink to="/profile/settings" className={({ isActive }) => `flex items-center gap-3 px-6 py-4 text-sm font-semibold transition-colors ${isActive ? 'bg-[#9A6A2F] text-[#F8F1E6]' : 'text-[#2F2418]/70 hover:bg-[#9A6A2F]/5'}`}>
                                <Settings className="w-5 h-5" /> Cài đặt hệ thống
                            </NavLink>
                        </div>

                        {/* Admin Card */}
                        {(profile.account?.role?.name === 'ADMIN' || profile.is_admin) && (
                            <div className="p-5 bg-gradient-to-br from-slate-900 to-slate-800 text-white border-0 shadow-lg mt-6">
                                <h3 className="font-bold mb-2 flex items-center gap-2">
                                    <Settings className="w-4 h-4 text-primary" /> Quản trị viên
                                </h3>
                                <button onClick={() => navigate('/admin/disputes')} className="btn-primary w-full py-2 justify-center text-xs">
                                    Xử lý tranh chấp
                                </button>
                            </div>
                        )}

                        {/* Logout */}
                        <div className="mt-6">
                            <button onClick={handleLogout} className="w-full flex items-center justify-center gap-2 py-3 text-red-400 hover:text-red-300 hover:bg-red-500/10 text-sm font-semibold transition-colors border border-red-400/15">
                                <LogOut className="w-4 h-4" /> Đăng xuất
                            </button>
                        </div>
                    </div>

                    {/* Right Content Area */}
                    <div className="flex-1">
                        {/* We pass fetchProfile so children can refresh the profile if they update something */}
                        <Outlet context={{ profile, fetchProfile }} />
                    </div>
                </div>
            </div>
        </div>
    );
}
