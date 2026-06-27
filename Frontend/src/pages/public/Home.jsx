import { useEffect, useState } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import apiClient from '@/services/apiClient';
import AuctionCard from '@/components/Auction/AuctionCard';
import Skeleton from '@/components/Elements/Skeleton';
import EmptyState from '@/components/Elements/EmptyState';
import { useScrollAnimation } from '@/hooks/useScrollAnimation';
import { useRipple, useMagnetic } from '@/hooks/useRipple';
import { Search, SlidersHorizontal, Gavel, ShieldCheck, Truck, ArrowRight, X, ChevronRight, Clock, Award, Lock } from 'lucide-react';

const statusFilters = [
    { value: '', label: 'Tất cả' },
    { value: 'ACTIVE', label: 'Đang diễn ra' },
    { value: 'PENDING', label: 'Sắp bắt đầu' },
    { value: 'CLOSED', label: 'Đã kết thúc' },
];

// Hero showcase items - placeholder images (thay bằng ảnh thật sau)
const heroShowcaseItems = [
    {
        id: 1,
        name: 'Bình gốm Bát Tràng thế kỷ XVIII',
        image: 'https://images.unsplash.com/photo-1565193566173-7a0ee3dbe261?w=600&h=750&fit=crop&q=80',
        estimate: '15.000.000 - 25.000.000đ',
    },
    {
        id: 2,
        name: 'Đồng hồ để bàn Pháp cổ',
        image: 'https://images.unsplash.com/photo-1509048191080-d2984bad6ae5?w=600&h=750&fit=crop&q=80',
        estimate: '8.000.000 - 12.000.000đ',
    },
    {
        id: 3,
        name: 'Tranh sơn mài nghệ thuật',
        image: 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=600&h=750&fit=crop&q=80',
        estimate: '20.000.000 - 35.000.000đ',
    },
];

