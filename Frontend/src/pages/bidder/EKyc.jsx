import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import apiClient from '@/services/apiClient';
import { ShieldCheck, Upload, AlertCircle, CheckCircle2, CreditCard, Lock, Eye, ArrowLeft, ImagePlus } from 'lucide-react';

export default function EKyc() {
    const navigate = useNavigate();
    const [frontImage, setFrontImage] = useState(null);
    const [backImage, setBackImage] = useState(null);
    const [previews, setPreviews] = useState({ front: null, back: null });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(false);

    const handleFileChange = (e, side) => {
        const file = e.target.files[0];
        if (file) {
            if (side === 'front') {
                setFrontImage(file);
                setPreviews(prev => ({ ...prev, front: URL.createObjectURL(file) }));
            } else {
                setBackImage(file);
                setPreviews(prev => ({ ...prev, back: URL.createObjectURL(file) }));
            }
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!frontImage || !backImage) {
            setError("Vui lòng tải lên cả mặt trước và mặt sau của CCCD");
            return;
        }

        setLoading(true);
        setError(null);

        const formData = new FormData();
        formData.append('front_image', frontImage);
        formData.append('back_image', backImage);

        try {
            await apiClient.post('/ekyc/verify', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            });
            setSuccess(true);
            setTimeout(() => navigate('/profile'), 3000);
        } catch (err) {
            setError(err.response?.data?.detail || "Có lỗi xảy ra trong quá trình xác thực");
        } finally {
            setLoading(false);
        }
    };

    if (success) {
        return (
            <div className="ekyc-light-page min-h-screen px-4 py-24 text-center animate-fade-in">
                <div className="mx-auto max-w-lg rounded-[2rem] border border-[#9A6A2F]/15 bg-white/82 p-10 shadow-[0_28px_90px_rgba(47,36,24,0.12)] backdrop-blur-xl">
                    <div className="w-20 h-20 bg-emerald-50 border border-emerald-200 rounded-full flex items-center justify-center mx-auto mb-6">
                        <CheckCircle2 className="w-10 h-10 text-emerald-600" />
                    </div>
                    <h2 className="font-serif text-4xl font-normal text-[#2F2418] mb-3">Xác Thực Thành Công</h2>
                    <p className="text-[#2F2418]/60 mb-8">Thông tin của bạn đã được hệ thống ghi nhận. Bạn sẽ được chuyển về trang hồ sơ trong giây lát.</p>
                    <button onClick={() => navigate('/profile')} className="auction-hero-cta justify-center">
                        Về trang hồ sơ
                    </button>
                </div>
            </div>
        );
    }

    const UploadBox = ({ side, label, preview }) => (
        <div className="space-y-3">
            <label className="text-xs uppercase tracking-[0.28em] text-[#9A6A2F] flex items-center gap-2">
                <CreditCard className="w-4 h-4" /> {label}
            </label>
            <div className={`relative group rounded-[1.4rem] border border-dashed overflow-hidden aspect-[1.6/1] flex flex-col items-center justify-center transition-all duration-500 cursor-pointer ${preview ? 'border-[#9A6A2F]/55 bg-white/80' : 'border-[#9A6A2F]/22 bg-white/60 hover:border-[#9A6A2F]/55 hover:bg-white/90'}`}>
                {preview ? (
                    <>
                        <img src={preview} alt={label} className="w-full h-full object-cover" />
                        <div className="absolute inset-0 bg-[#2F2418]/28 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center backdrop-blur-sm">
                            <span className="rounded-full bg-[#FFF8ED] text-[#2F2418] px-4 py-2 text-xs uppercase tracking-[0.18em] font-semibold flex items-center gap-2 shadow-lg">
                                <ImagePlus className="w-4 h-4" /> Thay đổi ảnh
                            </span>
                        </div>
                    </>
                ) : (
                    <div className="text-center p-6">
                        <div className="w-14 h-14 bg-[#F8F1E6] border border-[#9A6A2F]/18 shadow-[0_18px_45px_rgba(47,36,24,0.10)] flex items-center justify-center mx-auto mb-3 rounded-full">
                            <Upload className="w-6 h-6 text-[#9A6A2F]" />
                        </div>
                        <p className="text-sm font-medium text-[#2F2418] mb-1">Kéo thả hoặc nhấn để chọn</p>
                        <p className="text-xs text-[#2F2418]/45">JPG, PNG tối đa 10MB</p>
                    </div>
                )}
                <input 
                    type="file" 
                    accept="image/*" 
                    onChange={(e) => handleFileChange(e, side)}
                    className="absolute inset-0 opacity-0 cursor-pointer"
                />
            </div>
        </div>
    );

    return (
        <div className="ekyc-light-page min-h-screen px-4 py-10 md:px-6 animate-fade-in">
            <div className="mx-auto max-w-5xl">
            {/* Back button */}
            <button onClick={() => navigate('/profile')} className="mb-10 flex items-center gap-2 text-xs uppercase tracking-[0.22em] text-[#2F2418]/55 hover:text-[#9A6A2F] transition-colors">
                <ArrowLeft className="w-4 h-4" /> Quay lại hồ sơ
            </button>

            {/* Header */}
            <div className="text-center mb-14">
                <div className="w-16 h-16 bg-white/85 border border-[#9A6A2F]/18 rounded-full flex items-center justify-center mx-auto mb-5 shadow-[0_18px_50px_rgba(47,36,24,0.10)]">
                    <ShieldCheck className="w-8 h-8 text-[#9A6A2F]" />
                </div>
                <p className="auction-kicker mb-4">Private Client Verification</p>
                <h1 className="font-serif text-5xl md:text-7xl font-normal leading-[0.95] text-[#2F2418] mb-5">Xác Thực Danh Tính</h1>
                <p className="text-[#2F2418]/58 max-w-xl mx-auto leading-7">Cung cấp ảnh CCCD để mở khóa quyền tham gia các phiên đấu giá giá trị cao trong không gian private auction.</p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-8">
                {/* Upload Grid */}
                <div className="rounded-[2rem] border border-[#9A6A2F]/14 bg-white/72 p-6 md:p-8 shadow-[0_28px_90px_rgba(47,36,24,0.10)] backdrop-blur-xl">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <UploadBox side="front" label="Mặt trước CCCD" preview={previews.front} />
                        <UploadBox side="back" label="Mặt sau CCCD" preview={previews.back} />
                    </div>
                </div>

                {/* Error */}
                {error && (
                    <div className="rounded-2xl bg-red-50/90 border border-red-200 text-red-700 px-5 py-4 flex items-start gap-3 animate-fade-in shadow-[0_18px_50px_rgba(127,29,29,0.08)]">
                        <AlertCircle className="w-5 h-5 shrink-0 mt-0.5" />
                        <p className="text-sm font-medium">{error}</p>
                    </div>
                )}

                {/* Actions */}
                <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
                    <button 
                        type="submit" 
                        disabled={loading}
                        className="auction-hero-cta justify-center w-full sm:w-auto disabled:opacity-60 disabled:cursor-not-allowed"
                    >
                        {loading ? (
                            <span className="flex items-center gap-2">
                                <svg className="animate-spin h-5 w-5" viewBox="0 0 24 24"><circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" /><path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" /></svg>
                                Đang phân tích...
                            </span>
                        ) : (
                            <>
                                <ShieldCheck className="w-5 h-5" /> Bắt Đầu Xác Thực
                            </>
                        )}
                    </button>
                    <button 
                        type="button" 
                        onClick={() => navigate('/profile')}
                        className="text-sm text-[#2F2418]/52 hover:text-[#9A6A2F] font-medium transition-colors"
                    >
                        Để sau
                    </button>
                </div>
            </form>

            {/* Info Section */}
            <div className="mt-14 rounded-[2rem] border border-[#9A6A2F]/14 bg-white/68 p-6 md:p-8 shadow-[0_28px_90px_rgba(47,36,24,0.08)] backdrop-blur-xl">
                <h3 className="font-serif text-3xl font-normal text-[#2F2418] mb-6 flex items-center gap-3">
                    <Lock className="w-5 h-5 text-[#9A6A2F]" /> Tại sao cần xác thực?
                </h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {[
                        { icon: ShieldCheck, text: 'Đảm bảo tính minh bạch và uy tín cho toàn bộ cộng đồng' },
                        { icon: CreditCard, text: 'Kích hoạt quyền tham gia các phiên đấu giá giá trị cao' },
                        { icon: Lock, text: 'Bảo vệ tài khoản và các giao dịch tài chính của bạn' },
                        { icon: Eye, text: 'Sử dụng công nghệ AI Vision bảo mật và mã hóa thông tin' },
                    ].map((item, idx) => (
                        <div key={idx} className="flex items-start gap-3 rounded-2xl bg-[#FFF8ED]/82 border border-[#9A6A2F]/10 p-4">
                            <div className="w-8 h-8 bg-[#FFF8ED] rounded-full flex items-center justify-center shrink-0 shadow-[0_12px_28px_rgba(47,36,24,0.08)]">
                                <item.icon className="w-4 h-4 text-[#9A6A2F]" />
                            </div>
                            <p className="text-sm text-[#2F2418]/64 leading-relaxed">{item.text}</p>
                        </div>
                    ))}
                </div>
            </div>
            </div>
        </div>
    );
}
