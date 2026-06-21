import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Gavel, Eye, EyeOff, User, Lock, LogIn } from 'lucide-react';
import { login, getMyInfo } from '@/features/auth/api';
import useAuthStore from '@/store/useAuthStore';

export default function Login() {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({ username: '', password: '' });
    const [error, setError] = useState("");
    const [showPassword, setShowPassword] = useState(false);
    const [loading, setLoading] = useState(false);

    const handleChange = e => setFormData({...formData, [e.target.name]: e.target.value});

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setLoading(true);
        try {
             const res = await login({
                 userName: formData.username,
                 passWord: formData.password
             });
             
             const token = res.result?.token;
             const refreshToken = res.result?.refreshToken;
             const account = res.result?.account;
             
             if (token) {
                 localStorage.setItem('token', token);
                 if (refreshToken) {
                     localStorage.setItem('refreshToken', refreshToken);
                 }
                 
                 useAuthStore.getState().setToken(token);
                 
                 try {
                     const myInfoRes = await getMyInfo();
                     useAuthStore.getState().setUser(myInfoRes.result || myInfoRes);
                 } catch (err) {
                     useAuthStore.getState().setUser(account);
                 }
                 
                 navigate('/profile');
             } else {
                 setError("Đăng nhập thất bại");
             }
        } catch(err) {
             setError(err.response?.data?.message || err.response?.data?.detail || "Tên đăng nhập hoặc mật khẩu không chính xác");
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
                            Chào mừng trở lại!
                        </h1>
                        <p className="text-sm text-[#2F2418]/55">
                            Đăng nhập để tiếp tục đấu giá
                        </p>
                    </div>

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
                                <span className="flex items-center gap-2">
                                    <LogIn className="w-4 h-4" /> Đăng Nhập
                                </span>
                            )}
                        </button>
                    </form>
                </div>

                {/* Footer */}
                <p className="text-center text-sm text-[#2F2418]/55 mt-8">
                    Chưa có tài khoản?{' '}
                    <Link to="/register" className="text-[#9A6A2F] font-semibold hover:underline">
                        Đăng ký ngay
                    </Link>
                </p>
            </div>
        </div>
    );
}
