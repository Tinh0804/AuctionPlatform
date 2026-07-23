import { TOKEN_KEY, REFRESH_TOKEN_KEY } from '@/config/constants';
import { useState, useEffect, useRef } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { ArrowLeft, Eye, EyeOff, LockKeyhole, User, ArrowRight } from 'lucide-react';
import gsap from 'gsap';
import { authApi } from '@/features/auth/api';
import useAuthStore from '@/store/useAuthStore';

export default function Login() {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({ username: '', password: '' });
    const [error, setError] = useState("");
    const [showPassword, setShowPassword] = useState(false);
    const [loading, setLoading] = useState(false);

    const containerRef = useRef(null);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
        if (error) setError('');
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setLoading(true);

        try {
            const res = await authApi.login({
                userName: formData.username,
                passWord: formData.password
            });

            const token = res.result?.token;
            const refreshToken = res.result?.refreshToken;
            const account = res.result?.account;

            if (token) {
                localStorage.setItem(TOKEN_KEY, token);
                if (refreshToken) {
                    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
                }

                useAuthStore.getState().setToken(token);

                try {
                    const myInfoRes = await authApi.getMyInfo();
                    useAuthStore.getState().setUser(myInfoRes.result || myInfoRes);
                } catch (err) {
                    useAuthStore.getState().setUser(account);
                }

                navigate('/profile');
            } else {
                setError("Đăng nhập thất bại. Vui lòng kiểm tra lại tài khoản.");
            }
        } catch (err) {
            setError(
                err.response?.data?.message ||
                err.response?.data?.detail ||
                "Tên đăng nhập hoặc mật khẩu không chính xác"
            );
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        const ctx = gsap.context(() => {
            // Stagger for Bento Gallery
            gsap.fromTo('.gallery-item',
                { opacity: 0, y: 50, scale: 0.98 },
                { opacity: 1, y: 0, scale: 1, duration: 1.4, ease: "power4.out", stagger: 0.15 }
            );

            // Stagger for Form Elements
            gsap.fromTo('.form-element',
                { opacity: 0, x: 20 },
                { opacity: 1, x: 0, duration: 1.2, ease: "power3.out", stagger: 0.1, delay: 0.3 }
            );

            // Subtle parallax pan for images
            gsap.to('.gallery-image-pan', {
                y: "-8%",
                duration: 15,
                ease: "sine.inOut",
                yoyo: true,
                repeat: -1
            });

            // Floating badge animation
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
        <main ref={containerRef} className="auth-login-page flex min-h-[100dvh] w-full bg-[#faf7f1] text-[#1c1815] selection:bg-[#1c1815] selection:text-[#faf7f1] overflow-hidden">

            {/* ── LEFT COLUMN: CREATIVE BENTO GALLERY ── */}
            <section className="relative hidden w-[55%] p-10 lg:flex flex-col justify-center items-center xl:p-14">
                {/* Brand Header Absolute */}
                <div className="absolute top-10 left-10 z-20 flex items-center gap-4">
                    <img src="/brand/curator-mark.svg" alt="" className="h-12 w-12 shadow-xl" />
                    <div className="flex flex-col">
                        <span className="auth-serif text-xl font-bold leading-none tracking-tight text-[#1c1815]">The Curator</span>
                        <span className="mt-1 text-[10px] font-semibold uppercase tracking-[0.2em] text-[#746b62]">Antique Auction House</span>
                    </div>
                </div>

                {/* Bento Grid */}
                <div className="grid grid-cols-12 grid-rows-6 gap-4 w-full h-[80%] max-h-[800px] mt-12">

                    {/* Main Large Image */}
                    <div className="gallery-item col-span-8 row-span-4 rounded-3xl overflow-hidden relative shadow-sm border border-[#1c1815]/5">
                        <img
                            src="/images/auth/login-gallery.jpg"
                            alt="The Curator Gallery Collection"
                            className="gallery-image-pan w-full h-[120%] object-cover object-[50%_40%] contrast-[1.05]"
                        />
                        <div className="absolute inset-0 shadow-[inset_0_0_60px_rgba(0,0,0,0.1)] pointer-events-none" />

                        <div className="floating-badge absolute bottom-6 left-6 bg-white/80 backdrop-blur-md border border-white/50 px-5 py-3 shadow-xl rounded-2xl">
                            <span className="block font-bold text-[#1c1815] uppercase tracking-widest text-[9px] mb-1">Triển lãm nổi bật</span>
                            <span className="auth-serif block text-xl text-[#1c1815] leading-none">Nghệ Thuật Đương Đại</span>
                        </div>
                    </div>

                    {/* Top Right Dark Block */}
                    <div className="gallery-item col-span-4 row-span-2 rounded-3xl bg-[#1c1815] flex flex-col items-center justify-center p-6 text-[#faf7f1] shadow-xl relative overflow-hidden">
                        <div className="absolute inset-0 opacity-10 bg-[radial-gradient(circle_at_center,white_0%,transparent_100%)]"></div>
                        <h3 className="auth-serif text-5xl mb-1 tracking-tighter">98</h3>
                        <p className="text-[9px] tracking-[0.2em] uppercase text-[#faf7f1]/60 text-center">Tác phẩm độc bản<br/>đang đấu giá</p>
                    </div>

                    {/* Mid Right Image */}
                    <div className="gallery-item col-span-4 row-span-2 rounded-3xl overflow-hidden shadow-sm border border-[#1c1815]/5">
                        <img
                            src="https://images.unsplash.com/photo-1600573472550-8090b5e0745e?auto=format&fit=crop&q=80"
                            className="gallery-image-pan w-full h-[120%] object-cover"
                            alt="Sculpture"
                        />
                    </div>

                    {/* Bottom Wide Block */}
                    <div className="gallery-item col-span-12 row-span-2 rounded-3xl bg-[#eae5db] p-8 flex items-center justify-between border border-[#1c1815]/5 shadow-inner">
                        <div className="max-w-[60%]">
                            <h4 className="auth-serif text-2xl text-[#1c1815] mb-2">Đặc quyền Sưu tầm</h4>
                            <p className="text-[13px] text-[#746b62] leading-relaxed">
                                Trải nghiệm không gian đấu giá chuyên nghiệp, minh bạch và an toàn dành riêng cho giới mộ điệu.
                            </p>
                        </div>
                        <div className="h-16 w-16 rounded-full border border-[#1c1815]/20 flex items-center justify-center">
                            <ArrowRight className="text-[#1c1815] h-6 w-6" />
                        </div>
                    </div>
                </div>
            </section>

            {/* ── RIGHT COLUMN: EDITORIAL FORM ── */}
            <section className="relative flex w-full flex-col p-6 sm:p-10 lg:w-[45%] lg:p-14 xl:px-[8%] xl:py-16 justify-center bg-white shadow-[-20px_0_40px_rgba(0,0,0,0.02)] z-10">

                {/* Mobile Header (Hidden on Desktop) */}
                <div className="flex items-center justify-between lg:hidden mb-12">
                    <div className="form-element flex items-center gap-3">
                        <img src="/brand/curator-mark.svg" alt="" className="h-10 w-10" />
                        <span className="flex flex-col">
                            <span className="auth-serif text-xl font-bold leading-none">The Curator</span>
                            <span className="mt-1 text-[8px] font-semibold uppercase tracking-[0.18em] text-[#746b62]">Antique Auction House</span>
                        </span>
                    </div>
                </div>

                <div className="absolute top-10 right-10 hidden lg:flex form-element">
                    <Link
                        to="/"
                        className="group flex items-center gap-2 text-xs font-bold uppercase tracking-[0.2em] text-[#746b62] transition-colors hover:text-[#1c1815]"
                    >
                        <ArrowLeft className="h-3 w-3 transition-transform duration-300 group-hover:-translate-x-1" />
                        Trở về trang chủ
                    </Link>
                </div>

                <div className="mx-auto w-full max-w-[420px] flex-col">
                    <div className="form-element">
                        {/* Sans-serif high contrast title */}
                        <h1 className="auth-serif text-[4.5rem] md:text-[5.5rem] font-bold tracking-tighter text-[#1c1815] leading-[0.9]">
                            Đăng nhập
                        </h1>
                        <p className="mt-6 text-[17px] leading-relaxed text-[#746b62]">
                            Chào mừng trở lại. Nhập thông tin tài khoản để truy cập vào bộ sưu tập và các phiên đấu giá.
                        </p>
                    </div>

                    {error && (
                        <div className="form-element mt-6 border-l-4 border-[#1c1815] bg-[#faf7f1] p-4 text-sm text-[#1c1815] shadow-sm">
                            <div className="font-semibold">{error}</div>
                        </div>
                    )}

                    <form onSubmit={handleSubmit} className="mt-10 space-y-8">
                        {/* Input Field */}
                        <div className="group relative form-element">
                            <label htmlFor="username" className="mb-2 block text-xs font-bold uppercase tracking-[0.15em] text-[#1c1815]">
                                Tên tài khoản
                            </label>
                            <div className="flex items-center border-b-2 border-[#d8d1c9] pb-3 transition-colors group-focus-within:border-[#1c1815]">
                                <User className="mr-3 h-5 w-5 text-[#9b938b] group-focus-within:text-[#1c1815]" />
                                <input
                                    id="username"
                                    type="text"
                                    name="username"
                                    required
                                    value={formData.username}
                                    onChange={handleChange}
                                    placeholder="Nhập tên tài khoản"
                                    className="w-full bg-transparent text-base font-semibold text-[#1c1815] placeholder:text-[#9b938b] placeholder:font-normal focus:outline-none"
                                />
                            </div>
                        </div>

                        {/* Password Field */}
                        <div className="group relative form-element">
                            <label htmlFor="password" className="mb-2 block text-xs font-bold uppercase tracking-[0.15em] text-[#1c1815]">
                                Mật khẩu
                            </label>
                            <div className="flex items-center border-b-2 border-[#d8d1c9] pb-3 transition-colors group-focus-within:border-[#1c1815]">
                                <LockKeyhole className="mr-3 h-5 w-5 text-[#9b938b] group-focus-within:text-[#1c1815]" />
                                <input
                                    id="password"
                                    type={showPassword ? 'text' : 'password'}
                                    name="password"
                                    required
                                    value={formData.password}
                                    onChange={handleChange}
                                    placeholder="Nhập mật khẩu"
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
                        </div>

                        {/* Submit Button */}
                        <div className="pt-6 form-element">
                            <button
                                type="submit"
                                disabled={loading}
                                className="group relative flex w-full items-center justify-center gap-4 bg-[#1c1815] px-8 py-5 text-sm font-bold uppercase tracking-[0.2em] text-[#faf7f1] transition-all duration-300 hover:bg-[#2a241f] hover:shadow-[0_20px_40px_rgba(28,24,21,0.2)] active:scale-[0.98] disabled:cursor-wait disabled:opacity-70 rounded-xl"
                            >
                                <span>{loading ? 'Đang xác thực...' : 'Truy cập Phiên Đấu Giá'}</span>
                                {!loading && <ArrowRight className="h-5 w-5 transition-transform duration-300 group-hover:translate-x-1" />}
                            </button>
                        </div>
                    </form>

                    <div className="form-element mt-12 flex items-center justify-between border-t border-[#e8e3dc] pt-8 text-sm font-medium text-[#746b62]">
                        <Link to="/register" className="transition-colors hover:text-[#1c1815] hover:underline underline-offset-4">
                            Tạo tài khoản mới
                        </Link>
                        <Link to="/admin/login" className="transition-colors hover:text-[#1c1815]">
                            Dành cho Quản trị viên
                        </Link>
                    </div>
                </div>
            </section>
        </main>
    );
}
