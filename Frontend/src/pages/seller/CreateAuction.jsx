import { useState, useEffect, useRef } from 'react';
import {
    Camera, Video, Plus, Info, X, ImagePlus, ArrowLeft, Sparkles, Clock,
    DollarSign, FileText, Tag, ChevronRight, ChevronLeft, CheckCircle2,
    Gavel, Shield, Zap, Calendar, Layers, Eye, Package, AlertCircle,
    Timer, TrendingUp, Users, Star
} from 'lucide-react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import apiClient from '@/services/apiClient';
import { useToast } from '@/components/Elements/Toast';

const STEPS = [
    { id: 1, label: 'Thông tin sản phẩm', icon: Package },
    { id: 2, label: 'Thiết lập phiên đấu giá', icon: Gavel },
    { id: 3, label: 'Xác nhận & Đăng', icon: CheckCircle2 },
];

const money = (v) => Number(v || 0).toLocaleString('vi-VN');

const DURATION_PRESETS = [
    { label: '1 ngày', hours: 24 },
    { label: '3 ngày', hours: 72 },
    { label: '7 ngày', hours: 168 },
    { label: '14 ngày', hours: 336 },
];

export default function CreateAuction() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const relistId = searchParams.get('relist_id');
    const { toast } = useToast();

    const [step, setStep] = useState(1);
    const [categories, setCategories] = useState([]);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const [formData, setFormData] = useState({
        name: '',
        description: '',
        origin: '',
        category_id: '',
        condition: 'NEW',
        start_price: 5000,
        step_price: 200,
        deposit_amount: 500,
        start_time: new Date(Date.now() + 30 * 60000).toISOString().slice(0, 16),
        end_time: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().slice(0, 16),
        auto_extend: true,
        extend_minutes: 2,
        reserve_price: '',
        buy_now_price: '',
    });

    const [mainImage, setMainImage] = useState(null);
    const [additionalImages, setAdditionalImages] = useState([]);
    const [oldImages, setOldImages] = useState([]);
    const [errors, setErrors] = useState({});
    const mainImageRef = useRef(null);

    useEffect(() => {
        apiClient.get('/auctions/categories')
            .then(res => setCategories(res.data?.result || (Array.isArray(res.data) ? res.data : [])))
            .catch(console.error);
        if (relistId) {
            apiClient.get(`/auctions/${relistId}`).then(res => {
                const auc = res.data?.result || res.data;
                setFormData(prev => ({
                    ...prev,
                    name: auc.product_name || '',
                    description: auc.description || '',
                    origin: auc.origin || '',
                    category_id: auc.category_id || '',
                    condition: auc.condition || 'NEW',
                    start_price: auc.start_price || 5000,
                    step_price: auc.step_price || 200,
                    deposit_amount: auc.deposit_amount || 500,
                }));
                if (auc.images) setOldImages(auc.images);
            }).catch(console.error);
        }
    }, [relistId]);

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData(prev => ({ ...prev, [name]: type === 'checkbox' ? checked : value }));
        if (errors[name]) setErrors(prev => ({ ...prev, [name]: '' }));
    };

    const handleMainImage = (e) => {
        if (e.target.files?.[0]) setMainImage(e.target.files[0]);
    };

    const handleAdditionalImages = (e) => {
        if (e.target.files) setAdditionalImages(prev => [...prev, ...Array.from(e.target.files)]);
    };

    const removeAdditionalImage = (index) => {
        setAdditionalImages(prev => prev.filter((_, i) => i !== index));
    };

    const applyDurationPreset = (hours) => {
        const start = new Date(formData.start_time);
        const end = new Date(start.getTime() + hours * 3600000);
        setFormData(prev => ({ ...prev, end_time: end.toISOString().slice(0, 16) }));
    };

    // --- VALIDATION ---
    const validateStep1 = () => {
        const e = {};
        if (!formData.name.trim()) e.name = 'Tên vật phẩm không được để trống';
        if (!formData.description.trim()) e.description = 'Mô tả không được để trống';
        if (!formData.category_id) e.category_id = 'Vui lòng chọn danh mục';
        const hasMainImage = mainImage || oldImages.some(i => i.is_cover);
        if (!hasMainImage) e.mainImage = 'Vui lòng tải lên ảnh chính';
        setErrors(e);
        return Object.keys(e).length === 0;
    };

    const validateStep2 = () => {
        const e = {};
        if (!formData.start_price || Number(formData.start_price) < 1000) e.start_price = 'Giá khởi điểm tối thiểu là 1.000 đ';
        if (!formData.step_price || Number(formData.step_price) < 100) e.step_price = 'Bước giá tối thiểu là 100 đ';
        if (!formData.deposit_amount || Number(formData.deposit_amount) < 0) e.deposit_amount = 'Tiền cọc không hợp lệ';
        const start = new Date(formData.start_time);
        const end = new Date(formData.end_time);
        if (start >= end) e.end_time = 'Thời gian kết thúc phải sau thời gian bắt đầu';
        if (start < new Date()) e.start_time = 'Thời gian bắt đầu phải trong tương lai';
        setErrors(e);
        return Object.keys(e).length === 0;
    };

    const goNext = () => {
        if (step === 1 && !validateStep1()) return;
        if (step === 2 && !validateStep2()) return;
        setStep(s => Math.min(s + 1, 3));
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    const goPrev = () => {
        setStep(s => Math.max(s - 1, 1));
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    const handleSubmit = async () => {
        setIsSubmitting(true);
        try {
            const data = new FormData();
            const submitFields = { ...formData };
            if (!submitFields.reserve_price) delete submitFields.reserve_price;
            if (!submitFields.buy_now_price) delete submitFields.buy_now_price;
            Object.keys(submitFields).forEach(key => data.append(key, submitFields[key]));
            if (relistId) data.append('relist_id', relistId);
            if (mainImage) data.append('files', mainImage);
            additionalImages.forEach(img => data.append('files', img));

            const res = await apiClient.post('/auctions/create-auction', data, {
                headers: { 'Content-Type': 'multipart/form-data' }
            });
            toast.success('Đăng ký phiên đấu giá thành công! 🎉');
            setTimeout(() => navigate(`/auctions/${res.data.auction_id}`), 1200);
        } catch (error) {
            const detail = error.response?.data?.detail;
            const fieldNames = {
                name: 'Tên vật phẩm', description: 'Mô tả', origin: 'Nguồn gốc',
                category_id: 'Danh mục', start_price: 'Giá khởi điểm', step_price: 'Bước giá',
                deposit_amount: 'Tiền cọc', start_time: 'Thời gian bắt đầu', end_time: 'Thời gian kết thúc'
            };
            let msg = 'Lỗi tạo phiên đấu giá';
            if (typeof detail === 'string') msg = detail;
            else if (Array.isArray(detail)) {
                msg = detail.map(err => {
                    const field = err.loc[err.loc.length - 1];
                    return `${fieldNames[field] || field}: ${err.msg === 'Field required' ? 'không được để trống' : err.msg}`;
                }).join(', ');
            }
            toast.error(msg, 6000);
        } finally {
            setIsSubmitting(false);
        }
    };

    const inputClass = (field) =>
        `w-full bg-[#F8F1E6]/80 border ${errors[field] ? 'border-red-400' : 'border-[#2F2418]/12'} px-4 py-3 text-sm text-[#2F2418] placeholder-[#2F2418]/35 focus:outline-none focus:ring-2 ${errors[field] ? 'focus:ring-red-300' : 'focus:ring-[#9A6A2F]/20'} focus:border-[#9A6A2F]/60 transition-all rounded`;

    const labelClass = 'block text-sm font-semibold text-[#2F2418]/70 mb-2';

    const selectedCategory = categories.find(c => String(c.id) === String(formData.category_id));

    const durationLabel = () => {
        const start = new Date(formData.start_time);
        const end = new Date(formData.end_time);
        const diff = end - start;
        if (diff <= 0) return 'Không hợp lệ';
        const days = Math.floor(diff / 86400000);
        const hours = Math.floor((diff % 86400000) / 3600000);
        if (days > 0 && hours > 0) return `${days} ngày ${hours} giờ`;
        if (days > 0) return `${days} ngày`;
        return `${hours} giờ`;
    };

    return (
        <div className="min-h-screen bg-[radial-gradient(circle_at_12%_0%,rgba(154,106,47,0.14),transparent_30rem),linear-gradient(180deg,#F8F1E6,#FFF8ED_60%,#F8F1E6)] text-[#2F2418]">
            <div className="max-w-5xl mx-auto px-4 md:px-6 py-10">
                {/* Back */}
                <button onClick={() => navigate(-1)} className="flex items-center gap-2 text-sm text-[#2F2418]/50 hover:text-[#9A6A2F] transition-colors mb-8 group">
                    <ArrowLeft className="w-4 h-4 group-hover:-translate-x-1 transition-transform" />
                    Quay lại
                </button>

                {/* Header */}
                <div className="mb-10">
                    <p className="text-xs uppercase tracking-[0.35em] text-[#9A6A2F] mb-3">Consign With Us</p>
                    <h1 className="font-serif text-4xl md:text-5xl font-medium text-[#2F2418] mb-3">
                        {relistId ? 'Đăng lại đấu giá' : 'Tạo Phiên Đấu Giá'}
                    </h1>
                    <p className="text-[#2F2418]/55 max-w-xl leading-7 text-sm">
                        Hoàn tất 3 bước đơn giản để đưa vật phẩm của bạn lên sàn đấu giá một cách chuyên nghiệp và minh bạch.
                    </p>
                </div>

                {/* Step Indicator */}
                <div className="mb-10">
                    <div className="flex items-center gap-0">
                        {STEPS.map((s, idx) => {
                            const isActive = step === s.id;
                            const isDone = step > s.id;
                            const Icon = s.icon;
                            return (
                                <div key={s.id} className="flex items-center flex-1 last:flex-none">
                                    <div className={`flex items-center gap-3 py-3 px-4 rounded-xl transition-all duration-300 ${isActive ? 'bg-[#9A6A2F] text-[#F8F1E6] shadow-lg shadow-[#9A6A2F]/20' : isDone ? 'bg-[#9A6A2F]/15 text-[#9A6A2F]' : 'bg-white/60 text-[#2F2418]/40 border border-[#2F2418]/08'}`}>
                                        {isDone ? (
                                            <CheckCircle2 className="w-5 h-5 shrink-0" />
                                        ) : (
                                            <Icon className="w-5 h-5 shrink-0" />
                                        )}
                                        <span className="text-xs font-bold whitespace-nowrap hidden sm:inline">{s.label}</span>
                                        <span className="text-xs font-bold sm:hidden">{s.id}</span>
                                    </div>
                                    {idx < STEPS.length - 1 && (
                                        <div className={`h-px flex-1 mx-2 transition-all duration-500 ${step > s.id ? 'bg-[#9A6A2F]/40' : 'bg-[#2F2418]/10'}`} />
                                    )}
                                </div>
                            );
                        })}
                    </div>
                </div>

                {/* ======== STEP 1: Product Info ======== */}
                {step === 1 && (
                    <div className="space-y-6 animate-fade-in">
                        {/* Images */}
                        <SectionCard title="Hình ảnh vật phẩm" icon={Camera}>
                            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                                {/* Main Image */}
                                <div>
                                    <p className="text-xs font-bold text-[#2F2418]/50 uppercase tracking-wider mb-3">Ảnh bìa *</p>
                                    <div
                                        className={`aspect-square bg-[#F8F1E6] border-2 border-dashed ${errors.mainImage ? 'border-red-400' : 'border-[#9A6A2F]/25'} flex flex-col items-center justify-center relative hover:border-[#9A6A2F] hover:bg-[#9A6A2F]/5 transition-all cursor-pointer overflow-hidden rounded-xl group`}
                                        onClick={() => mainImageRef.current?.click()}
                                    >
                                        {mainImage ? (
                                            <img src={URL.createObjectURL(mainImage)} className="w-full h-full object-cover" alt="Main" />
                                        ) : oldImages.find(i => i.is_cover) ? (
                                            <img src={oldImages.find(i => i.is_cover).url} className="w-full h-full object-cover opacity-80" alt="Cover" />
                                        ) : (
                                            <div className="text-center p-4">
                                                <div className="w-12 h-12 bg-[#9A6A2F]/10 border border-[#9A6A2F]/20 rounded-xl flex items-center justify-center mx-auto mb-3 group-hover:bg-[#9A6A2F]/20 transition-colors">
                                                    <ImagePlus className="w-5 h-5 text-[#9A6A2F]" />
                                                </div>
                                                <span className="text-xs font-semibold text-[#2F2418]/60">Tải lên ảnh chính</span>
                                                <p className="text-[10px] text-[#2F2418]/40 mt-1">PNG, JPG tối đa 10MB</p>
                                            </div>
                                        )}
                                        <input ref={mainImageRef} type="file" accept="image/*" onChange={handleMainImage} className="hidden" />
                                        {mainImage && (
                                            <button
                                                onClick={(e) => { e.stopPropagation(); setMainImage(null); }}
                                                className="absolute top-2 right-2 bg-red-500 text-white rounded-lg p-1.5 shadow-lg opacity-0 group-hover:opacity-100 transition-opacity"
                                            >
                                                <X className="w-3 h-3" />
                                            </button>
                                        )}
                                    </div>
                                    {errors.mainImage && <p className="text-red-500 text-xs mt-1.5 flex items-center gap-1"><AlertCircle className="w-3 h-3" />{errors.mainImage}</p>}
                                </div>

                                {/* Additional Images */}
                                <div className="md:col-span-2">
                                    <p className="text-xs font-bold text-[#2F2418]/50 uppercase tracking-wider mb-3">
                                        Ảnh chi tiết <span className="text-[#9A6A2F]">({additionalImages.length})</span>
                                    </p>
                                    <div className="grid grid-cols-3 sm:grid-cols-4 gap-3">
                                        {additionalImages.length === 0 && oldImages.filter(i => !i.is_cover).map((img, idx) => (
                                            <div key={`old-${idx}`} className="aspect-square bg-slate-50 border border-[#2F2418]/10 rounded-xl relative group overflow-hidden">
                                                <img src={img.url} className="w-full h-full object-cover opacity-80" alt="" />
                                                <div className="absolute inset-0 bg-black/30 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity rounded-xl">
                                                    <span className="text-[10px] text-white font-bold bg-black/40 px-2 py-1 rounded">Ảnh cũ</span>
                                                </div>
                                            </div>
                                        ))}
                                        {additionalImages.map((img, idx) => (
                                            <div key={`new-${idx}`} className="aspect-square border border-[#2F2418]/10 rounded-xl relative group overflow-hidden">
                                                <img src={URL.createObjectURL(img)} className="w-full h-full object-cover" alt="" />
                                                <button onClick={() => removeAdditionalImage(idx)} className="absolute top-1.5 right-1.5 bg-red-500 text-white rounded-lg p-1 shadow-lg opacity-0 group-hover:opacity-100 transition-opacity">
                                                    <X className="w-3 h-3" />
                                                </button>
                                            </div>
                                        ))}
                                        <label className="aspect-square bg-[#F8F1E6] border-2 border-dashed border-[#9A6A2F]/25 rounded-xl flex flex-col items-center justify-center cursor-pointer hover:border-[#9A6A2F] hover:bg-[#9A6A2F]/5 transition-all">
                                            <Plus className="w-5 h-5 text-[#9A6A2F]/70 mb-1" />
                                            <span className="text-[10px] font-semibold text-[#2F2418]/50">Thêm ảnh</span>
                                            <input type="file" multiple accept="image/*" onChange={handleAdditionalImages} className="hidden" />
                                        </label>
                                        <div className="aspect-square bg-[#F8F1E6] border border-[#2F2418]/08 rounded-xl flex flex-col items-center justify-center opacity-35 cursor-not-allowed">
                                            <Video className="w-5 h-5 text-[#2F2418]/40 mb-1" />
                                            <span className="text-[9px] text-[#2F2418]/40 text-center px-1">Video 360°<br/>(Sắp có)</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </SectionCard>

                        {/* Basic Info */}
                        <SectionCard title="Thông tin cơ bản" icon={FileText}>
                            <div className="space-y-5">
                                <div>
                                    <label className={labelClass}>Tên vật phẩm *</label>
                                    <input type="text" name="name" value={formData.name} onChange={handleChange}
                                        className={inputClass('name')}
                                        placeholder="Ví dụ: Bình gốm thời Minh, Thế kỷ 15" />
                                    <ErrorMsg msg={errors.name} />
                                </div>

                                <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                                    <div>
                                        <label className={labelClass}>Danh mục *</label>
                                        <select name="category_id" value={formData.category_id} onChange={handleChange} className={inputClass('category_id')}>
                                            <option value="">Chọn danh mục...</option>
                                            {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                                        </select>
                                        <ErrorMsg msg={errors.category_id} />
                                    </div>
                                    <div>
                                        <label className={labelClass}>Tình trạng vật phẩm</label>
                                        <div className="grid grid-cols-2 gap-3">
                                            {[{ v: 'NEW', label: 'Mới / Hoàn hảo', icon: Star }, { v: 'USED', label: 'Đã qua sử dụng', icon: Package }].map(opt => (
                                                <button key={opt.v} type="button"
                                                    onClick={() => setFormData(p => ({ ...p, condition: opt.v }))}
                                                    className={`flex items-center gap-2 p-3 rounded border-2 text-sm font-semibold transition-all ${formData.condition === opt.v ? 'border-[#9A6A2F] bg-[#9A6A2F]/10 text-[#9A6A2F]' : 'border-[#2F2418]/12 text-[#2F2418]/60 hover:border-[#9A6A2F]/40'}`}>
                                                    <opt.icon className="w-4 h-4" />{opt.label}
                                                </button>
                                            ))}
                                        </div>
                                    </div>
                                </div>
                                <div>
                                    <label className={labelClass}>Nguồn gốc xuất xứ</label>
                                    <input type="text" name="origin" value={formData.origin} onChange={handleChange}
                                        className={inputClass('origin')}
                                        placeholder="Gia tộc Nguyễn, Huế, Việt Nam" />
                                </div>
                            </div>
                        </SectionCard>

                        {/* Description */}
                        <SectionCard title="Mô tả chi tiết" icon={Sparkles}>
                            <div>
                                <label className={labelClass}>Câu chuyện & Ý nghĩa vật phẩm *</label>
                                <textarea name="description" value={formData.description} onChange={handleChange}
                                    className={`${inputClass('description')} min-h-36 resize-none`}
                                    placeholder="Kể về lịch sử, những người chủ sở hữu trước đây, chứng nhận giám định..." />
                                <div className="flex justify-between items-center mt-1">
                                    <ErrorMsg msg={errors.description} />
                                    <span className="text-xs text-[#2F2418]/35 ml-auto">{formData.description.length} ký tự</span>
                                </div>
                            </div>
                        </SectionCard>
                    </div>
                )}

                {/* ======== STEP 2: Auction Setup ======== */}
                {step === 2 && (
                    <div className="space-y-6 animate-fade-in">
                        {/* Pricing */}
                        <SectionCard title="Cấu hình giá" icon={DollarSign}>
                            <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
                                <div>
                                    <label className={labelClass}>Giá khởi điểm (VNĐ) *</label>
                                    <div className="relative">
                                        <input type="number" name="start_price" value={formData.start_price} onChange={handleChange}
                                            className={inputClass('start_price')} min="1000" />
                                    </div>
                                    <p className="text-xs text-[#9A6A2F] mt-1 font-medium">{money(formData.start_price)} đ</p>
                                    <ErrorMsg msg={errors.start_price} />
                                </div>
                                <div>
                                    <label className={labelClass}>Bước giá tối thiểu (VNĐ) *</label>
                                    <input type="number" name="step_price" value={formData.step_price} onChange={handleChange}
                                        className={inputClass('step_price')} min="100" />
                                    <p className="text-xs text-[#9A6A2F] mt-1 font-medium">{money(formData.step_price)} đ / lần đấu</p>
                                    <ErrorMsg msg={errors.step_price} />
                                </div>
                                <div>
                                    <label className={labelClass}>Tiền đặt cọc (VNĐ)</label>
                                    <input type="number" name="deposit_amount" value={formData.deposit_amount} onChange={handleChange}
                                        className={inputClass('deposit_amount')} min="0" />
                                    <p className="text-xs text-[#9A6A2F] mt-1 font-medium">{money(formData.deposit_amount)} đ</p>
                                    <ErrorMsg msg={errors.deposit_amount} />
                                </div>
                            </div>

                            {/* Optional Pricing */}
                            <div className="mt-6 pt-5 border-t border-[#2F2418]/08 grid grid-cols-1 md:grid-cols-2 gap-5">
                                <div>
                                    <label className={`${labelClass} flex items-center gap-2`}>
                                        <TrendingUp className="w-3.5 h-3.5 text-[#9A6A2F]" />
                                        Giá đặt trước tối thiểu (Tùy chọn)
                                    </label>
                                    <input type="number" name="reserve_price" value={formData.reserve_price} onChange={handleChange}
                                        className={inputClass('reserve_price')} min="0"
                                        placeholder="Để trống nếu không áp dụng" />
                                    <p className="text-[10px] text-[#2F2418]/40 mt-1">Phiên chỉ chốt khi giá đạt mức này</p>
                                </div>
                                <div>
                                    <label className={`${labelClass} flex items-center gap-2`}>
                                        <Zap className="w-3.5 h-3.5 text-amber-500" />
                                        Giá mua ngay (Buy Now) (Tùy chọn)
                                    </label>
                                    <input type="number" name="buy_now_price" value={formData.buy_now_price} onChange={handleChange}
                                        className={inputClass('buy_now_price')} min="0"
                                        placeholder="Để trống nếu không áp dụng" />
                                    <p className="text-[10px] text-[#2F2418]/40 mt-1">Người mua có thể kết thúc ngay với giá này</p>
                                </div>
                            </div>
                        </SectionCard>

                        {/* Schedule */}
                        <SectionCard title="Lịch trình phiên đấu giá" icon={Calendar}>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-5 mb-6">
                                <div>
                                    <label className={`${labelClass} flex items-center gap-2`}>
                                        <Clock className="w-3.5 h-3.5 text-[#9A6A2F]" />Thời gian bắt đầu *
                                    </label>
                                    <input type="datetime-local" name="start_time" value={formData.start_time} onChange={handleChange}
                                        className={inputClass('start_time')} />
                                    <ErrorMsg msg={errors.start_time} />
                                </div>
                                <div>
                                    <label className={`${labelClass} flex items-center gap-2`}>
                                        <Timer className="w-3.5 h-3.5 text-[#9A6A2F]" />Thời gian kết thúc *
                                    </label>
                                    <input type="datetime-local" name="end_time" value={formData.end_time} onChange={handleChange}
                                        className={inputClass('end_time')} />
                                    <ErrorMsg msg={errors.end_time} />
                                </div>
                            </div>

                            {/* Duration Presets */}
                            <div>
                                <p className="text-xs font-bold text-[#2F2418]/50 uppercase tracking-wider mb-3">Thời lượng nhanh</p>
                                <div className="flex flex-wrap gap-2">
                                    {DURATION_PRESETS.map(p => (
                                        <button key={p.label} type="button"
                                            onClick={() => applyDurationPreset(p.hours)}
                                            className="px-4 py-2 text-xs font-bold border border-[#9A6A2F]/25 text-[#9A6A2F] hover:bg-[#9A6A2F] hover:text-[#F8F1E6] rounded-full transition-all">
                                            {p.label}
                                        </button>
                                    ))}
                                </div>
                            </div>

                            {/* Duration summary */}
                            {formData.start_time && formData.end_time && (
                                <div className="mt-4 bg-[#9A6A2F]/08 border border-[#9A6A2F]/20 rounded-xl p-4 flex items-center gap-3">
                                    <Timer className="w-5 h-5 text-[#9A6A2F] shrink-0" />
                                    <div>
                                        <p className="text-sm font-bold text-[#2F2418]">Thời lượng phiên: <span className="text-[#9A6A2F]">{durationLabel()}</span></p>
                                        <p className="text-xs text-[#2F2418]/50 mt-0.5">
                                            Bắt đầu: {new Date(formData.start_time).toLocaleString('vi-VN')} → Kết thúc: {new Date(formData.end_time).toLocaleString('vi-VN')}
                                        </p>
                                    </div>
                                </div>
                            )}
                        </SectionCard>

                        {/* Advanced Options */}
                        <SectionCard title="Tùy chọn nâng cao" icon={Layers}>
                            <div className="space-y-4">
                                <label className="flex items-start gap-4 cursor-pointer group">
                                    <div className={`relative w-12 h-6 rounded-full transition-colors shrink-0 mt-0.5 ${formData.auto_extend ? 'bg-[#9A6A2F]' : 'bg-[#2F2418]/15'}`}
                                        onClick={() => setFormData(p => ({ ...p, auto_extend: !p.auto_extend }))}>
                                        <span className={`absolute top-1 w-4 h-4 bg-white rounded-full shadow transition-transform ${formData.auto_extend ? 'translate-x-7' : 'translate-x-1'}`} />
                                    </div>
                                    <div>
                                        <p className="text-sm font-bold text-[#2F2418]">Tự động gia hạn phiên</p>
                                        <p className="text-xs text-[#2F2418]/50 mt-0.5">Nếu có lượt đấu trong những giây cuối, phiên sẽ được gia hạn thêm để đảm bảo công bằng</p>
                                    </div>
                                </label>

                                {formData.auto_extend && (
                                    <div className="ml-16 bg-[#F8F1E6] border border-[#2F2418]/08 rounded-xl p-4">
                                        <label className={labelClass}>Thời gian gia hạn (phút)</label>
                                        <div className="flex items-center gap-3">
                                            {[1, 2, 5, 10].map(v => (
                                                <button key={v} type="button"
                                                    onClick={() => setFormData(p => ({ ...p, extend_minutes: v }))}
                                                    className={`w-12 h-10 rounded-lg text-sm font-bold border-2 transition-all ${formData.extend_minutes === v ? 'border-[#9A6A2F] bg-[#9A6A2F] text-[#F8F1E6]' : 'border-[#2F2418]/12 text-[#2F2418]/60 hover:border-[#9A6A2F]/50'}`}>
                                                    {v}
                                                </button>
                                            ))}
                                        </div>
                                    </div>
                                )}
                            </div>
                        </SectionCard>
                    </div>
                )}

                {/* ======== STEP 3: Review ======== */}
                {step === 3 && (
                    <div className="space-y-6 animate-fade-in">
                        <div className="bg-gradient-to-br from-[#9A6A2F]/12 to-[#9A6A2F]/05 border border-[#9A6A2F]/25 rounded-2xl p-6 flex items-start gap-4">
                            <CheckCircle2 className="w-6 h-6 text-[#9A6A2F] shrink-0 mt-0.5" />
                            <div>
                                <h3 className="font-bold text-[#2F2418] mb-1">Xem lại trước khi đăng</h3>
                                <p className="text-sm text-[#2F2418]/60">Vui lòng kiểm tra lại thông tin bên dưới. Sau khi đăng, bạn không thể chỉnh sửa một số thông tin chính.</p>
                            </div>
                        </div>

                        {/* Preview Product */}
                        <SectionCard title="Thông tin sản phẩm" icon={Package}>
                            <div className="flex flex-col md:flex-row gap-6">
                                {(mainImage || oldImages.find(i => i.is_cover)) && (
                                    <div className="w-full md:w-48 h-48 rounded-xl overflow-hidden shrink-0 border border-[#2F2418]/08">
                                        <img
                                            src={mainImage ? URL.createObjectURL(mainImage) : oldImages.find(i => i.is_cover)?.url}
                                            className="w-full h-full object-cover"
                                            alt="Preview"
                                        />
                                    </div>
                                )}
                                <div className="flex-1 space-y-3">
                                    <ReviewRow label="Tên vật phẩm" value={formData.name} bold />
                                    <ReviewRow label="Danh mục" value={selectedCategory?.name || 'Chưa chọn'} />
                                    <ReviewRow label="Tình trạng" value={formData.condition === 'NEW' ? 'Mới / Hoàn hảo' : 'Đã qua sử dụng'} />
                                    <ReviewRow label="Nguồn gốc" value={formData.origin || '—'} />
                                    <div>
                                        <span className="text-xs text-[#2F2418]/50 font-semibold uppercase tracking-wider">Mô tả</span>
                                        <p className="text-sm text-[#2F2418]/75 mt-1 leading-6 line-clamp-3">{formData.description}</p>
                                    </div>
                                    <p className="text-xs text-[#2F2418]/40">{additionalImages.length + (oldImages.filter(i => !i.is_cover).length)} ảnh chi tiết</p>
                                </div>
                            </div>
                        </SectionCard>

                        {/* Auction Setup Summary */}
                        <SectionCard title="Thiết lập phiên đấu giá" icon={Gavel}>
                            <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
                                <ReviewCard label="Giá khởi điểm" value={`${money(formData.start_price)} đ`} accent />
                                <ReviewCard label="Bước giá" value={`${money(formData.step_price)} đ`} />
                                <ReviewCard label="Tiền đặt cọc" value={`${money(formData.deposit_amount)} đ`} />
                                {formData.reserve_price && <ReviewCard label="Giá đặt trước" value={`${money(formData.reserve_price)} đ`} />}
                                {formData.buy_now_price && <ReviewCard label="Giá mua ngay" value={`${money(formData.buy_now_price)} đ`} />}
                                <ReviewCard label="Thời lượng phiên" value={durationLabel()} />
                            </div>
                            <div className="mt-4 p-4 bg-[#F8F1E6] rounded-xl border border-[#2F2418]/08 space-y-1.5">
                                <div className="flex items-center gap-2 text-sm text-[#2F2418]/70">
                                    <Clock className="w-4 h-4 text-[#9A6A2F]" />
                                    <strong>Bắt đầu:</strong> {new Date(formData.start_time).toLocaleString('vi-VN')}
                                </div>
                                <div className="flex items-center gap-2 text-sm text-[#2F2418]/70">
                                    <Timer className="w-4 h-4 text-[#9A6A2F]" />
                                    <strong>Kết thúc:</strong> {new Date(formData.end_time).toLocaleString('vi-VN')}
                                </div>
                                {formData.auto_extend && (
                                    <div className="flex items-center gap-2 text-sm text-[#9A6A2F] font-medium">
                                        <Shield className="w-4 h-4" />
                                        Gia hạn tự động: +{formData.extend_minutes} phút khi có đấu giá cuối phiên
                                    </div>
                                )}
                            </div>
                        </SectionCard>

                        {/* Terms */}
                        <div className="bg-amber-50/60 border border-amber-200/60 rounded-2xl p-5 flex items-start gap-3">
                            <Info className="w-5 h-5 text-amber-600 shrink-0 mt-0.5" />
                            <div className="text-sm text-amber-800/80">
                                <p className="font-bold mb-1">Cam kết khi đăng ký</p>
                                <ul className="space-y-1 text-xs leading-5">
                                    <li>• Thông tin vật phẩm là trung thực, hình ảnh thực tế không qua chỉnh sửa.</li>
                                    <li>• Vật phẩm sẵn sàng giao dịch khi phiên kết thúc thành công.</li>
                                    <li>• Sàn đấu giá có quyền từ chối hoặc xóa phiên vi phạm điều khoản.</li>
                                </ul>
                            </div>
                        </div>
                    </div>
                )}

                {/* Navigation Buttons */}
                <div className="flex items-center justify-between mt-10 pt-8 border-t border-[#2F2418]/10">
                    <button
                        onClick={step === 1 ? () => navigate(-1) : goPrev}
                        className="flex items-center gap-2 px-6 py-3 border border-[#2F2418]/15 text-sm font-semibold text-[#2F2418]/65 hover:border-[#9A6A2F]/50 hover:text-[#9A6A2F] rounded-xl transition-all group"
                    >
                        <ChevronLeft className="w-4 h-4 group-hover:-translate-x-1 transition-transform" />
                        {step === 1 ? 'Hủy' : 'Bước trước'}
                    </button>

                    {step < 3 ? (
                        <button onClick={goNext}
                            className="flex items-center gap-2 px-8 py-3 bg-[#9A6A2F] text-[#F8F1E6] text-sm font-bold rounded-xl hover:bg-[#2F2418] transition-colors shadow-lg shadow-[#9A6A2F]/20 group">
                            Tiếp theo
                            <ChevronRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
                        </button>
                    ) : (
                        <button
                            onClick={handleSubmit}
                            disabled={isSubmitting}
                            className="flex items-center gap-2 px-10 py-3.5 bg-[#9A6A2F] text-[#F8F1E6] text-sm font-bold rounded-xl hover:bg-[#2F2418] transition-colors shadow-lg shadow-[#9A6A2F]/20 disabled:opacity-60 disabled:cursor-not-allowed"
                        >
                            {isSubmitting ? (
                                <>
                                    <div className="w-4 h-4 border-2 border-[#F8F1E6]/40 border-t-[#F8F1E6] rounded-full animate-spin" />
                                    Đang đăng...
                                </>
                            ) : (
                                <>
                                    <Sparkles className="w-4 h-4" />
                                    Đăng ký đấu giá
                                </>
                            )}
                        </button>
                    )}
                </div>
            </div>
        </div>
    );
}

// Helper components
function SectionCard({ title, icon: Icon, children }) {
    return (
        <div className="bg-white/80 backdrop-blur-sm border border-[#2F2418]/08 rounded-2xl shadow-sm p-6 md:p-8">
            <h3 className="text-base font-bold text-[#2F2418] mb-6 flex items-center gap-2.5">
                <div className="w-8 h-8 bg-[#9A6A2F]/10 rounded-lg flex items-center justify-center">
                    <Icon className="w-4 h-4 text-[#9A6A2F]" />
                </div>
                {title}
            </h3>
            {children}
        </div>
    );
}

function ErrorMsg({ msg }) {
    if (!msg) return null;
    return (
        <p className="text-red-500 text-xs mt-1.5 flex items-center gap-1">
            <AlertCircle className="w-3 h-3 shrink-0" />{msg}
        </p>
    );
}

function ReviewRow({ label, value, bold }) {
    return (
        <div className="flex items-start gap-3">
            <span className="text-xs text-[#2F2418]/45 font-semibold uppercase tracking-wider w-28 shrink-0 pt-0.5">{label}</span>
            <span className={`text-sm text-[#2F2418] ${bold ? 'font-bold' : ''}`}>{value}</span>
        </div>
    );
}

function ReviewCard({ label, value, accent }) {
    return (
        <div className={`p-4 rounded-xl border ${accent ? 'border-[#9A6A2F]/30 bg-[#9A6A2F]/06' : 'border-[#2F2418]/08 bg-[#F8F1E6]/50'}`}>
            <p className="text-xs text-[#2F2418]/50 font-semibold uppercase tracking-wider mb-1">{label}</p>
            <p className={`text-base font-bold ${accent ? 'text-[#9A6A2F]' : 'text-[#2F2418]'}`}>{value}</p>
        </div>
    );
}
