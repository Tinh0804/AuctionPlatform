import { useEffect, useMemo, useRef, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { Activity, ArrowUpRight, ChevronRight, Clock3, Filter, Gavel, Search, SlidersHorizontal, Users, X } from 'lucide-react';
import client from '../api/client';
import EmptyState from '../components/EmptyState';

const statusOptions = [
    { value: '', label: 'Tat ca phien' },
    { value: 'ACTIVE', label: 'Dang dau gia' },
    { value: 'PENDING', label: 'Sap mo san' },
    { value: 'CLOSED', label: 'Da chot phien' },
];

const formatCurrency = value => `${Number(value || 0).toLocaleString('vi-VN')} d`;
const statusText = { ACTIVE: 'Dang dau gia', PENDING: 'Cho mo san', CLOSED: 'Da chot phien', ENDED: 'Da ket thuc', FAILED: 'That bai', CANCELLED: 'Da huy' };

const getCountdown = auc => {
    const targetDate = auc?.status === 'PENDING' ? new Date(auc.start_time) : new Date(auc?.end_time);
    const difference = targetDate - new Date();
    if (!auc || Number.isNaN(targetDate.getTime()) || difference <= 0) return '00:00:00';

    const days = Math.floor(difference / (1000 * 60 * 60 * 24));
    const hours = Math.floor((difference / (1000 * 60 * 60)) % 24);
    const minutes = Math.floor((difference / 1000 / 60) % 60);
    const seconds = Math.floor((difference / 1000) % 60);
    return `${days > 0 ? `${days}d ` : ''}${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
};

function AuctionList() {
    const [auctions, setAuctions] = useState([]);
    const [categories, setCategories] = useState([]);
    const [searchParams, setSearchParams] = useSearchParams();
    const [showMobileFilter, setShowMobileFilter] = useState(false);
    const [heroCountdown, setHeroCountdown] = useState('');
    const pageRef = useRef(null);

    const currentStatus = searchParams.get('status') || '';
    const currentCategory = searchParams.get('category_id') || '';

    useEffect(() => {
        client.get('/auctions/categories').then(res => setCategories(res.data)).catch(console.error);
    }, []);

    useEffect(() => {
        const params = new URLSearchParams();
        if (currentStatus) params.append('status', currentStatus);
        if (currentCategory) params.append('category_id', currentCategory);

        const qs = params.toString();
        client.get(`/auctions${qs ? '?' + qs : ''}`).then(res => setAuctions(res.data)).catch(console.error);
    }, [currentStatus, currentCategory]);

    const activeLots = useMemo(() => auctions.filter(auc => auc.status === 'ACTIVE'), [auctions]);
    const heroLot = useMemo(() => activeLots[0] || auctions[0], [activeLots, auctions]);
    const featuredLots = useMemo(() => activeLots.filter(auc => auc.id !== heroLot?.id).slice(0, 6), [activeLots, heroLot]);

    useEffect(() => {
        if (!heroLot) return undefined;
        setHeroCountdown(getCountdown(heroLot));
        const timer = setInterval(() => setHeroCountdown(getCountdown(heroLot)), 1000);
        return () => clearInterval(timer);
    }, [heroLot]);

    useEffect(() => {
        const runFallbackReveal = () => {
            const nodes = pageRef.current?.querySelectorAll('[data-reveal]') || [];
            const observer = new IntersectionObserver(entries => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        entry.target.classList.add('is-visible');
                        observer.unobserve(entry.target);
                    }
                });
            }, { threshold: 0.12 });
            nodes.forEach(node => observer.observe(node));
            return () => observer.disconnect();
        };

        const cleanup = runFallbackReveal();
        const loadGsap = async () => {
            if (!window.gsap || !window.ScrollTrigger) return;
            const gsap = window.gsap;
            const ScrollTrigger = window.ScrollTrigger;
            gsap.registerPlugin(ScrollTrigger);
            cleanup?.();
            gsap.utils.toArray('[data-reveal]').forEach((node, index) => {
                gsap.fromTo(node, { autoAlpha: 0, y: 72 }, {
                    autoAlpha: 1,
                    y: 0,
                    duration: 1.15,
                    delay: (index % 4) * 0.06,
                    ease: 'power3.out',
                    scrollTrigger: { trigger: node, start: 'top 86%' },
                });
            });
            gsap.to('[data-parallax]', {
                yPercent: 12,
                ease: 'none',
                scrollTrigger: { trigger: pageRef.current, start: 'top top', end: 'bottom top', scrub: true },
            });
        };

        loadGsap();
        return () => cleanup?.();
    }, [auctions.length]);

    /*
        GSAP ScrollTrigger is intentionally loaded from index.html/CDN when available.
        This keeps Docker dev containers from crashing if node_modules are stale; the
        IntersectionObserver reveal above remains the safe fallback.
    */
    useEffect(() => {
        if (window.gsap && window.ScrollTrigger) return;
        const script = document.createElement('script');
        script.src = 'https://cdn.jsdelivr.net/npm/gsap@3.15.0/dist/gsap.min.js';
        script.async = true;
        script.onload = () => {
            const triggerScript = document.createElement('script');
            triggerScript.src = 'https://cdn.jsdelivr.net/npm/gsap@3.15.0/dist/ScrollTrigger.min.js';
            triggerScript.async = true;
            document.body.appendChild(triggerScript);
        };
        document.body.appendChild(script);
        return () => {};
    }, []);

    const updateFilter = (key, value) => {
        const newParams = new URLSearchParams(searchParams);
        value ? newParams.set(key, value) : newParams.delete(key);
        setSearchParams(newParams);
    };

    const activeFilterCount = (currentStatus ? 1 : 0) + (currentCategory ? 1 : 0);

    const categoryPalette = ['01', '02', '03', '04', '05', '06', '07', '08'];

    const FilterPanel = ({ className = '' }) => (
        <aside className={`auction-filter-panel ${className}`}>
            <div className="auction-filter-glow" />
            <div className="auction-filter-header">
                <div>
                    <div className="flex items-center gap-3 text-[#2F2418]">
                        <Filter className="h-4 w-4 text-[#9A6A2F]" />
                        <p className="text-xs uppercase tracking-[0.36em]">Bo Loc San</p>
                    </div>
                    <p className="mt-3 text-xs leading-5 text-[#2F2418]/50">Loc nhanh theo trang thai phien va nhom hang de vao dung san dang co gia.</p>
                </div>
                {activeFilterCount > 0 && <span className="auction-filter-count">{activeFilterCount}</span>}
            </div>
            <div className="mt-8 space-y-8">
                <div>
                    <p className="mb-4 text-xs uppercase tracking-[0.28em] text-[#9A6A2F]">Trang thai phien</p>
                    <div className="auction-status-grid">
                        {statusOptions.map(opt => (
                            <button key={opt.value} onClick={() => updateFilter('status', opt.value)} className={`auction-filter-chip ${currentStatus === opt.value ? 'is-active' : ''}`}>
                                <span>{opt.label}</span>
                                <ChevronRight className="h-3.5 w-3.5" />
                            </button>
                        ))}
                    </div>
                </div>
                <div>
                    <div className="mb-4 flex items-center justify-between gap-3">
                        <p className="text-xs uppercase tracking-[0.28em] text-[#9A6A2F]">Danh mục</p>
                        <Activity className="h-4 w-4 text-[#9A6A2F]/55" />
                    </div>
                    <div className="auction-category-list">
                        <button onClick={() => updateFilter('category_id', '')} className={`auction-category-chip ${currentCategory === '' ? 'is-active' : ''}`}>
                            <span className="auction-category-mark">All</span>
                            <span className="auction-category-name">Tất cả danh mục</span>
                            <ChevronRight className="h-4 w-4" />
                        </button>
                        {categories.map((category, index) => (
                            <button key={category.id} onClick={() => updateFilter('category_id', category.id)} className={`auction-category-chip tone-${categoryPalette[index % categoryPalette.length]} ${currentCategory === category.id ? 'is-active' : ''}`}>
                                <span className="auction-category-mark">{String(index + 1).padStart(2, '0')}</span>
                                <span className="auction-category-name">{category.name}</span>
                                <ChevronRight className="h-4 w-4" />
                            </button>
                        ))}
                    </div>
                </div>
            </div>
        </aside>
    );

    const LotImage = ({ auc, className = '' }) => auc?.cover_image ? (
        <img src={auc.cover_image} alt={auc.product_name} className={`h-full w-full object-cover ${className}`} loading="lazy" />
    ) : <div className="flex h-full w-full items-center justify-center bg-[#FFF8ED] text-[#9A6A2F]"><Gavel className="h-12 w-12" /></div>;

    const LotCard = ({ auc, index, featured = false }) => (
        <Link to={`/auctions/${auc.id}`} data-reveal className={`auction-lot-card group ${featured ? 'featured' : ''}`}>
            <div className="auction-lot-image">
                <LotImage auc={auc} className="transition duration-[1200ms] ease-out group-hover:scale-110" />
                <div className="absolute inset-0 bg-gradient-to-t from-[#2F2418]/36 via-transparent to-transparent opacity-70" />
                    <div className={`absolute left-5 top-5 rounded-full border px-3 py-1 text-[10px] uppercase tracking-[0.24em] shadow-sm backdrop-blur-md ${auc.status === 'ACTIVE' ? 'border-red-600/35 bg-red-700 text-white' : 'border-[#9A6A2F]/25 bg-[#FFF8ED]/82 text-[#2F2418]'}`}>
                    {statusText[auc.status] || auc.status}
                </div>
                {(auc.status === 'ACTIVE' || auc.status === 'PENDING') && <div className="absolute bottom-5 left-5 right-5 flex items-center justify-between border border-white/25 bg-[#111827]/76 px-3 py-2 text-[#FFF8ED] backdrop-blur-md"><span className="flex items-center gap-2 text-[10px] uppercase tracking-[0.18em] text-white/68"><Clock3 className="h-3.5 w-3.5 text-[#E8C58F]" />{auc.status === 'ACTIVE' ? 'Con lai' : 'Mo sau'}</span><span className="font-mono text-sm font-bold tabular-nums">{getCountdown(auc)}</span></div>}
            </div>
            <div className="auction-lot-caption">
                {auc.category_name && <p className="text-[10px] uppercase tracking-[0.28em] text-[#9A6A2F]">{auc.category_name}</p>}
                <h3>{auc.product_name}</h3>
                <div className="mt-4 flex items-end justify-between gap-4">
                    <div>
                        <p className="text-[11px] uppercase tracking-[0.22em] text-[#2F2418]/50">Gia hien tai</p>
                        <p className="font-serif text-xl text-[#2F2418]">{formatCurrency(auc.current_price)}</p>
                    </div>
                    <span className="flex items-center gap-2 text-xs text-[#2F2418]/60"><Users className="h-3.5 w-3.5" />{Number(auc.bid_count || 0)} luot</span>
                </div>
            </div>
        </Link>
    );

    return (
        <main ref={pageRef} className="auction-house-page">
            <div className="auction-market-ambience" aria-hidden="true">
                <span className="auction-orbit auction-orbit-one" />
                <span className="auction-orbit auction-orbit-two" />
                <span className="auction-bid-ticker auction-bid-ticker-one">+ bid</span>
                <span className="auction-bid-ticker auction-bid-ticker-two">LIVE</span>
                <span className="auction-bid-ticker auction-bid-ticker-three">closing</span>
            </div>
            {heroLot && (
                <section className="auction-hero">
                    <div className="absolute inset-0 auction-hero-image-layer" data-parallax><LotImage auc={heroLot} /></div>
                    <div className="auction-hero-scanline" />
                    <div className="absolute inset-0 bg-gradient-to-r from-[#0E0A07]/95 via-[#15110D]/72 to-[#15110D]/22" />
                    <div className="absolute inset-0 bg-gradient-to-t from-[#15110D] via-transparent to-[#2F2418]/12" />
                    <div className="relative z-10 mx-auto flex min-h-[86vh] max-w-7xl items-end px-5 pb-16 pt-28 md:px-8 md:pb-24">
                        <div className="max-w-3xl" data-reveal>
                            <p className="mb-5 text-xs uppercase tracking-[0.48em] text-[#9A6A2F]">San dau gia truc tiep / Lot dang nong</p>
                            <h1 className="font-serif text-5xl font-normal leading-[0.95] text-[#FFF8ED] md:text-7xl lg:text-8xl">{heroLot.product_name}</h1>
                            <div className="mt-8 grid max-w-2xl grid-cols-2 gap-4 md:grid-cols-3">
                                <div className="auction-hero-stat"><span>Gia hien tai</span><strong>{formatCurrency(heroLot.current_price)}</strong></div>
                                <div className="auction-hero-stat"><span>Luot ra gia</span><strong>{Number(heroLot.bid_count || 0)}</strong></div>
                                <div className="auction-hero-stat col-span-2 md:col-span-1"><span>Dem nguoc</span><strong>{heroCountdown}</strong></div>
                            </div>
                            <Link to={`/auctions/${heroLot.id}`} className="auction-hero-cta">Tham Gia Đấu Giá <ArrowUpRight className="h-5 w-5" /></Link>
                        </div>
                    </div>
                </section>
            )}

            <section className="mx-auto max-w-7xl px-5 py-20 md:px-8 md:py-28">
                <div data-reveal className="mb-12 flex flex-col justify-between gap-6 md:flex-row md:items-end">
                    <div><p className="auction-kicker">Phien dang mo</p><h2 className="auction-section-title">Cac phien dang dau gia</h2></div>
                    <p className="max-w-md text-sm leading-7 text-[#FFF8ED]/62">Danh sach nay chi hien cac phien ACTIVE thuc te tu he thong, khong chen so lieu ao.</p>
                </div>
                {featuredLots.length > 0 ? <div className="auction-lot-grid">{featuredLots.map((auc, index) => <LotCard key={auc.id} auc={auc} index={index} featured />)}</div> : <div data-reveal className="border border-[#E8C58F]/25 bg-[#15110D]/55 p-8 text-[#FFF8ED]/70">Hien chua co them phien ACTIVE nao ngoai phien noi bat.</div>}
            </section>

            <section className="mx-auto max-w-7xl px-5 pb-24 md:px-8 md:pb-32">
                <div className="mb-10 flex items-center justify-between gap-4" data-reveal>
                    <div><p className="auction-kicker">Bang phien</p><h2 className="auction-section-title">Tat ca phien dau gia</h2><p className="mt-3 text-sm text-[#FFF8ED]/58">Dang hien thi {auctions.length} phien theo bo loc hien tai</p></div>
                    <button onClick={() => setShowMobileFilter(true)} className="inline-flex items-center gap-2 border border-[#9A6A2F]/35 px-4 py-3 text-xs uppercase tracking-[0.22em] text-[#2F2418] md:hidden"><SlidersHorizontal className="h-4 w-4" />Filter{activeFilterCount > 0 ? ` (${activeFilterCount})` : ''}</button>
                </div>
                <div className="grid gap-10 lg:grid-cols-[260px_1fr]">
                    <FilterPanel className="hidden lg:block" />
                    {auctions.length > 0 ? <div className="auction-lot-grid">{auctions.map((auc, index) => <LotCard key={auc.id} auc={auc} index={index} />)}</div> : <EmptyState icon={Search} title="Không tìm thấy kết quả" description="Thử thay đổi bộ lọc hoặc quay lại sau" />}
                </div>
            </section>

            {showMobileFilter && <div className="fixed inset-0 z-50 lg:hidden"><div className="absolute inset-0 bg-[#2F2418]/28 backdrop-blur-sm" onClick={() => setShowMobileFilter(false)} /><div className="absolute bottom-0 left-0 right-0 max-h-[86vh] overflow-y-auto bg-[#F8F1E6] p-6 shadow-2xl"><div className="mb-6 flex items-center justify-between"><p className="text-sm uppercase tracking-[0.3em] text-[#2F2418]">Filters</p><button onClick={() => setShowMobileFilter(false)} className="grid h-10 w-10 place-items-center border border-[#9A6A2F]/30 text-[#2F2418]"><X className="h-4 w-4" /></button></div><FilterPanel /></div></div>}
        </main>
    );
}

export default AuctionList;