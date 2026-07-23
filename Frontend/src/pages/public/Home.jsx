import { useEffect, useState, useRef } from 'react';
import gsap from 'gsap';
import { useSearchParams, Link } from 'react-router-dom';
import apiClient from '@/services/apiClient';
import AuctionCard from '@/components/Auction/AuctionCard';
import Skeleton from '@/components/Elements/Skeleton';
import EmptyState from '@/components/Elements/EmptyState';
import { useScrollAnimation } from '@/hooks/useScrollAnimation';
import { useRipple, useMagnetic } from '@/hooks/useRipple';
import { Search, SlidersHorizontal, Gavel, ShieldCheck, Truck, ArrowRight, X, ChevronRight, Clock, Award, Lock, Filter, Grid, List, Users } from 'lucide-react';

const statusFilters = [
    { value: '', label: 'Tất cả phiên' },
    { value: 'ACTIVE', label: 'Đang đấu giá' },
    { value: 'PENDING', label: 'Sắp mở sàn' },
    { value: 'CLOSED', label: 'Đã chốt phiên' },
];

const statusColors = {
    ACTIVE: 'bg-green-500/20 text-green-400 border-green-500/30',
    PENDING: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30',
    CLOSED: 'bg-gray-500/20 text-gray-400 border-gray-500/30',
};

const getStatusText = (status) => {
    const map = {
        ACTIVE: 'Đang đấu giá',
        PENDING: 'Sắp mở sàn',
        CLOSED: 'Đã chốt phiên',
        ENDED: 'Đã kết thúc',
        FAILED: 'Thất bại',
        CANCELLED: 'Đã hủy'
    };
    return map[status] || status;
};

