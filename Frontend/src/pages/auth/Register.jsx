import { useState, useEffect, useRef } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { ArrowLeft, Eye, EyeOff, LockKeyhole, User, Mail, Phone, ArrowRight, AlertCircle, CheckCircle2, Sparkles } from 'lucide-react';
import gsap from 'gsap';
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

    const containerRef = useRef(null);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
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
            const requestData = {
                userName: formData.username,
                passWord: formData.password,
                fullName: formData.full_name,
                phone: formData.phone,
                email: formData.email
            };

            await authApi.register(requestData);

            setSuccess('Đăng ký tài khoản thành công!');
            toast.success('Đăng ký tài khoản thành công! 🎉');

            setTimeout(() => {
                navigate('/login');
            }, 1800);
        } catch (err) {
            const message = err.response?.data?.message || err.response?.data?.detail || 'Có lỗi xảy ra trong quá trình đăng ký.';
            toast.error(message);
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

    useEffect(() => {
        const ctx = gsap.context(() => {
            gsap.fromTo('.gallery-item',
                { opacity: 0, y: 50, scale: 0.98 },
                { opacity: 1, y: 0, scale: 1, duration: 1.4, ease: "power4.out", stagger: 0.15 }
            );

            gsap.fromTo('.form-element',
                { opacity: 0, x: 20 },
                { opacity: 1, x: 0, duration: 1.2, ease: "power3.out", stagger: 0.08, delay: 0.3 }
            );

            gsap.to('.gallery-image-pan', {
                y: "-8%",
                duration: 15,
                ease: "sine.inOut",
                yoyo: true,
                repeat: -1
            });

            gsap.to('.floating-badge', {
                y: -10,
                duration: 4,
                ease: "sine.inOut",
                yoyo: true,
                repeat: -1
            });
        }, containerRef);
        return () => ctx.revert();
    }, []);

    return (
        <main ref={containerRef} className="auth-login-page flex lg:flex-row-reverse min-h-[100dvh] w-full bg-[#faf7f1] text-[#1c1815] selection:bg-[#1c1815] selection:text-[#faf7f1] overflow-hidden">

            {/* ── RIGHT COLUMN: CREATIVE BENTO GALLERY ── */}
            <section className="relative hidden w-[55%] p-10 lg:flex flex-col justify-center items-center xl:p-14">
                <div className="absolute top-10 right-10 z-20 flex items-center gap-4">
                    <img src="/brand/curator-mark.svg" alt="" className="h-12 w-12 shadow-xl" />
                    <div className="flex flex-col">
                        <span className="auth-serif text-xl font-bold leading-none tracking-tight text-[#1c1815]">The Curator</span>
                        <span className="mt-1 text-[10px] font-semibold uppercase tracking-[0.2em] text-[#746b62]">Antique Auction House</span>
                    </div>
                </div>

                <div className="grid grid-cols-12 grid-rows-6 gap-4 w-full h-[80%] max-h-[800px] mt-12">

                    {/* Top Right Dark Block */}
                    <div className="gallery-item col-span-4 row-span-2 rounded-3xl bg-[#1c1815] flex flex-col items-center justify-center p-6 text-[#faf7f1] shadow-xl relative overflow-hidden order-2">
                        <div className="absolute inset-0 opacity-10 bg-[radial-gradient(circle_at_center,white_0%,transparent_100%)]"></div>
                        <h3 className="auth-serif text-5xl mb-1 tracking-tighter">New</h3>
                        <p className="text-[9px] tracking-[0.2em] uppercase text-[#faf7f1]/60 text-center">Bắt đầu hành trình<br/>sưu tầm</p>
                    </div>

                    {/* Main Large Image */}
                    <div className="gallery-item col-span-8 row-span-4 rounded-3xl overflow-hidden relative shadow-sm border border-[#1c1815]/5 order-1">
                        <img
                            src="https://images.unsplash.com/photo-1578301978018-3005759f48f7?auto=format&fit=crop&q=80"
                            alt="The Curator Gallery Collection"
                            className="gallery-image-pan w-full h-[120%] object-cover object-[50%_40%] contrast-[1.05]"
                        />
                        <div className="absolute inset-0 shadow-[inset_0_0_60px_rgba(0,0,0,0.1)] pointer-events-none" />

                        <div className="floating-badge absolute bottom-6 left-6 bg-white/80 backdrop-blur-md border border-white/50 px-5 py-3 shadow-xl rounded-2xl text-left">
                            <span className="block font-bold text-[#1c1815] uppercase tracking-widest text-[9px] mb-1">Thành viên mới</span>
                            <span className="auth-serif block text-xl text-[#1c1815] leading-none">Đặc quyền riêng</span>
                        </div>
                    </div>

                    {/* Mid Right Image */}
                    <div className="gallery-item col-span-4 row-span-2 rounded-3xl overflow-hidden shadow-sm border border-[#1c1815]/5 order-3">
                        <img
                            src="https://images.unsplash.com/photo-1544967082-d9d25d867d66?auto=format&fit=crop&q=80"
                            className="gallery-image-pan w-full h-[120%] object-cover grayscale-[10%]"
                            alt="Sculpture"
                        />
                    </div>

                    {/* Bottom Wide Block */}
                    <div className="gallery-item col-span-12 row-span-2 rounded-3xl bg-[#eae5db] p-8 flex items-center justify-between border border-[#1c1815]/5 shadow-inner order-4">
                        <div className="max-w-[60%]">
                            <h4 className="auth-serif text-2xl text-[#1c1815] mb-2">Đăng ký tham gia</h4>
                            <p className="text-[13px] text-[#746b62] leading-relaxed">
                                Tạo tài khoản để nhận thông báo về các phiên đấu giá kín và danh mục tác phẩm mới nhất.
                            </p>
                        </div>
                        <div className="h-16 w-16 rounded-full border border-[#1c1815]/20 flex items-center justify-center">
                            <ArrowRight className="text-[#1c1815] h-6 w-6" />
                        </div>
                    </div>
                </div>
            </section>

            {/* ── LEFT COLUMN: EDITORIAL FORM ── */}
            <section className="relative flex w-full flex-col h-[100dvh] overflow-y-auto overflow-x-hidden p-6 sm:p-10 lg:w-[45%] lg:p-14 xl:px-[8%] bg-white shadow-[20px_0_40px_rgba(0,0,0,0.02)] z-10 custom-scrollbar">

                <div className="absolute top-10 left-10 hidden lg:flex form-element z-20">
                    <Link
                        to="/"
                        className="group flex items-center gap-2 text-xs font-bold uppercase tracking-[0.2em] text-[#746b62] transition-colors hover:text-[#1c1815]"
                    >
                        <ArrowLeft className="h-3 w-3 transition-transform duration-300 group-hover:-translate-x-1" />
                        Trở về trang chủ
                    </Link>
                </div>

                <div className="mx-auto w-full max-w-[420px] flex-col py-16">
                    <div className="form-element">
                        <h1 className="auth-serif text-[4rem] md:text-[5rem] font-bold tracking-tighter text-[#1c1815] leading-[0.9]">
                            Đăng ký
                        </h1>
                        <p className="mt-4 text-[17px] leading-relaxed text-[#746b62]">
                            Tạo tài khoản để tham gia đấu giá những hiện vật giá trị và quản lý bộ sưu tập.
                        </p>
                    </div>

                    {success && (
                        <div className="form-element mt-6 border-l-4 border-emerald-500 bg-emerald-50 p-4 text-sm text-emerald-800 shadow-sm flex items-start gap-3">
                            <CheckCircle2 className="h-5 w-5 shrink-0 mt-0.5" />
                            <div className="font-semibold">{success}</div>
                        </div>
                    )}

                    {errors.server && (
                        <div className="form-element mt-6 border-l-4 border-red-500 bg-red-50 p-4 text-sm text-red-800 shadow-sm flex items-start gap-3">
                            <AlertCircle className="h-5 w-5 shrink-0 mt-0.5" />
                            <div className="font-semibold">{errors.server}</div>
                        </div>
                    )}

                    <form onSubmit={handleSubmit} className="mt-10 space-y-6">

                        {/* Họ và tên */}
                        <div className="group relative form-element">
                            <label className="mb-2 block text-xs font-bold uppercase tracking-[0.15em] text-[#1c1815]">
                                Họ và tên
                            </label>
                            <div className={`flex items-center border-b-2 ${errors.full_name ? 'border-red-400' : 'border-[#d8d1c9] group-focus-within:border-[#1c1815]'} pb-3 transition-colors`}>
                                <User className={`mr-3 h-5 w-5 ${errors.full_name ? 'text-red-400' : 'text-[#9b938b] group-focus-within:text-[#1c1815]'}`} />
                                <input
                                    type="text"
                                    name="full_name"
                                    value={formData.full_name}
                                    onChange={handleChange}
                                    placeholder="Ví dụ: Nguyễn Văn A"
                                    className="w-full bg-transparent text-base font-semibold text-[#1c1815] placeholder:text-[#9b938b] placeholder:font-normal focus:outline-none"
                                />
                            </div>
                            {errors.full_name && <p className="text-red-500 text-xs mt-1.5 flex items-center gap-1"><AlertCircle className="w-3.5 h-3.5" />{errors.full_name}</p>}
                        </div>

                        {/* Email */}
                        <div className="group relative form-element">
                            <label className="mb-2 block text-xs font-bold uppercase tracking-[0.15em] text-[#1c1815]">
                                Email
                            </label>
                            <div className={`flex items-center border-b-2 ${errors.email ? 'border-red-400' : 'border-[#d8d1c9] group-focus-within:border-[#1c1815]'} pb-3 transition-colors`}>
                                <Mail className={`mr-3 h-5 w-5 ${errors.email ? 'text-red-400' : 'text-[#9b938b] group-focus-within:text-[#1c1815]'}`} />
                                <input
                                    type="email"
                                    name="email"
                                    value={formData.email}
                                    onChange={handleChange}
                                    placeholder="email@example.com"
                                    className="w-full bg-transparent text-base font-semibold text-[#1c1815] placeholder:text-[#9b938b] placeholder:font-normal focus:outline-none"
                                />
                            </div>
                            {errors.email && <p className="text-red-500 text-xs mt-1.5 flex items-center gap-1"><AlertCircle className="w-3.5 h-3.5" />{errors.email}</p>}
                        </div>

                        {/* Phone */}
                        <div className="group relative form-element">
                            <label className="mb-2 block text-xs font-bold uppercase tracking-[0.15em] text-[#1c1815]">
                                Số điện thoại
                            </label>
                            <div className={`flex items-center border-b-2 ${errors.phone ? 'border-red-400' : 'border-[#d8d1c9] group-focus-within:border-[#1c1815]'} pb-3 transition-colors`}>
                                <Phone className={`mr-3 h-5 w-5 ${errors.phone ? 'text-red-400' : 'text-[#9b938b] group-focus-within:text-[#1c1815]'}`} />
                                <input
                                    type="text"
                                    name="phone"
                                    value={formData.phone}
                                    onChange={handleChange}
                                    placeholder="Ví dụ: 0987654321"
                                    className="w-full bg-transparent text-base font-semibold text-[#1c1815] placeholder:text-[#9b938b] placeholder:font-normal focus:outline-none"
                                />
                            </div>
                            {errors.phone && <p className="text-red-500 text-xs mt-1.5 flex items-center gap-1"><AlertCircle className="w-3.5 h-3.5" />{errors.phone}</p>}
                        </div>

                        {/* Username */}
                        <div className="group relative form-element">
                            <label className="mb-2 block text-xs font-bold uppercase tracking-[0.15em] text-[#1c1815]">
                                Tên tài khoản
                            </label>
                            <div className={`flex items-center border-b-2 ${errors.username ? 'border-red-400' : 'border-[#d8d1c9] group-focus-within:border-[#1c1815]'} pb-3 transition-colors`}>
                                <User className={`mr-3 h-5 w-5 ${errors.username ? 'text-red-400' : 'text-[#9b938b] group-focus-within:text-[#1c1815]'}`} />
                                <input
                                    type="text"
                                    name="username"
                                    value={formData.username}
                                    onChange={handleChange}
                                    placeholder="Tên đăng nhập viết liền không dấu"
                                    className="w-full bg-transparent text-base font-semibold text-[#1c1815] placeholder:text-[#9b938b] placeholder:font-normal focus:outline-none"
                                />
                            </div>
                            {errors.username && <p className="text-red-500 text-xs mt-1.5 flex items-center gap-1"><AlertCircle className="w-3.5 h-3.5" />{errors.username}</p>}
                        </div>

                        {/* Password */}
                        <div className="group relative form-element">
                            <label className="mb-2 block text-xs font-bold uppercase tracking-[0.15em] text-[#1c1815]">
                                Mật khẩu
                            </label>
                            <div className={`flex items-center border-b-2 ${errors.password ? 'border-red-400' : 'border-[#d8d1c9] group-focus-within:border-[#1c1815]'} pb-3 transition-colors`}>
                                <LockKeyhole className={`mr-3 h-5 w-5 ${errors.password ? 'text-red-400' : 'text-[#9b938b] group-focus-within:text-[#1c1815]'}`} />
                                <input
                                    type={showPassword ? 'text' : 'password'}
                                    name="password"
                                    value={formData.password}
                                    onChange={handleChange}
                                    placeholder="Tối thiểu 6 ký tự"
                                    className="w-full bg-transparent text-base font-semibold text-[#1c1815] placeholder:text-[#9b938b] placeholder:font-normal focus:outline-none"
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowPassword(!showPassword)}
                                    className="ml-2 text-[#9b938b] transition-colors hover:text-[#1c1815]"
                                >
                                    {showPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
                                </button>
                            </div>
                            {errors.password && <p className="text-red-500 text-xs mt-1.5 flex items-center gap-1"><AlertCircle className="w-3.5 h-3.5" />{errors.password}</p>}
                        </div>

                        {/* Confirm Password */}
                        <div className="group relative form-element">
                            <label className="mb-2 block text-xs font-bold uppercase tracking-[0.15em] text-[#1c1815]">
                                Xác nhận mật khẩu
                            </label>
                            <div className={`flex items-center border-b-2 ${errors.confirm_password ? 'border-red-400' : 'border-[#d8d1c9] group-focus-within:border-[#1c1815]'} pb-3 transition-colors`}>
                                <LockKeyhole className={`mr-3 h-5 w-5 ${errors.confirm_password ? 'text-red-400' : 'text-[#9b938b] group-focus-within:text-[#1c1815]'}`} />
                                <input
                                    type={showConfirmPassword ? 'text' : 'password'}
                                    name="confirm_password"
                                    value={formData.confirm_password}
                                    onChange={handleChange}
                                    placeholder="Nhập lại mật khẩu"
                                    className="w-full bg-transparent text-base font-semibold text-[#1c1815] placeholder:text-[#9b938b] placeholder:font-normal focus:outline-none"
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                                    className="ml-2 text-[#9b938b] transition-colors hover:text-[#1c1815]"
                                >
                                    {showConfirmPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
                                </button>
                            </div>
                            {errors.confirm_password && <p className="text-red-500 text-xs mt-1.5 flex items-center gap-1"><AlertCircle className="w-3.5 h-3.5" />{errors.confirm_password}</p>}
                        </div>

                        {/* Submit Button */}
                        <div className="pt-8 form-element">
                            <button
                                type="submit"
                                disabled={loading || success !== ''}
                                className="group relative flex w-full items-center justify-center gap-4 bg-[#1c1815] px-8 py-5 text-sm font-bold uppercase tracking-[0.2em] text-[#faf7f1] transition-all duration-300 hover:bg-[#2a241f] hover:shadow-[0_20px_40px_rgba(28,24,21,0.2)] active:scale-[0.98] disabled:cursor-wait disabled:opacity-70 rounded-xl"
                            >
                                <span>{loading ? 'Đang tạo tài khoản...' : 'Tạo Tài Khoản'}</span>
                                {!loading && <ArrowRight className="h-5 w-5 transition-transform duration-300 group-hover:translate-x-1" />}
                            </button>
                        </div>
                    </form>

                    <div className="form-element mt-10 mb-10 flex items-center justify-center border-t border-[#e8e3dc] pt-8 text-sm font-medium text-[#746b62]">
                        <span className="mr-2">Đã có tài khoản?</span>
                        <Link to="/login" className="transition-colors text-[#1c1815] hover:underline underline-offset-4 font-bold">
                            Đăng nhập ngay
                        </Link>
                    </div>
                </div>
            </section>
        </main>
    );
}