export default function Home() {
    const [auctions, setAuctions] = useState([]);
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchParams, setSearchParams] = useSearchParams();
    const [mobileFilterOpen, setMobileFilterOpen] = useState(false);
    const [heroIdx, setHeroIdx] = useState(0);
    
    const currentStatus = searchParams.get('status') || '';
    const currentCategory = searchParams.get('category_id') || '';

    const [sectionRef, sectionVisible] = useScrollAnimation({ threshold: 0.05 });
    const [ctaRef, ctaVisible] = useScrollAnimation({ threshold: 0.2 });
    
    const ripple = useRipple('rgba(255, 255, 255, 0.5)');
    const magneticProps = useMagnetic(0.2);

    // Hero carousel auto-rotate
    useEffect(() => {
        const interval = setInterval(() => {
            setHeroIdx(prev => (prev + 1) % heroShowcaseItems.length);
        }, 5000);
        return () => clearInterval(interval);
    }, []);

    useEffect(() => {
        apiClient.get('/auctions/categories')
            .then(res => setCategories(res.data?.result || (Array.isArray(res.data) ? res.data : [])))
            .catch(console.error);
    }, []);

    useEffect(() => {
        setLoading(true);
        const params = new URLSearchParams();
        if (currentStatus) params.append('status', currentStatus);
        if (currentCategory) params.append('category_id', currentCategory);
        
        const qs = params.toString();
        apiClient.get(`/auctions${qs ? '?' + qs : ''}`)
            .then(res => {
                const data = res.data?.result;
                let auctionsList = [];
                if (Array.isArray(data)) {
                    auctionsList = data;
                } else if (data && Array.isArray(data.content)) {
                    auctionsList = data.content; // Page response
                }
                setAuctions(auctionsList);
                setLoading(false);
            })
            .catch(err => {
                console.error(err);
                setLoading(false);
            });
    }, [currentStatus, currentCategory]);

    const updateFilter = (key, value) => {
        const newParams = new URLSearchParams(searchParams);
        if (value) {
            newParams.set(key, value);
        } else {
            newParams.delete(key);
        }
        setSearchParams(newParams);
    };

    const activeFilterCount = [currentStatus, currentCategory].filter(Boolean).length;
    const currentItem = heroShowcaseItems[heroIdx];

    return (
        <div>
            {/* ══════════ HERO ══════════ */}
            <section className="relative bg-slate-950 overflow-hidden">
                {/* Background image with overlay */}
                <div className="absolute inset-0">
                    {heroShowcaseItems.map((item, i) => (
                        <div
                            key={item.id}
                            className="absolute inset-0 transition-opacity duration-1000 ease-in-out"
                            style={{ opacity: i === heroIdx ? 1 : 0 }}
                        >
                            <img 
                                src={item.image} 
                                alt="" 
                                className={`w-full h-full object-cover ${i === heroIdx ? 'cinematic-hero-image' : ''}`}
                            />
                        </div>
                    ))}
                    {/* Dark overlay */}
                    <div className="absolute inset-0 bg-gradient-to-r from-slate-950/95 via-slate-950/80 to-slate-950/40" />
                    <div className="absolute inset-0 bg-gradient-to-t from-slate-950 via-transparent to-slate-950/30" />
                </div>
                
                <div className="relative max-w-7xl mx-auto px-4 md:px-6">
                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 lg:gap-16 items-center min-h-[520px] md:min-h-[600px] py-16 md:py-20">
                        {/* Left - Text */}
                        <div>
                            <p className="hero-line-reveal text-amber-400 text-sm font-semibold tracking-[0.2em] uppercase mb-5">
                                Sàn đấu giá đồ cổ trực tuyến
                            </p>
                            
                            <h1 className="text-4xl md:text-5xl lg:text-[3.5rem] font-bold text-white leading-[1.15] mb-6">
                                <span className="hero-line-reveal delay-1">Khám phá vẻ đẹp</span><br />
                                <span className="hero-line-reveal delay-2 text-amber-400">vượt thời gian</span>
                            </h1>
                            
                            <p className="hero-copy-reveal text-slate-300 text-lg leading-relaxed mb-8 max-w-lg">
                                Sưu tầm những vật phẩm cổ quý hiếm từ khắp nơi. Đấu giá minh bạch, thanh toán an toàn qua Escrow.
                            </p>
                            
                            <div className="hero-copy-reveal flex flex-wrap gap-4 mb-12">
                                <a 
                                    href="#auction-floor" 
                                    className="magnetic-button inline-flex items-center gap-2.5 bg-amber-500 hover:bg-amber-400 text-slate-900 font-semibold px-7 py-3.5 rounded-lg transition-all duration-300 text-sm shadow-lg hover:shadow-xl hover:scale-105"
                                    onClick={ripple}
                                    {...magneticProps}
                                >
                                    <Gavel className="w-4.5 h-4.5" /> Xem đấu giá
                                </a>
                                <Link 
                                    to="/auctions/create" 
                                    className="magnetic-button inline-flex items-center gap-2.5 border border-white/20 text-white hover:bg-white/10 px-7 py-3.5 rounded-lg font-semibold transition-all duration-300 text-sm hover:scale-105"
                                    onClick={ripple}
                                    {...magneticProps}
                                >
                                    Đăng bán <ArrowRight className="w-4 h-4" />
                                </Link>
                            </div>

                            {/* Trust signals */}
                            <div className="hero-copy-reveal flex flex-wrap gap-6 text-sm text-slate-400">
                                <span className="flex items-center gap-2">
                                    <ShieldCheck className="w-4 h-4 text-emerald-400" /> Escrow bảo mật
                                </span>
                                <span className="flex items-center gap-2">
                                    <Truck className="w-4 h-4 text-blue-400" /> Vận chuyển cẩn thận
                                </span>
                                <span className="flex items-center gap-2">
                                    <Lock className="w-4 h-4 text-amber-400" /> eKYC xác thực
                                </span>
                            </div>
                        </div>

                        {/* Right - Featured item card */}
                        <div className="hidden lg:block">
                            <div className="relative ml-auto max-w-sm">
                                {/* Main featured card */}
                                <div className="bg-white/5 backdrop-blur-sm border border-white/10 rounded-2xl overflow-hidden">
                                    <div className="aspect-[4/5] overflow-hidden relative">
                                        {heroShowcaseItems.map((item, i) => (
                                            <img 
                                                key={item.id}
                                                src={item.image} 
                                                alt={item.name}
                                                className={`absolute inset-0 w-full h-full object-cover transition-opacity duration-700 ${i === heroIdx ? 'cinematic-hero-image' : ''}`}
                                                style={{ opacity: i === heroIdx ? 1 : 0 }}
                                            />
                                        ))}
                                        <div className="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/80 to-transparent p-6">
                                            <p className="text-white font-semibold text-lg leading-snug mb-1">{currentItem.name}</p>
                                            <p className="text-amber-300 text-sm">Ước tính: {currentItem.estimate}</p>
                                        </div>
                                    </div>
                                </div>
                                
                                {/* Carousel indicators */}
                                <div className="flex justify-center gap-2 mt-4">
                                    {heroShowcaseItems.map((_, i) => (
                                        <button 
                                            key={i}
                                            onClick={() => setHeroIdx(i)}
                                            className={`h-1 rounded-full transition-all duration-300 ${i === heroIdx ? 'w-8 bg-amber-400' : 'w-4 bg-white/20 hover:bg-white/40'}`}
                                        />
                                    ))}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            {/* ══════════ FEATURES BAR ══════════ */}
            <section className="border-y border-[#9A6A2F]/15 bg-[#F8F1E6]">
                <div className="max-w-7xl mx-auto px-4 md:px-6 py-10">
                    <div className="grid grid-cols-1 sm:grid-cols-3 gap-6 md:gap-10">
                        <div className="flex items-start gap-4 border-l border-[#9A6A2F]/35 pl-5">
                            <div className="w-11 h-11 border border-[#9A6A2F]/40 bg-[#FFF8ED] flex items-center justify-center shrink-0">
                                <Award className="w-5 h-5 text-[#9A6A2F]" />
                            </div>
                            <div>
                                <h3 className="font-serif text-[#2F2418] text-lg mb-1">Đồ cổ chính hãng</h3>
                                <p className="text-[#2F2418]/55 text-sm leading-relaxed">Mỗi vật phẩm được xác thực bởi chuyên gia trước khi lên sàn</p>
                            </div>
                        </div>
                        <div className="flex items-start gap-4 border-l border-[#9A6A2F]/35 pl-5">
                            <div className="w-11 h-11 border border-[#9A6A2F]/40 bg-[#FFF8ED] flex items-center justify-center shrink-0">
                                <ShieldCheck className="w-5 h-5 text-[#9A6A2F]" />
                            </div>
                            <div>
                                <h3 className="font-serif text-[#2F2418] text-lg mb-1">Thanh toán Escrow</h3>
                                <p className="text-[#2F2418]/55 text-sm leading-relaxed">Tiền được giữ an toàn cho đến khi bạn nhận hàng và xác nhận</p>
                            </div>
                        </div>
                        <div className="flex items-start gap-4 border-l border-[#9A6A2F]/35 pl-5">
                            <div className="w-11 h-11 border border-[#9A6A2F]/40 bg-[#FFF8ED] flex items-center justify-center shrink-0">
                                <Clock className="w-5 h-5 text-[#9A6A2F]" />
                            </div>
                            <div>
                                <h3 className="font-serif text-[#2F2418] text-lg mb-1">Đấu giá thời gian thực</h3>
                                <p className="text-[#2F2418]/55 text-sm leading-relaxed">Theo dõi và đặt giá trực tiếp, đếm ngược từng giây</p>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            {/* ══════════ AUCTION FLOOR ══════════ */}
            <section className="bg-[#F8F1E6]" id="auction-floor">
                {/* Section Header with background */}
                <div className="bg-[#EFE2CF] relative overflow-hidden border-b border-[#9A6A2F]/15">
                    <div className="absolute inset-0 opacity-10">
                        <div className="absolute inset-0" style={{backgroundImage: 'radial-gradient(circle at 20% 50%, rgba(154,106,47,0.28) 0%, transparent 50%), radial-gradient(circle at 80% 50%, rgba(245,230,200,0.10) 0%, transparent 48%)'}} />
                    </div>
                    <div className="max-w-7xl mx-auto px-4 md:px-6 py-10 md:py-14 relative">
                        <div className="flex flex-col md:flex-row md:items-end md:justify-between gap-4 mb-8">
                            <div>
                                <p className="text-[#9A6A2F] text-xs font-semibold tracking-[0.28em] uppercase mb-3">Explore Collections</p>
                                <h2 className="font-serif text-3xl md:text-5xl text-[#2F2418] mb-3">
                                    Sàn Đấu Giá
                                </h2>
                                <p className="text-[#2F2418]/55 text-sm max-w-md leading-relaxed">Khám phá và đấu giá những vật phẩm quý hiếm từ các nhà sưu tầm trên khắp Việt Nam</p>
                            </div>
                            
                            {/* Mobile Filter Toggle */}
                            <button 
                                onClick={() => setMobileFilterOpen(!mobileFilterOpen)}
                                className="md:hidden inline-flex items-center gap-2 text-sm font-medium text-[#2F2418] bg-white/[0.04] border border-[#9A6A2F]/25 px-4 py-2.5 relative self-start backdrop-blur-sm hover:border-[#9A6A2F]/60 transition-colors"
                            >
                                <SlidersHorizontal className="w-4 h-4" /> Bộ lọc
                                {activeFilterCount > 0 && (
                                    <span className="absolute -top-1.5 -right-1.5 w-5 h-5 bg-[#9A6A2F] text-[#F8F1E6] text-[10px] rounded-full flex items-center justify-center font-bold">{activeFilterCount}</span>
                                )}
                            </button>
                        </div>

                        {/* Status Filter Tabs - on dark bg */}
                        <div className="flex flex-wrap gap-1 border-b border-white/10 -mb-px">
                            {statusFilters.map(sf => (
                                <button
                                    key={sf.value}
                                    onClick={() => updateFilter('status', sf.value)}
                                    className={`px-5 py-3 text-sm font-medium border-b-2 transition-all ${
                                        currentStatus === sf.value
                                            ? 'border-[#9A6A2F] text-[#9A6A2F]'
                                            : 'border-transparent text-[#2F2418]/45 hover:text-[#2F2418] hover:border-[#2F2418]/25'
                                    }`}
                                >
                                    {sf.label}
                                </button>
                            ))}
                        </div>
                    </div>
                </div>

                <div className="max-w-7xl mx-auto px-4 md:px-6 py-12 md:py-16">

                <div className="flex flex-col lg:flex-row gap-8 lg:gap-10">
                    {/* ── Sidebar ── */}
                    <aside className="hidden lg:block w-56 shrink-0">
                        <div className="sticky top-24 space-y-6">
                            <div>
                                <h3 className="text-xs font-semibold text-[#9A6A2F] uppercase tracking-[0.24em] mb-4">
                                    Danh mục
                                </h3>
                                <ul className="space-y-0.5">
                                    <li>
                                        <button 
                                            onClick={() => updateFilter('category_id', '')} 
                                            className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-colors ${
                                                currentCategory === '' 
                                                    ? 'bg-[#9A6A2F] text-[#F8F1E6] font-semibold' 
                                                    : 'text-[#2F2418]/55 hover:bg-white/[0.04] hover:text-[#2F2418]'
                                            }`}
                                        >
                                            Tất cả danh mục
                                        </button>
                                    </li>
                                    {categories.map(c => (
                                        <li key={c.id}>
                                            <button 
                                                onClick={() => updateFilter('category_id', c.id)} 
                                                className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-colors flex items-center justify-between ${
                                                    String(currentCategory) === String(c.id) 
                                                        ? 'bg-[#9A6A2F] text-[#F8F1E6] font-semibold' 
                                                        : 'text-[#2F2418]/55 hover:bg-white/[0.04] hover:text-[#2F2418]'
                                                }`}
                                            >
                                                {c.name}
                                                <ChevronRight className="w-3.5 h-3.5 opacity-30" />
                                            </button>
                                        </li>
                                    ))}
                                </ul>
                            </div>
                        </div>
                    </aside>

                    {/* Mobile Filter Panel */}
                    {mobileFilterOpen && (
                        <div className="lg:hidden bg-[#FFF8ED] border border-[#9A6A2F]/20 p-5 shadow-[0_25px_80px_rgba(47,36,24,0.10)]">
                            <div className="flex items-center justify-between mb-4">
                                <h3 className="font-semibold text-[#2F2418] text-sm">Bộ lọc</h3>
                                <button onClick={() => setMobileFilterOpen(false)} className="p-1 hover:bg-white/[0.06]">
                                    <X className="w-5 h-5 text-[#2F2418]/55" />
                                </button>
                            </div>
                            <div>
                                <h4 className="text-xs font-semibold text-[#9A6A2F] uppercase tracking-[0.22em] mb-3">Danh mục</h4>
                                <div className="flex flex-wrap gap-2">
                                    <button 
                                        onClick={() => updateFilter('category_id', '')}
                                        className={`px-3 py-1.5 text-sm font-medium transition-colors ${currentCategory === '' ? 'bg-[#9A6A2F] text-[#F8F1E6]' : 'bg-white/[0.04] text-[#2F2418]/60 hover:bg-white/[0.08]'}`}
                                    >
                                        Tất cả
                                    </button>
                                    {categories.map(c => (
                                        <button 
                                            key={c.id}
                                            onClick={() => updateFilter('category_id', c.id)}
                                             className={`px-3 py-1.5 text-sm font-medium transition-colors ${String(currentCategory) === String(c.id) ? 'bg-[#9A6A2F] text-[#F8F1E6]' : 'bg-white/[0.04] text-[#2F2418]/60 hover:bg-white/[0.08]'}`}
                                        >
                                            {c.name}
                                        </button>
                                    ))}
                                </div>
                            </div>
                        </div>
                    )}

                    {/* ── Product Masonry Gallery ── */}
                    <div ref={sectionRef} className="flex-grow min-w-0">
                        {loading ? (
                            <Skeleton.List count={6} />
                        ) : auctions.length > 0 ? (
                            <div className="home-masonry-gallery">
                                {auctions.map((auc, i) => (
                                    <AuctionCard key={auc.id} auc={auc} index={i} variant={['tall', 'short', 'wide', 'medium', 'tall', 'short'][i % 6]} />
                                ))}
                            </div>
                        ) : (
                            <EmptyState 
                                icon={Search}
                                title="Chưa có sản phẩm nào"
                                description="Thử thay đổi bộ lọc hoặc quay lại sau nhé!"
                            />
                        )}
                    </div>
                </div>
                </div>
            </section>

            {/* ══════════ CTA ══════════ */}
            <section ref={ctaRef} className="bg-[#F8F1E6] px-4 md:px-6 pb-24">
                <div className={`relative max-w-7xl mx-auto overflow-hidden border border-[#9A6A2F]/20 transition-all duration-700 ${ctaVisible ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-6'}`}>
                    {/* Background image */}
                    <div className="absolute inset-0">
                        <img 
                            src="https://images.unsplash.com/photo-1513519245088-0e12902e35ca?w=1400&h=500&fit=crop&q=80" 
                            alt="" 
                            className="w-full h-full object-cover"
                        />
                        <div className="absolute inset-0 bg-[#EFE2CF]/86" />
                        <div className="absolute inset-0 bg-gradient-to-r from-[#EFE2CF] via-[#EFE2CF]/80 to-transparent" />
                    </div>
                    
                    <div className="relative px-8 md:px-20 py-16 md:py-24 text-left max-w-3xl">
                        <p className="text-[#9A6A2F] text-xs font-semibold tracking-[0.28em] uppercase mb-5">Consign With Us</p>
                        <h2 className="font-serif text-3xl md:text-5xl text-[#2F2418] mb-5">
                            Bạn có đồ cổ muốn bán?
                        </h2>
                        <p className="text-[#2F2418]/62 max-w-xl mb-9 text-sm md:text-base leading-8">
                            Tạo phiên đấu giá miễn phí và tiếp cận hàng nghìn nhà sưu tầm trên khắp Việt Nam.
                        </p>
                        <Link 
                            to="/auctions/create" 
                            className="magnetic-button inline-flex items-center gap-3 bg-[#9A6A2F] hover:bg-[#2F2418] text-[#F8F1E6] font-bold px-8 py-4 transition-all duration-300 text-xs uppercase tracking-[0.2em] hover:scale-105 hover:shadow-xl"
                            onClick={ripple}
                            {...magneticProps}
                        >
                            Tạo phiên đấu giá <ArrowRight className="w-4 h-4" />
                        </Link>
                    </div>
                </div>
            </section>
        </div>
    );
}
