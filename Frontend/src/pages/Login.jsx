import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Gavel, Eye, EyeOff, User, Mail, Phone, Lock, UserPlus, LogIn, CheckCircle2, Sparkles } from 'lucide-react';
import client from '../api/client';

export default function Login() {
    const navigate = useNavigate();
    const [isLogin, setIsLogin] = useState(true);
    const [formData, setFormData] = useState({ username: '', password: '', email: '', full_name: '', phone_number: '' });
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [showPassword, setShowPassword] = useState(false);
    const [loading, setLoading] = useState(false);

    const handleChange = e => setFormData({...formData, [e.target.name]: e.target.value});

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setSuccess("");
        setLoading(true);
        try {
             if (isLogin) {
                 const params = new URLSearchParams();
                 params.append('username', formData.username);
                 params.append('password', formData.password);
                 const res = await client.post('/auth/login', params, {
                      headers: {'Content-Type': 'application/x-www-form-urlencoded'}
                 });
                 localStorage.setItem('token', res.data.access_token);
                 navigate('/profile');
             } else {
                  const registeredUsername = formData.username;
                  const registeredPassword = formData.password;
                  await client.post('/auth/register', formData);
                  setSuccess("Tài khoản đã được tạo thành công. Bạn có thể đăng nhập ngay.");
                   setFormData({ username: registeredUsername, password: registeredPassword, email: '', full_name: '', phone_number: '' });
                 setIsLogin(true);
             }
        } catch(err) {
             setError(err.response?.data?.detail || "Có lỗi từ máy chủ");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-[85vh] flex items-center justify-center py-16 px-4 bg-[radial-gradient(circle_at_50%_0%,rgba(154,106,47,0.16),transparent_32rem),linear-gradient(180deg,#F8F1E6,#FFF8ED)]">
            <div className="w-full max-w-md animate-fade-in">
                {/* Logo */}
                <div className="text-center mb-8">
                    <Link to="/" className="inline-flex items-center gap-2.5 group">
                        <div className="w-11 h-11 bg-gradient-to-br from-primary to-primary-dark rounded-xl flex items-center justify-center shadow-glow">
                            <Gavel className="w-6 h-6 text-white" />
                        </div>
                        <span className="font-serif text-2xl font-semibold text-[#2F2418] tracking-[0.12em] uppercase">
                            The <span className="text-[#9A6A2F]">Curator</span>
                        </span>
                    </Link>
                </div>

                {/* Card */}
                <div className="bg-[#FFF8ED]/95 border border-[#2F2418]/10 shadow-[0_30px_90px_rgba(47,36,24,0.12)] p-8 md:p-10">
                    {/* Header */}
                    <div className="text-center mb-8">
                        <h1 className="font-serif text-3xl font-medium text-[#2F2418] mb-2">
                            {isLogin ? "Chào mừng trở lại!" : "Tạo tài khoản mới"}
                        </h1>
                         <p className="text-sm text-[#2F2418]/55">
                            {isLogin ? "Đăng nhập để tiếp tục đấu giá" : "Đăng ký miễn phí để bắt đầu đấu giá"}
                        </p>
                    </div>

                    {/* Tab Switcher */}
                    <div className="flex bg-[#F8F1E6] border border-[#2F2418]/10 p-1 mb-8">
                        <button 
                            onClick={() => setIsLogin(true)} 
                             className={`flex-1 flex items-center justify-center gap-2 py-2.5 text-sm font-semibold transition-all ${isLogin ? 'bg-[#9A6A2F] text-[#F8F1E6] shadow-soft' : 'text-[#2F2418]/50 hover:text-[#2F2418]'}`}
                        >
                            <LogIn className="w-4 h-4" /> Đăng nhập
                        </button>
                        <button 
                            onClick={() => setIsLogin(false)} 
                             className={`flex-1 flex items-center justify-center gap-2 py-2.5 text-sm font-semibold transition-all ${!isLogin ? 'bg-[#9A6A2F] text-[#F8F1E6] shadow-soft' : 'text-[#2F2418]/50 hover:text-[#2F2418]'}`}
                        >
                            <UserPlus className="w-4 h-4" /> Đăng ký
                        </button>
                    </div>

                    {/* Error */}
                    {success && (
                        <div className="relative mb-6 overflow-hidden border border-emerald-500/25 bg-gradient-to-br from-emerald-50 via-[#FFF8ED] to-amber-50 px-4 py-4 text-[#2F2418] shadow-[0_18px_45px_rgba(16,185,129,0.13)] animate-fade-in">
                            <div className="absolute -right-8 -top-8 h-24 w-24 rounded-full bg-emerald-400/15 blur-2xl" />
                            <div className="relative flex items-start gap-3">
                                <div className="mt-0.5 flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-emerald-500 text-white shadow-[0_10px_24px_rgba(16,185,129,0.28)]">
                                    <CheckCircle2 className="h-5 w-5" />
                                </div>
                                <div className="min-w-0">
                                    <div className="flex items-center gap-1.5 text-sm font-bold text-emerald-700">
                                        <Sparkles className="h-4 w-4" />
                                        Đăng ký thành công
                                    </div>
                                    <p className="mt-1 text-sm leading-6 text-[#2F2418]/70">{success}</p>
                                </div>
                            </div>
                        </div>
                    )}

                    {/* Error */}
                    {error && (
                        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-xl mb-6 text-sm font-medium animate-fade-in">
                            {error}
                        </div>
                    )}

                    {/* Form */}
                    <form onSubmit={handleSubmit} className="space-y-5">
                        {/* Username */}
                        <div>
                            <label className="block text-sm font-semibold text-[#2F2418]/72 mb-2">Tên tài khoản</label>
                            <div className="relative">
                                <User className="absolute left-4 top-1/2 -translate-y-1/2 w-4.5 h-4.5 text-[#9A6A2F]/55" />
                                <input 
                                    type="text" 
                                    name="username" 
                                    required 
                                    value={formData.username}
                                    onChange={handleChange} 
                                    placeholder="Nhập tên tài khoản"
                                    className="w-full bg-[#F8F1E6]/70 border border-[#2F2418]/12 px-4 py-3 text-sm text-[#2F2418] placeholder-[#2F2418]/35 focus:outline-none focus:ring-2 focus:ring-[#9A6A2F]/20 focus:border-[#9A6A2F]/60 transition-all pl-11" 
                                />
                            </div>
                        </div>

                        {/* Register-only fields */}
                        {!isLogin && (
                            <div className="space-y-5 animate-fade-in">
                                <div>
                                     <label className="block text-sm font-semibold text-[#2F2418]/72 mb-2">Họ và tên</label>
                                    <div className="relative">
                                         <User className="absolute left-4 top-1/2 -translate-y-1/2 w-4.5 h-4.5 text-[#9A6A2F]/55" />
                                        <input 
                                            type="text" 
                                            name="full_name" 
                                            required 
                                            value={formData.full_name}
                                            onChange={handleChange} 
                                            placeholder="Nhập họ và tên"
                                             className="w-full bg-[#F8F1E6]/70 border border-[#2F2418]/12 px-4 py-3 text-sm text-[#2F2418] placeholder-[#2F2418]/35 focus:outline-none focus:ring-2 focus:ring-[#9A6A2F]/20 focus:border-[#9A6A2F]/60 transition-all pl-11" 
                                        />
                                    </div>
                                </div>
                                <div>
                                     <label className="block text-sm font-semibold text-[#2F2418]/72 mb-2">Email</label>
                                    <div className="relative">
                                         <Mail className="absolute left-4 top-1/2 -translate-y-1/2 w-4.5 h-4.5 text-[#9A6A2F]/55" />
                                        <input 
                                            type="email" 
                                            name="email" 
                                            required 
                                            value={formData.email}
                                            onChange={handleChange} 
                                            placeholder="email@example.com"
                                             className="w-full bg-[#F8F1E6]/70 border border-[#2F2418]/12 px-4 py-3 text-sm text-[#2F2418] placeholder-[#2F2418]/35 focus:outline-none focus:ring-2 focus:ring-[#9A6A2F]/20 focus:border-[#9A6A2F]/60 transition-all pl-11" 
                                        />
                                    </div>
                                </div>
                                <div>
                                     <label className="block text-sm font-semibold text-[#2F2418]/72 mb-2">Số điện thoại</label>
                                    <div className="relative">
                                         <Phone className="absolute left-4 top-1/2 -translate-y-1/2 w-4.5 h-4.5 text-[#9A6A2F]/55" />
                                        <input 
                                            type="text" 
                                            name="phone_number" 
                                            required 
                                            value={formData.phone_number}
                                            onChange={handleChange} 
                                            placeholder="0xxx xxx xxx"
                                             className="w-full bg-[#F8F1E6]/70 border border-[#2F2418]/12 px-4 py-3 text-sm text-[#2F2418] placeholder-[#2F2418]/35 focus:outline-none focus:ring-2 focus:ring-[#9A6A2F]/20 focus:border-[#9A6A2F]/60 transition-all pl-11" 
                                        />
                                    </div>
                                </div>
                            </div>
                        )}

                        {/* Password */}
                        <div>
                            <label className="block text-sm font-semibold text-[#2F2418]/72 mb-2">Mật khẩu</label>
                            <div className="relative">
                                <Lock className="absolute left-4 top-1/2 -translate-y-1/2 w-4.5 h-4.5 text-[#9A6A2F]/55" />
                                <input 
                                    type={showPassword ? 'text' : 'password'} 
                                    name="password" 
                                    required 
                                    value={formData.password}
                                    onChange={handleChange} 
                                    placeholder="••••••••"
                                    className="w-full bg-[#F8F1E6]/70 border border-[#2F2418]/12 px-4 py-3 text-sm text-[#2F2418] placeholder-[#2F2418]/35 focus:outline-none focus:ring-2 focus:ring-[#9A6A2F]/20 focus:border-[#9A6A2F]/60 transition-all pl-11 pr-12" 
                                />
                                <button 
                                    type="button" 
                                    onClick={() => setShowPassword(!showPassword)} 
                                    className="absolute right-4 top-1/2 -translate-y-1/2 text-[#9A6A2F]/55 hover:text-[#9A6A2F] transition-colors"
                                >
                                    {showPassword ? <EyeOff className="w-4.5 h-4.5" /> : <Eye className="w-4.5 h-4.5" />}
                                </button>
                            </div>
                        </div>

                        {/* Submit */}
                        <button 
                            type="submit" 
                            disabled={loading}
                            className="w-full py-3.5 text-base justify-center inline-flex items-center gap-2 bg-[#9A6A2F] text-[#F8F1E6] font-bold hover:bg-[#2F2418] transition-colors disabled:opacity-60 disabled:cursor-not-allowed"
                        >
                            {loading ? (
                                <span className="flex items-center gap-2">
                                    <svg className="animate-spin h-5 w-5" viewBox="0 0 24 24"><circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" /><path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" /></svg>
                                    Đang xử lý...
                                </span>
                            ) : (
                                isLogin ? "Đăng Nhập" : "Tạo Tài Khoản"
                            )}
                        </button>
                    </form>
                </div>

                {/* Footer */}
                <p className="text-center text-sm text-[#2F2418]/55 mt-8">
                    {isLogin ? "Chưa có tài khoản? " : "Đã có tài khoản? "}
                    <button onClick={() => { setIsLogin(!isLogin); setError(''); setSuccess(''); }} className="text-[#9A6A2F] font-semibold hover:underline">
                        {isLogin ? "Đăng ký ngay" : "Đăng nhập"}
                    </button>
                </p>
            </div>
        </div>
    );
}
