import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Gavel, Eye, EyeOff, User, Mail, Phone, Lock, UserPlus, ArrowLeft, AlertCircle, CheckCircle2, Sparkles } from 'lucide-react';
import { authApi } from '@/features/auth/api';
import { registerSchema } from '@/schemas/dto';
import { useToast } from '@/components/Elements/Toast';

export default function Register() {
    const navigate = useNavigate();
    const { toast } = useToast();

    const [formData, setFormData] = useState({
        full_name: '',
        email: '',
        phone: '',
        username: '',
        password: '',
        confirm_password: ''
    });

    const [errors, setErrors] = useState({});
    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);
    const [loading, setLoading] = useState(false);
    const [success, setSuccess] = useState('');

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
        // Xóa lỗi của trường đang sửa
        if (errors[name]) {
            setErrors(prev => ({ ...prev, [name]: '' }));
        }
    };

    const handleValidate = () => {
        const result = registerSchema.safeParse(formData);
        if (!result.success) {
            const fieldErrors = {};
            result.error.issues.forEach((issue) => {
                const path = issue.path[0];
                if (!fieldErrors[path]) {
                    fieldErrors[path] = issue.message;
                }
            });
            setErrors(fieldErrors);
            return false;
        }
        setErrors({});
        return true;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSuccess('');
        
        if (!handleValidate()) {
            return;
        }

        setLoading(true);
        try {
            // Map data từ snake_case của UI form sang camelCase của backend RegisterRequest
            const requestData = {
                userName: formData.username,
                passWord: formData.password,
                fullName: formData.full_name,
                phone: formData.phone,
                email: formData.email
            };

            await authApi.register(requestData);
            
            setSuccess('Đăng ký tài khoản thành công! Bạn sẽ được chuyển hướng sang trang đăng nhập.');
            toast.success('Đăng ký tài khoản thành công! 🎉');
            
            // Redirect sang login sau 1.5s
            setTimeout(() => {
                navigate('/login');
            }, 1800);
        } catch (err) {
            const message = err.response?.data?.message || err.response?.data?.detail || 'Có lỗi xảy ra trong quá trình đăng ký.';
            toast.error(message);
            // Nếu message chứa các lỗi cụ thể, ta có thể hiển thị lỗi
            if (message.includes('Username')) {
                setErrors(prev => ({ ...prev, username: 'Tên đăng nhập đã tồn tại trên hệ thống' }));
            } else if (message.includes('Email')) {
                setErrors(prev => ({ ...prev, email: 'Email đã được đăng ký sử dụng' }));
            } else if (message.includes('Phone')) {
                setErrors(prev => ({ ...prev, phone: 'Số điện thoại đã được đăng ký sử dụng' }));
            } else {
                setErrors({ server: message });
            }
        } finally {
            setLoading(false);
        }
    };

    const inputClass = (field) =>
        `w-full bg-[#F8F1E6]/70 border ${errors[field] ? 'border-red-400' : 'border-[#2F2418]/12'} px-4 py-3 text-sm text-[#2F2418] placeholder-[#2F2418]/35 focus:outline-none focus:ring-2 ${errors[field] ? 'focus:ring-red-300' : 'focus:ring-[#9A6A2F]/20'} focus:border-[#9A6A2F]/60 transition-all pl-11`;

    const labelClass = 'block text-sm font-semibold text-[#2F2418]/72 mb-2';

    return (
        <div className="min-h-[85vh] flex items-center justify-center py-16 px-4 bg-[radial-gradient(circle_at_50%_0%,rgba(154,106,47,0.16),transparent_32rem),linear-gradient(180deg,#F8F1E6,#FFF8ED)]">
            <div className="w-full max-w-md animate-fade-in">
                {/* Logo & Back button */}
                <div className="flex items-center justify-between mb-8">
                    <button onClick={() => navigate(-1)} className="flex items-center gap-2 text-sm text-[#2F2418]/50 hover:text-[#9A6A2F] transition-colors group">
                        <ArrowLeft className="w-4 h-4 group-hover:-translate-x-1 transition-transform" />
                        Quay lại
                    </button>
                    <Link to="/" className="inline-flex items-center gap-2.5 group">
                        <div className="w-9 h-9 border border-[#9A6A2F]/50 bg-[#FFF8ED] flex items-center justify-center">
                            <Gavel className="w-5 h-5 text-[#9A6A2F]" />
                        </div>
                        <span className="font-serif text-lg font-semibold text-[#2F2418] tracking-[0.12em] uppercase">
                            The <span className="text-[#9A6A2F]">Curator</span>
                        </span>
                    </Link>
                </div>

                {/* Card Container */}
                <div className="bg-[#FFF8ED]/95 border border-[#2F2418]/10 shadow-[0_30px_90px_rgba(47,36,24,0.12)] p-8 md:p-10">
                    <div className="text-center mb-8">
                        <h1 className="font-serif text-3xl font-medium text-[#2F2418] mb-2">
                            Đăng Ký Tài Khoản
                        </h1>
                        <p className="text-sm text-[#2F2418]/55">
                            Đăng ký tài khoản miễn phí để tham gia đấu giá những hiện vật giá trị
                        </p>
                    </div>

                    {/* Success state info */}
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
                                        Gửi thành công
                                    </div>
                                    <p className="mt-1 text-sm leading-6 text-[#2F2418]/70">{success}</p>
                                </div>
                            </div>
                        </div>
                    )}

                    {/* General Server Error */}
                    {errors.server && (
                        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-6 text-sm font-medium animate-fade-in flex items-center gap-2">
                            <AlertCircle className="w-4 h-4 shrink-0" />
                            <span>{errors.server}</span>
                        </div>
                    )}

                    {/* Form fields */}
                    <form onSubmit={handleSubmit} className="space-y-5">
                        {/* Họ và tên */}
                        <div>
                            <label className={labelClass}>Họ và tên</label>
                            <div className="relative">
                                <User className="absolute left-4 top-1/2 -translate-y-1/2 w-4.5 h-4.5 text-[#9A6A2F]/55" />
                                <input
                                    type="text"
                                    name="full_name"
                                    value={formData.full_name}
                                    onChange={handleChange}
                                    placeholder="Nhập họ và tên của bạn"
                                    className={inputClass('full_name')}
                                />
                            </div>
                            {errors.full_name && (
                                <p className="text-red-500 text-xs mt-1.5 flex items-center gap-1"><AlertCircle className="w-3.5 h-3.5 shrink-0" />{errors.full_name}</p>
                            )}
                        </div>

                        {/* Email */}
                        <div>
                            <label className={labelClass}>Email</label>
                            <div className="relative">
                                <Mail className="absolute left-4 top-1/2 -translate-y-1/2 w-4.5 h-4.5 text-[#9A6A2F]/55" />
                                <input
                                    type="email"
                                    name="email"
                                    value={formData.email}
                                    onChange={handleChange}
                                    placeholder="email@example.com"
                                    className={inputClass('email')}
                                />
                            </div>
                            {errors.email && (
                                <p className="text-red-500 text-xs mt-1.5 flex items-center gap-1"><AlertCircle className="w-3.5 h-3.5 shrink-0" />{errors.email}</p>
                            )}
                        </div>

                        {/* Số điện thoại */}
                        <div>
                            <label className={labelClass}>Số điện thoại</label>
                            <div className="relative">
                                <Phone className="absolute left-4 top-1/2 -translate-y-1/2 w-4.5 h-4.5 text-[#9A6A2F]/55" />
                                <input
                                    type="text"
                                    name="phone"
                                    value={formData.phone}
                                    onChange={handleChange}
                                    placeholder="Ví dụ: 0987654321"
                                    className={inputClass('phone')}
                                />
                            </div>
                            {errors.phone && (
                                <p className="text-red-500 text-xs mt-1.5 flex items-center gap-1"><AlertCircle className="w-3.5 h-3.5 shrink-0" />{errors.phone}</p>
                            )}
                        </div>

                        {/* Tên đăng nhập */}
                        <div>
                            <label className={labelClass}>Tên đăng nhập</label>
                            <div className="relative">
                                <User className="absolute left-4 top-1/2 -translate-y-1/2 w-4.5 h-4.5 text-[#9A6A2F]/55" />
                                <input
                                    type="text"
                                    name="username"
                                    value={formData.username}
                                    onChange={handleChange}
                                    placeholder="Nhập tên đăng nhập"
                                    className={inputClass('username')}
                                />
                            </div>
                            {errors.username && (
                                <p className="text-red-500 text-xs mt-1.5 flex items-center gap-1"><AlertCircle className="w-3.5 h-3.5 shrink-0" />{errors.username}</p>
                            )}
                        </div>

                        {/* Mật khẩu */}
                        <div>
                            <label className={labelClass}>Mật khẩu</label>
                            <div className="relative">
                                <Lock className="absolute left-4 top-1/2 -translate-y-1/2 w-4.5 h-4.5 text-[#9A6A2F]/55" />
                                <input
                                    type={showPassword ? 'text' : 'password'}
                                    name="password"
                                    value={formData.password}
                                    onChange={handleChange}
                                    placeholder="••••••••"
                                    className={`${inputClass('password')} pr-12`}
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowPassword(!showPassword)}
                                    className="absolute right-4 top-1/2 -translate-y-1/2 text-[#9A6A2F]/55 hover:text-[#9A6A2F] transition-colors"
                                >
                                    {showPassword ? <EyeOff className="w-4.5 h-4.5" /> : <Eye className="w-4.5 h-4.5" />}
                                </button>
                            </div>
                            {errors.password && (
                                <p className="text-red-500 text-xs mt-1.5 flex items-center gap-1"><AlertCircle className="w-3.5 h-3.5 shrink-0" />{errors.password}</p>
                            )}
                        </div>

                        {/* Xác nhận mật khẩu */}
                        <div>
                            <label className={labelClass}>Xác nhận mật khẩu</label>
                            <div className="relative">
                                <Lock className="absolute left-4 top-1/2 -translate-y-1/2 w-4.5 h-4.5 text-[#9A6A2F]/55" />
                                <input
                                    type={showConfirmPassword ? 'text' : 'password'}
                                    name="confirm_password"
                                    value={formData.confirm_password}
                                    onChange={handleChange}
                                    placeholder="••••••••"
                                    className={`${inputClass('confirm_password')} pr-12`}
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                                    className="absolute right-4 top-1/2 -translate-y-1/2 text-[#9A6A2F]/55 hover:text-[#9A6A2F] transition-colors"
                                >
                                    {showConfirmPassword ? <EyeOff className="w-4.5 h-4.5" /> : <Eye className="w-4.5 h-4.5" />}
                                </button>
                            </div>
                            {errors.confirm_password && (
                                <p className="text-red-500 text-xs mt-1.5 flex items-center gap-1"><AlertCircle className="w-3.5 h-3.5 shrink-0" />{errors.confirm_password}</p>
                            )}
                        </div>

                        {/* Submit Button */}
                        <button
                            type="submit"
                            disabled={loading || success !== ''}
                            className="w-full py-3.5 text-base justify-center inline-flex items-center gap-2 bg-[#9A6A2F] text-[#F8F1E6] font-bold hover:bg-[#2F2418] transition-colors disabled:opacity-60 disabled:cursor-not-allowed"
                        >
                            {loading ? (
                                <span className="flex items-center gap-2">
                                    <svg className="animate-spin h-5 w-5" viewBox="0 0 24 24"><circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" /><path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" /></svg>
                                    Đang xử lý...
                                </span>
                            ) : (
                                <>
                                    <UserPlus className="w-4.5 h-4.5" />
                                    Tạo Tài Khoản
                                </>
                            )}
                        </button>
                    </form>
                </div>

                {/* Footer link to Login */}
                <p className="text-center text-sm text-[#2F2418]/55 mt-8">
                    Đã có tài khoản?{' '}
                    <Link to="/login" className="text-[#9A6A2F] font-semibold hover:underline">
                        Đăng nhập
                    </Link>
                </p>
            </div>
        </div>
    );
}