export default function Home() {
    const [auctions, setAuctions] = useState([]);
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchParams, setSearchParams] = useSearchParams();
    const [mobileFilterOpen, setMobileFilterOpen] = useState(false);
    const [viewMode, setViewMode] = useState('grid'); // 'grid' | 'list'
    const heroRef = useRef(null);
    
    const currentStatus = searchParams.get('status') || '';
    const currentCategory = searchParams.get('category_id') || '';

    const [sectionRef, sectionVisible] = useScrollAnimation({ threshold: 0.05 });
    const [ctaRef, ctaVisible] = useScrollAnimation({ threshold: 0.2 });
    
    const ripple = useRipple('rgba(255, 255, 255, 0.5)');
    const magneticProps = useMagnetic(0.2);

    useEffect(() => {
        const media = gsap.matchMedia();
        media.add('(prefers-reduced-motion: no-preference)', () => {
            const ctx = gsap.context(() => {
                gsap.fromTo('.hero-text-stagger',
                { opacity: 0, y: 40 },
                    { opacity: 1, y: 0, duration: 1.05, ease: 'power3.out', stagger: 0.12, delay: 0.15 }
                );
                gsap.to('.hero-bg-zoom', {
                    scale: 1.06,
                    xPercent: -0.8,
                    duration: 18,
                    ease: 'sine.inOut',
                    yoyo: true,
                    repeat: -1
                });
            }, heroRef);
            return () => ctx.revert();
        });
        return () => media.revert();
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
                    auctionsList = data.content;
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

    const clearAllFilters = () => {
        setSearchParams({});
    };

    const activeFilterCount = [currentStatus, currentCategory].filter(Boolean).length;
    // Format currency
    const formatCurrency = value => `${Number(value || 0).toLocaleString('vi-VN')} đ`;

    // Get countdown
    const getCountdown = (auc) => {
        const targetDateStr = auc?.status === 'PENDING' ? (auc.startTime || auc.start_time) : (auc.endTime || auc.end_time);
        const targetDate = new Date(targetDateStr);
        const difference = targetDate - new Date();
        if (!auc || Number.isNaN(targetDate.getTime()) || difference <= 0) return '00:00:00';

        const days = Math.floor(difference / (1000 * 60 * 60 * 24));
        const hours = Math.floor((difference / (1000 * 60 * 60)) % 24);
        const minutes = Math.floor((difference / 1000 / 60) % 60);
        const seconds = Math.floor((difference / 1000) % 60);
        return `${days > 0 ? `${days}d ` : ''}${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    };

    // Auction Card Component
    const AuctionItem = ({ auc, variant = 'medium' }) => {
        const isActive = auc.status === 'ACTIVE';
        const isPending = auc.status === 'PENDING';
        
        const coverImageUrl = (auc.images && auc.images.length > 0) 
            ? (auc.images.find(img => img.isCover)?.url || auc.images[0].url)
            : (auc.coverImage || auc.cover_image);

        return (
            <Link to={`/auctions/${auc.id}`} className="group block">
                <div className={`bg-white rounded-lg overflow-hidden shadow-sm hover:shadow-xl transition-all duration-300 border border-[#9A6A2F]/10 hover:border-[#9A6A2F]/30 ${variant === 'tall' ? 'row-span-2' : ''} ${variant === 'wide' ? 'col-span-2' : ''}`}>
                    <div className="relative aspect-[4/3] overflow-hidden bg-[#F8F1E6]">
                        {coverImageUrl ? (
                            <img 
                                src={coverImageUrl} 
                                alt={auc.productName || auc.product_name} 
                                className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                                loading="lazy"
                            />
                        ) : (
                            <div className="w-full h-full flex items-center justify-center">
                                <Gavel className="h-12 w-12 text-[#9A6A2F]/30" />
                            </div>
                        )}
                        
                        {/* Status Badge */}
                        <div className={`absolute top-3 left-3 px-3 py-1 rounded-full text-xs font-medium border backdrop-blur-sm ${statusColors[auc.status] || statusColors.CLOSED}`}>
                            {getStatusText(auc.status)}
                        </div>

                        {/* Countdown for ACTIVE and PENDING */}
                        {(isActive || isPending) && (
                            <div className="absolute bottom-3 left-3 right-3 bg-black/70 backdrop-blur-sm rounded-lg px-3 py-2 flex items-center justify-between">
                                <span className="flex items-center gap-2 text-xs text-white/70">
                                    <Clock className="h-3.5 w-3.5 text-[#E8C58F]" />
                                    {isActive ? 'Còn lại' : 'Mở sau'}
                                </span>
                                <span className="font-mono text-sm font-bold text-[#E8C58F]">
                                    {getCountdown(auc)}
                                </span>
                            </div>
                        )}
                    </div>

                    <div className="p-4">
                        {(auc.categoryName || auc.category_name) && (
                            <p className="text-xs uppercase tracking-wider text-[#9A6A2F] mb-1">
                                {auc.categoryName || auc.category_name}
                            </p>
                        )}
                        <h3 className="text-sm font-medium text-[#2F2418] line-clamp-1 group-hover:text-[#9A6A2F] transition-colors">
                            {auc.productName || auc.product_name}
                        </h3>
                        
                        <div className="mt-3 flex items-end justify-between">
                            <div>
                                <p className="text-[10px] uppercase tracking-wider text-[#2F2418]/40">
                                    Giá hiện tại
                                </p>
                                <p className="text-lg font-semibold text-[#9A6A2F]">
                                    {formatCurrency(auc.currentPrice != null ? auc.currentPrice : auc.current_price)}
                                </p>
                            </div>
                            <div className="flex items-center gap-1.5 text-xs text-[#2F2418]/50">
                                <Users className="h-3.5 w-3.5" />
                                <span>{Number(auc.bidCount || auc.bid_count || 0)}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </Link>
        );
    };

    // List View Component
    const AuctionListItem = ({ auc }) => {
        const coverImageUrl = (auc.images && auc.images.length > 0) 
            ? (auc.images.find(img => img.isCover)?.url || auc.images[0].url)
            : (auc.coverImage || auc.cover_image);

        return (
        <Link to={`/auctions/${auc.id}`} className="group block">
            <div className="bg-white rounded-lg overflow-hidden shadow-sm hover:shadow-xl transition-all duration-300 border border-[#9A6A2F]/10 hover:border-[#9A6A2F]/30">
                <div className="flex flex-col sm:flex-row">
                    <div className="relative w-full sm:w-48 h-48 sm:h-auto flex-shrink-0 overflow-hidden bg-[#F8F1E6]">
                        {coverImageUrl ? (
                            <img 
                                src={coverImageUrl} 
                                alt={auc.productName || auc.product_name} 
                                className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                                loading="lazy"
                            />
                        ) : (
                            <div className="w-full h-full flex items-center justify-center">
                                <Gavel className="h-12 w-12 text-[#9A6A2F]/30" />
                            </div>
                        )}
                        
                        <div className={`absolute top-3 left-3 px-3 py-1 rounded-full text-xs font-medium border backdrop-blur-sm ${statusColors[auc.status] || statusColors.CLOSED}`}>
                            {getStatusText(auc.status)}
                        </div>
                    </div>

                    <div className="flex-1 p-4 flex flex-col justify-between">
                        <div>
                            {(auc.categoryName || auc.category_name) && (
                                <p className="text-xs uppercase tracking-wider text-[#9A6A2F] mb-1">
                                    {auc.categoryName || auc.category_name}
                                </p>
                            )}
                            <h3 className="text-base font-medium text-[#2F2418] group-hover:text-[#9A6A2F] transition-colors">
                                {auc.productName || auc.product_name}
                            </h3>
                            
                            {(auc.status === 'ACTIVE' || auc.status === 'PENDING') && (
                                <div className="mt-2 flex items-center gap-2 text-sm text-[#2F2418]/60">
                                    <Clock className="h-4 w-4 text-[#9A6A2F]" />
                                    <span>{auc.status === 'ACTIVE' ? 'Còn lại' : 'Mở sau'}:</span>
                                    <span className="font-mono font-bold text-[#9A6A2F]">
                                        {getCountdown(auc)}
                                    </span>
                                </div>
                            )}
                        </div>

                        <div className="mt-3 flex items-end justify-between">
                            <div>
                                <p className="text-[10px] uppercase tracking-wider text-[#2F2418]/40">
                                    Giá hiện tại
                                </p>
                                <p className="text-xl font-semibold text-[#9A6A2F]">
                                    {formatCurrency(auc.currentPrice != null ? auc.currentPrice : auc.current_price)}
                                </p>
                            </div>
                            <div className="flex items-center gap-4">
                                <div className="flex items-center gap-1.5 text-sm text-[#2F2418]/50">
                                    <Users className="h-4 w-4" />
                                    <span>{Number(auc.bidCount || auc.bid_count || 0)} lượt</span>
                                </div>
                                <ArrowRight className="h-5 w-5 text-[#9A6A2F]/50 group-hover:text-[#9A6A2F] transition-colors" />
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </Link>
        );
    };

    return (
        <div>
            {/* ══════════ HERO ══════════ */}
            <section ref={heroRef} className="relative flex min-h-[calc(100svh-4rem)] overflow-hidden bg-[#120d09] md:min-h-[calc(100svh-4.75rem)]">
                <img
                    src="/images/home/auction-hero-v2.jpg"
                    alt="Không gian trưng bày cổ vật Việt Nam của The Curator"
                    className="hero-bg-zoom absolute inset-0 h-full w-full origin-center object-cover object-[66%_50%]"
                    fetchPriority="high"
                />
                <div className="absolute inset-0 bg-[linear-gradient(90deg,rgba(12,8,5,0.97)_0%,rgba(12,8,5,0.88)_34%,rgba(12,8,5,0.36)_66%,rgba(12,8,5,0.08)_100%)]" />
                <div className="absolute inset-0 bg-[linear-gradient(180deg,rgba(8,5,3,0.08)_45%,rgba(8,5,3,0.72)_100%)]" />
                <div className="absolute inset-0 opacity-[0.12] [background-image:linear-gradient(rgba(255,248,237,0.16)_1px,transparent_1px),linear-gradient(90deg,rgba(255,248,237,0.12)_1px,transparent_1px)] [background-size:88px_88px] [mask-image:linear-gradient(to_right,black,transparent_72%)]" />

                <div className="relative z-10 mx-auto grid w-full max-w-[92rem] grid-cols-1 px-6 pb-28 pt-20 md:px-12 md:pb-32 md:pt-24 lg:grid-cols-12 lg:px-16 xl:px-20">
                    <div className="flex flex-col justify-center lg:col-span-7 xl:col-span-6">
                        <div className="hero-text-stagger mb-6 flex items-center gap-4">
                            <span className="h-px w-10 bg-[#c79a5b]" aria-hidden="true" />
                            <span className="text-[10px] font-bold uppercase tracking-[0.34em] text-[#e8c58f] sm:text-xs">
                                Nhà đấu giá tuyển chọn · Est. 2026
                            </span>
                        </div>

                        <h1 className="hero-text-stagger max-w-[760px] font-serif text-[clamp(3.6rem,8vw,7.4rem)] font-medium leading-[0.82] tracking-[-0.055em] text-[#fff8ed] drop-shadow-[0_12px_35px_rgba(0,0,0,0.28)]">
                            Di sản<br />
                            <span className="ml-[0.32em] italic text-[#d8b27c]">được tiếp nối.</span>
                        </h1>

                        <p className="hero-text-stagger mt-7 max-w-lg text-sm font-light leading-7 text-[#fff8ed]/70 sm:text-base sm:leading-8">
                            Những cổ vật mang dấu ấn thời gian, được thẩm định kỹ lưỡng và đưa đến đúng người trân trọng giá trị của chúng.
                        </p>

                        <div className="hero-text-stagger mt-9 flex flex-col gap-3 sm:flex-row sm:items-center">
                            <a href="#auction-floor" className="group inline-flex items-center justify-center gap-4 border border-[#c79a5b] bg-[#b17b3d] px-7 py-4 text-[11px] font-bold uppercase tracking-[0.2em] text-white shadow-[0_18px_45px_rgba(125,78,29,0.24)] transition-all duration-500 ease-out hover:-translate-y-1 hover:bg-[#c18b4b] hover:shadow-[0_24px_60px_rgba(125,78,29,0.34)]">
                                Khám phá phiên đấu
                                <ArrowRight className="h-4 w-4 transition-transform duration-500 group-hover:translate-x-1" aria-hidden="true" />
                            </a>
                            <Link to="/auctions/create" className="inline-flex items-center justify-center px-7 py-4 text-[11px] font-bold uppercase tracking-[0.2em] text-[#fff8ed]/80 transition-colors duration-300 hover:text-[#e8c58f]">
                                Ký gửi vật phẩm
                            </Link>
                        </div>
                    </div>

                    <aside className="hero-text-stagger mt-14 self-end lg:col-span-4 lg:col-start-9 lg:mt-0 lg:mb-4" aria-label="Cam kết của The Curator">
                        <div className="border border-white/20 bg-[#15100c]/55 p-5 shadow-[0_28px_80px_rgba(0,0,0,0.28)] backdrop-blur-xl sm:p-6">
                            <div className="flex items-center justify-between border-b border-white/15 pb-4">
                                <span className="text-[9px] font-bold uppercase tracking-[0.28em] text-[#e8c58f]">The Curator Standard</span>
                                <ShieldCheck className="h-5 w-5 text-[#e8c58f]" aria-hidden="true" />
                            </div>
                            <p className="mt-5 font-serif text-2xl font-medium leading-tight text-[#fff8ed] sm:text-3xl">
                                Giá trị thật,<br />niềm tin thật.
                            </p>
                            <p className="mt-3 text-xs leading-6 text-[#fff8ed]/60">
                                Xác thực chuyên gia · Thanh toán Escrow · Đấu giá thời gian thực
                            </p>
                        </div>
                    </aside>
                </div>

                <div className="absolute bottom-0 left-0 right-0 z-10 border-t border-white/15 bg-black/15 backdrop-blur-md">
                    <div className="mx-auto flex max-w-[92rem] items-center justify-between px-6 py-4 text-[9px] font-bold uppercase tracking-[0.24em] text-[#fff8ed]/50 md:px-12 lg:px-16 xl:px-20">
                        <span>Curated in Vietnam</span>
                        <a href="#auction-floor" className="group flex items-center gap-3 transition-colors duration-300 hover:text-[#e8c58f]">
                            Cuộn để khám phá
                            <span className="h-px w-10 bg-current transition-[width] duration-500 group-hover:w-16" aria-hidden="true" />
                        </a>
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
                {/* Section Header */}
                <div className="bg-[#EFE2CF] relative overflow-hidden border-b border-[#9A6A2F]/15">
                    <div className="absolute inset-0 opacity-10">
                        <div className="absolute inset-0" style={{backgroundImage: 'radial-gradient(circle at 20% 50%, rgba(154,106,47,0.28) 0%, transparent 50%), radial-gradient(circle at 80% 50%, rgba(245,230,200,0.10) 0%, transparent 48%)'}} />
                    </div>
                    <div className="max-w-7xl mx-auto px-4 md:px-6 py-10 md:py-14 relative">
                        <div className="flex flex-col md:flex-row md:items-end md:justify-between gap-4 mb-8">
                            <div>
                                <p className="text-[#9A6A2F] text-xs font-semibold tracking-[0.28em] uppercase mb-3">Phiên đấu giá</p>
                                <h2 className="font-serif text-3xl md:text-5xl text-[#2F2418] mb-3">
                                    Sàn Đấu Giá
                                </h2>
                                <p className="text-[#2F2418]/55 text-sm max-w-md leading-relaxed">Khám phá và đấu giá những vật phẩm quý hiếm từ các nhà sưu tầm trên khắp Việt Nam</p>
                            </div>
                            
                            <div className="flex items-center gap-3">
                                {/* View Toggle */}
                                <div className="hidden sm:flex rounded-lg border border-[#9A6A2F]/20 p-1 bg-white/50">
                                    <button
                                        onClick={() => setViewMode('grid')}
                                        className={`p-1.5 rounded transition-colors ${
                                            viewMode === 'grid' 
                                                ? 'bg-[#9A6A2F] text-white' 
                                                : 'text-[#2F2418]/40 hover:text-[#2F2418]'
                                        }`}
                                    >
                                        <Grid className="h-4 w-4" />
                                    </button>
                                    <button
                                        onClick={() => setViewMode('list')}
                                        className={`p-1.5 rounded transition-colors ${
                                            viewMode === 'list' 
                                                ? 'bg-[#9A6A2F] text-white' 
                                                : 'text-[#2F2418]/40 hover:text-[#2F2418]'
                                        }`}
                                    >
                                        <List className="h-4 w-4" />
                                    </button>
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
                        </div>

                        {/* Status Filter Tabs */}
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
                        {/* ── Sidebar Filters ── */}
                        <aside className="hidden lg:block w-64 shrink-0">
                            <div className="sticky top-24 space-y-6">
                                {/* Active filters summary */}
                                {activeFilterCount > 0 && (
                                    <div className="bg-[#EFE2CF] rounded-lg p-4">
                                        <div className="flex items-center justify-between mb-2">
                                            <span className="text-xs font-semibold text-[#2F2418] uppercase tracking-wider">
                                                Đang lọc ({activeFilterCount})
                                            </span>
                                            <button 
                                                onClick={clearAllFilters}
                                                className="text-xs text-[#9A6A2F] hover:text-[#2F2418] transition-colors"
                                            >
                                                Xóa tất cả
                                            </button>
                                        </div>
                                        <div className="flex flex-wrap gap-2">
                                            {currentStatus && (
                                                <span className="px-2 py-1 text-xs bg-[#9A6A2F]/20 text-[#9A6A2F] rounded">
                                                    {statusFilters.find(s => s.value === currentStatus)?.label}
                                                </span>
                                            )}
                                            {currentCategory && (
                                                <span className="px-2 py-1 text-xs bg-[#9A6A2F]/20 text-[#9A6A2F] rounded">
                                                    {categories.find(c => String(c.id) === String(currentCategory))?.name}
                                                </span>
                                            )}
                                        </div>
                                    </div>
                                )}

                                {/* Categories */}
                                <div>
                                    <h3 className="text-xs font-semibold text-[#9A6A2F] uppercase tracking-[0.24em] mb-4 flex items-center gap-2">
                                        <Filter className="w-4 h-4" /> Danh mục
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

                                {/* Results count */}
                                <div className="pt-4 border-t border-[#9A6A2F]/10">
                                    <p className="text-sm text-[#2F2418]/50">
                                        <span className="text-[#9A6A2F] font-semibold">{auctions.length}</span> phiên đấu giá
                                    </p>
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
                                            className={`px-3 py-1.5 text-sm font-medium transition-colors ${
                                                currentCategory === '' 
                                                    ? 'bg-[#9A6A2F] text-[#F8F1E6]' 
                                                    : 'bg-white/[0.04] text-[#2F2418]/60 hover:bg-white/[0.08]'
                                            }`}
                                        >
                                            Tất cả
                                        </button>
                                        {categories.map(c => (
                                            <button 
                                                key={c.id}
                                                onClick={() => updateFilter('category_id', c.id)}
                                                className={`px-3 py-1.5 text-sm font-medium transition-colors ${
                                                    String(currentCategory) === String(c.id) 
                                                        ? 'bg-[#9A6A2F] text-[#F8F1E6]' 
                                                        : 'bg-white/[0.04] text-[#2F2418]/60 hover:bg-white/[0.08]'
                                                }`}
                                            >
                                                {c.name}
                                            </button>
                                        ))}
                                    </div>
                                </div>
                            </div>
                        )}

                        {/* ── Auction Grid ── */}
                        <div ref={sectionRef} className="flex-grow min-w-0">
                            {loading ? (
                                <Skeleton.List count={6} />
                            ) : auctions.length > 0 ? (
                                viewMode === 'grid' ? (
                                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                                        {auctions.map((auc) => (
                                            <AuctionItem key={auc.id} auc={auc} />
                                        ))}
                                    </div>
                                ) : (
                                    <div className="space-y-4">
                                        {auctions.map((auc) => (
                                            <AuctionListItem key={auc.id} auc={auc} />
                                        ))}
                                    </div>
                                )
                            ) : (
                                <EmptyState 
                                    icon={Search}
                                    title="Không tìm thấy phiên đấu giá"
                                    description="Thử thay đổi bộ lọc hoặc quay lại sau nhé!"
                                />
                            )}
                        </div>
                    </div>
                </div>
            </section>

            <section ref={ctaRef} className="bg-[#F8F1E6] px-4 md:px-6 pb-24">
                <div className={`relative max-w-7xl mx-auto overflow-hidden border border-[#9A6A2F]/20 transition-all duration-700 ${ctaVisible ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-6'}`}>
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
                        <p className="text-[#9A6A2F] text-xs font-semibold tracking-[0.28em] uppercase mb-5">Hợp tác cùng chúng tôi</p>
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
