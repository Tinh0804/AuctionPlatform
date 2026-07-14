import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Shield, Lock, User, Eye, EyeOff, Terminal } from 'lucide-react';
import { login, getMyInfo } from '@/features/auth/api';
import useAuthStore from '@/store/useAuthStore';

export default function AdminLoginPage() {
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
                 if (account?.role?.name !== 'ADMIN') {
                     throw new Error("Tài khoản không có quyền quản trị viên");
                 }

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
                 
                 navigate('/admin');
             } else {
                 setError("Đăng nhập thất bại");
             }
        } catch(err) {
             setError(err.message || err.response?.data?.message || err.response?.data?.detail || "Tên đăng nhập hoặc mật khẩu không chính xác");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-gray-900 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8 relative overflow-hidden">
            {/* Background elements */}
            <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-gray-800 via-gray-900 to-black"></div>
            <div className="absolute top-0 w-full h-px bg-gradient-to-r from-transparent via-blue-500/50 to-transparent"></div>
            <div className="absolute bottom-0 w-full h-px bg-gradient-to-r from-transparent via-blue-500/10 to-transparent"></div>
            
            <div className="w-full max-w-md relative z-10 animate-fade-in">
                {/* Header */}
                <div className="text-center mb-10">
                    <div className="mx-auto w-16 h-16 bg-gray-800 rounded-2xl flex items-center justify-center mb-6 shadow-[0_0_40px_rgba(59,130,246,0.3)] border border-gray-700/50 relative group">
                        <div className="absolute inset-0 bg-blue-500/20 rounded-2xl blur-xl group-hover:bg-blue-500/30 transition-all"></div>
                        <Shield className="w-8 h-8 text-blue-400 relative z-10" />
                    </div>
                    <h2 className="text-3xl font-bold text-white tracking-tight">Admin Portal</h2>
                    <p className="mt-2 text-sm text-gray-400 flex items-center justify-center gap-2">
                        <Terminal className="w-4 h-4" /> Hệ thống quản trị nội bộ
                    </p>
                </div>

                {/* Card */}
                <div className="bg-gray-800/50 backdrop-blur-xl rounded-3xl border border-gray-700/50 shadow-2xl p-8">
                    {error && (
                        <div className="bg-red-500/10 border border-red-500/20 rounded-xl p-4 mb-6 animate-fade-in">
                            <p className="text-sm font-medium text-red-400 text-center">{error}</p>
                        </div>
                    )}

                    <form onSubmit={handleSubmit} className="space-y-6">
                        <div>
                            <label className="block text-sm font-medium text-gray-300 mb-2">Tên tài khoản</label>
                            <div className="relative group">
                                <User className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-500 group-focus-within:text-blue-400 transition-colors" />
                                <input 
                                    type="text" 
                                    name="username" 
                                    required 
                                    value={formData.username}
                                    onChange={handleChange} 
                                    placeholder="Nhập tên đăng nhập"
                                    className="w-full bg-gray-900/50 border border-gray-700 rounded-xl px-4 py-3.5 pl-12 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500 transition-all" 
                                />
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-300 mb-2">Mật khẩu</label>
                            <div className="relative group">
                                <Lock className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-500 group-focus-within:text-blue-400 transition-colors" />
                                <input 
                                    type={showPassword ? 'text' : 'password'} 
                                    name="password" 
                                    required 
                                    value={formData.password}
                                    onChange={handleChange} 
                                    placeholder="••••••••"
                                    className="w-full bg-gray-900/50 border border-gray-700 rounded-xl px-4 py-3.5 pl-12 pr-12 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500 transition-all" 
                                />
                                <button 
                                    type="button" 
                                    onClick={() => setShowPassword(!showPassword)} 
                                    className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-300 transition-colors"
                                >
                                    {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                                </button>
                            </div>
                        </div>

                        <button 
                            type="submit" 
                            disabled={loading}
                            className="w-full bg-blue-600 hover:bg-blue-500 text-white font-medium py-3.5 rounded-xl transition-all shadow-[0_0_20px_rgba(37,99,235,0.3)] hover:shadow-[0_0_30px_rgba(37,99,235,0.5)] disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
                        >
                            {loading ? (
                                <>
                                    <svg className="animate-spin h-5 w-5 text-white" viewBox="0 0 24 24"><circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" /><path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" /></svg>
                                    Đang xác thực...
                                </>
                            ) : (
                                'Đăng Nhập Quản Trị'
                            )}
                        </button>
                    </form>
                </div>
                
                <div className="mt-8 text-center">
                    <Link to="/" className="text-sm text-gray-500 hover:text-gray-300 transition-colors inline-flex items-center gap-2">
                        &larr; Trở về trang chủ
                    </Link>
                </div>
            </div>
        </div>
    );
}
