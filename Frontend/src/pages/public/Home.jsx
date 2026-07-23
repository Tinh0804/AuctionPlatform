import { useEffect, useState, useRef } from 'react';
import gsap from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';
import { useSearchParams, Link } from 'react-router-dom';
import apiClient from '@/services/apiClient';
import Skeleton from '@/components/Elements/Skeleton';
import { useScrollAnimation } from '@/hooks/useScrollAnimation';
import { useRipple, useMagnetic } from '@/hooks/useRipple';
import { Gavel, ShieldCheck, Truck, ArrowRight, Clock, Award, Users } from 'lucide-react';

gsap.registerPlugin(ScrollTrigger);

const statusFilters = [
    { value: '', label: 'Tất cả phiên' },
    { value: 'ACTIVE', label: 'Đang đấu giá' },
    { value: 'PENDING', label: 'Sắp mở sàn' },
    { value: 'CLOSED', label: 'Đã chốt phiên' },
];

const statusColors = {
    ACTIVE: 'bg-emerald-900/82 text-white border-white/20',
    PENDING: 'bg-[#9A6A2F]/90 text-white border-white/20',
    CLOSED: 'bg-[#1c1815]/76 text-white border-white/20',
};

const heroWords = ['nghệ thuật', 'thời trang', 'công nghệ', 'xe sưu tầm'];
const heroSlides = [
    ['/images/home/auction-hero-marketplace.webp', 'Không gian đấu giá đa dạng từ thời trang đến xe sưu tầm'],
    ['/images/home/auction-hero-tech-fashion.webp', 'Bộ sưu tập đấu giá công nghệ và thời trang'],
    ['/images/home/auction-hero-art-auto.webp', 'Bộ sưu tập đấu giá nghệ thuật, thiết kế và xe'],
];

const mockAuctions = [
    { id: 'preview-01', _mock: true, status: 'ACTIVE', productName: 'Bình nguyệt men lam thế kỷ XIX', categoryName: 'Gốm cổ', currentPrice: 128000000, bidCount: 24, endTime: '2027-08-18T14:30:00', coverImage: '/images/home/lot-porcelain.webp' },
    { id: 'preview-02', _mock: true, status: 'PENDING', productName: 'Tủ sơn son thếp vàng triều Nguyễn', categoryName: 'Nội thất cổ', currentPrice: 245000000, bidCount: 0, startTime: '2027-08-12T09:00:00', coverImage: '/images/home/lot-lacquer-cabinet.webp' },
    { id: 'preview-03', _mock: true, status: 'ACTIVE', productName: 'Tượng Quan Âm đồng cổ', categoryName: 'Điêu khắc', currentPrice: 186000000, bidCount: 31, endTime: '2027-08-16T20:15:00', coverImage: '/images/home/lot-bronze-statue.webp' },
    { id: 'preview-04', _mock: true, status: 'ACTIVE', productName: 'Trống đồng Đông Sơn tuyển chọn', categoryName: 'Khảo cổ', currentPrice: 320000000, bidCount: 18, endTime: '2027-08-20T16:00:00', coverImage: '/images/home/lot-bronze-drum.webp' },
    { id: 'preview-05', _mock: true, status: 'PENDING', productName: 'Sơn mài phong cảnh Bắc Bộ', categoryName: 'Mỹ thuật', currentPrice: 96000000, bidCount: 0, startTime: '2027-08-14T10:00:00', coverImage: '/images/home/lot-lacquer-art.webp' },
    { id: 'preview-06', _mock: true, status: 'CLOSED', productName: 'Ghế học giả gỗ trắc chạm hoa', categoryName: 'Nội thất cổ', currentPrice: 158000000, bidCount: 42, endTime: '2026-07-20T18:00:00', coverImage: '/images/home/lot-scholar-chair.webp' },
];

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
    const [typedHeroWord, setTypedHeroWord] = useState('');
    const [heroWordIndex, setHeroWordIndex] = useState(0);
    const [deletingHeroWord, setDeletingHeroWord] = useState(false);
    const [heroSlide, setHeroSlide] = useState(0);
    const [previousHeroSlide, setPreviousHeroSlide] = useState(0);
    const [searchParams, setSearchParams] = useSearchParams();
    const heroRef = useRef(null);
    const exhibitionRef = useRef(null);
    
    const currentStatus = searchParams.get('status') || '';
    const currentCategory = searchParams.get('category_id') || '';
    const displayAuctions = auctions.length ? auctions : mockAuctions.filter(auc => !currentStatus || auc.status === currentStatus);

    const [sectionRef] = useScrollAnimation({ threshold: 0.05 });
    const [ctaRef, ctaVisible] = useScrollAnimation({ threshold: 0.2 });
    
    const ripple = useRipple('rgba(255, 255, 255, 0.5)');
    const magneticProps = useMagnetic(0.2);

    useEffect(() => {
        if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
            setTypedHeroWord(heroWords[0]);
            return undefined;
        }

        const target = heroWords[heroWordIndex];
        const complete = !deletingHeroWord && typedHeroWord === target;
        const empty = deletingHeroWord && typedHeroWord === '';
        const timer = setTimeout(() => {
            if (complete) {
                setDeletingHeroWord(true);
            } else if (empty) {
                setDeletingHeroWord(false);
                setHeroWordIndex(index => (index + 1) % heroWords.length);
            } else {
                setTypedHeroWord(target.slice(0, typedHeroWord.length + (deletingHeroWord ? -1 : 1)));
            }
        }, complete ? 1400 : empty ? 250 : deletingHeroWord ? 45 : 85);

        return () => clearTimeout(timer);
    }, [typedHeroWord, heroWordIndex, deletingHeroWord]);

    useEffect(() => {
        if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return undefined;
        const timer = setInterval(() => {
            setHeroSlide(current => {
                setPreviousHeroSlide(current);
                return (current + 1) % heroSlides.length;
            });
        }, 5200);
        return () => clearInterval(timer);
    }, []);

    const selectHeroSlide = index => {
        if (index === heroSlide) return;
        setPreviousHeroSlide(heroSlide);
        setHeroSlide(index);
    };

    useEffect(() => {
        const media = gsap.matchMedia();
        media.add('(prefers-reduced-motion: no-preference)', () => {
            const ctx = gsap.context(() => {
                gsap.fromTo('.hero-text-stagger',
                { opacity: 0, y: 40 },
                    { opacity: 1, y: 0, duration: 1.05, ease: 'power3.out', stagger: 0.12, delay: 0.15 }
                );
            }, heroRef);
            return () => ctx.revert();
        });
        return () => media.revert();
    }, []);

    useEffect(() => {
        const media = gsap.matchMedia();
        media.add('(prefers-reduced-motion: no-preference)', () => {
            const ctx = gsap.context(() => {
                gsap.utils.toArray('.museum-plate').forEach((plate, index) => {
                    gsap.fromTo(plate,
                        { autoAlpha: 0, y: 70 },
                        {
                            autoAlpha: 1,
                            y: 0,
                            duration: 1,
                            delay: index % 2 * 0.08,
                            ease: 'power3.out',
                            scrollTrigger: { trigger: plate, start: 'top 86%', once: true }
                        }
                    );
                    const image = plate.querySelector('img');
                    if (image) {
                        gsap.fromTo(image,
                            { yPercent: -3 },
                            { yPercent: 3, ease: 'none', scrollTrigger: { trigger: plate, start: 'top bottom', end: 'bottom top', scrub: 1 } }
                        );
                    }
                });
            }, exhibitionRef);
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

    const AuctionListItem = ({ auc, index }) => {
        const coverImageUrl = (auc.images && auc.images.length > 0) 
            ? (auc.images.find(img => img.isCover)?.url || auc.images[0].url)
            : (auc.coverImage || auc.cover_image);
        const isOpen = auc.status === 'ACTIVE' || auc.status === 'PENDING';
        const priceLabel = auc.status === 'CLOSED' ? 'Giá chốt' : auc.status === 'PENDING' ? 'Giá khởi điểm' : 'Giá hiện tại';

        return (
        <Link to={auc._mock ? '/auctions' : `/auctions/${auc.id}`} className="group block">
            <article className="overflow-hidden rounded-2xl border border-[#1c1815]/10 bg-[#fffdf8] shadow-[0_12px_38px_rgba(28,24,21,0.055)] transition-all duration-400 hover:border-[#9A6A2F]/30 hover:shadow-[0_20px_50px_rgba(28,24,21,0.1)]">
                <div className="grid md:grid-cols-[210px_1fr_220px] md:items-stretch">
                    <div className="relative h-56 overflow-hidden bg-[#F8F1E6] md:h-full md:min-h-[178px]">
                        {coverImageUrl ? (
                            <img 
                                src={coverImageUrl} 
                                alt={auc.productName || auc.product_name} 
                                className="h-full w-full object-cover transition-transform duration-700 group-hover:scale-[1.035]"
                                loading="lazy"
                            />
                        ) : (
                            <div className="w-full h-full flex items-center justify-center">
                                <Gavel className="h-12 w-12 text-[#9A6A2F]/30" />
                            </div>
                        )}
                        <span className="absolute bottom-3 left-3 rounded-full border border-white/60 bg-white/78 px-2.5 py-1 text-[9px] font-bold uppercase tracking-[0.16em] text-[#1c1815] backdrop-blur-md">
                            Lot {String(index + 1).padStart(3, '0')}
                        </span>
                    </div>

                    <div className="flex min-w-0 flex-col justify-between p-5 md:p-6">
                        <div>
                            <div className="mb-2.5 flex items-center gap-3 text-[9px] font-bold uppercase tracking-[0.2em] text-[#9A6A2F]">
                                <span>{auc.categoryName || auc.category_name || 'Tuyển chọn'}</span>
                                <span className="h-px w-6 bg-[#9A6A2F]/35" />
                                <span className="text-[#746b62]">The Curator</span>
                            </div>
                            <h3 className="truncate text-xl font-semibold leading-tight text-[#1c1815] transition-colors group-hover:text-[#9A6A2F]">
                                {auc.productName || auc.product_name}
                            </h3>
                        </div>

                        <div className="mt-5 flex flex-wrap items-center gap-3 text-[11px] text-[#746b62]">
                            <span className={`rounded-full border px-2.5 py-1 text-[9px] font-bold uppercase tracking-[0.1em] ${statusColors[auc.status] || statusColors.CLOSED}`}>
                                {getStatusText(auc.status)}
                            </span>
                            {isOpen && (
                                <span className="flex items-center gap-1.5">
                                    <Clock className="h-3.5 w-3.5 text-[#9A6A2F]" />
                                    <span>{auc.status === 'ACTIVE' ? 'Còn' : 'Mở sau'}</span>
                                    <strong className="font-mono text-[#1c1815]">{getCountdown(auc)}</strong>
                                </span>
                            )}
                            <span className="flex items-center gap-1.5">
                                <Users className="h-3.5 w-3.5" />
                                {Number(auc.bidCount || auc.bid_count || 0)} lượt
                            </span>
                        </div>
                    </div>

                    <div className="flex items-center justify-between border-t border-[#1c1815]/10 bg-[#f7f1e8] p-5 md:flex-col md:items-stretch md:justify-center md:border-l md:border-t-0 md:p-6">
                        <div>
                            <p className="text-[9px] font-bold uppercase tracking-[0.2em] text-[#746b62]">{priceLabel}</p>
                            <p className="mt-1.5 text-xl font-bold text-[#1c1815]">
                                {formatCurrency(auc.currentPrice != null ? auc.currentPrice : auc.current_price)}
                            </p>
                        </div>
                        <span className="mt-0 inline-flex items-center gap-2 text-[9px] font-bold uppercase tracking-[0.16em] text-[#9A6A2F] md:mt-5">
                            Xem phiên
                            <ArrowRight className="h-3.5 w-3.5 transition-transform group-hover:translate-x-1" />
                        </span>
                    </div>
                </div>
            </article>
        </Link>
        );
    };

    return (
        <div className="home-page">
            {/* ══════════ HERO ══════════ */}
            <section ref={heroRef} className="auction-live-hero">
                <span className="auction-hero-index" aria-hidden="true">01</span>
                <div className="auction-live-shell">
                    <div className="auction-live-copy">
                        <div className="hero-text-stagger auction-live-kicker">
                            <span><Gavel /></span>
                            Live auction marketplace · 24/7
                        </div>

                        <h1 className="hero-text-stagger auction-live-title">
                            Đấu giá dành cho
                            <span className="auction-typewriter">{typedHeroWord}<i aria-hidden="true" /></span>
                        </h1>

                        <p className="hero-text-stagger auction-live-description">
                            Mỗi phiên đấu là một cơ hội thật. Theo dõi trực tiếp, đặt giá minh bạch và giao dịch an toàn trên mọi thiết bị.
                        </p>

                        <div className="hero-text-stagger auction-live-actions">
                            <a href="#auction-floor">
                                Vào sàn đấu <ArrowRight />
                            </a>
                            <Link to="/auctions/create">Tạo phiên mới</Link>
                        </div>
                    </div>

                    <div className="hero-text-stagger auction-live-stage">
                        <div className="auction-live-frame">
                            <img
                                src={heroSlides[previousHeroSlide][0]}
                                alt=""
                                className="auction-hero-slide-base"
                            />
                            <img
                                key={heroSlide}
                                src={heroSlides[heroSlide][0]}
                                alt={heroSlides[heroSlide][1]}
                                className="auction-hero-slide-reveal"
                                fetchPriority={heroSlide === 0 ? 'high' : 'auto'}
                            />
                            <div className="auction-image-meta">
                                <span><i /> Đang mở phiên</span>
                                <div className="auction-slide-dots" aria-label="Chuyển ảnh giới thiệu">
                                    {heroSlides.map(([, alt], index) => (
                                        <button
                                            key={alt}
                                            type="button"
                                            className={index === heroSlide ? 'is-active' : ''}
                                            onClick={() => selectHeroSlide(index)}
                                            aria-label={`Ảnh ${index + 1}`}
                                        />
                                    ))}
                                </div>
                                <strong>Đặt giá trực tiếp · Escrow bảo vệ</strong>
                            </div>
                        </div>
                        <p className="auction-image-caption">Nhiều danh mục.<br />Một trải nghiệm liền mạch.</p>
                    </div>
                </div>
            </section>
            <section className="home-standard-strip">
                <div className="mx-auto grid max-w-[90rem] grid-cols-1 sm:grid-cols-2 lg:grid-cols-4">
                    {[
                        [Award, '01', 'Tuyển chọn', 'Thẩm định bởi chuyên gia'],
                        [ShieldCheck, '02', 'Bảo chứng', 'Escrow cho mọi giao dịch'],
                        [Clock, '03', 'Trực tiếp', 'Đặt giá theo thời gian thực'],
                        [Truck, '04', 'An tâm', 'Vận chuyển có bảo hiểm'],
                    ].map(([Icon, number, title, copy]) => (
                        <div key={title} className="home-standard-item">
                            <span className="home-standard-icon">
                                <Icon className="h-[18px] w-[18px]" />
                            </span>
                            <div>
                                <span className="home-standard-number">{number}</span>
                                <h3>{title}</h3>
                                <p>{copy}</p>
                            </div>
                        </div>
                    ))}
                </div>
            </section>

            <section ref={exhibitionRef} className="museum-archive" aria-labelledby="exhibition-title">
                <div className="museum-archive-topline">
                    <span>Marketplace Highlights</span>
                    <span>Featured Selection No. 01</span>
                    <span>Vietnam · 2026</span>
                </div>

                <div className="museum-archive-layout">
                    <aside className="museum-archive-intro">
                        <p>Nhiều lĩnh vực · Một nền tảng</p>
                        <h2 id="exhibition-title">Mọi giá trị đều có<br /><em>một phiên đấu xứng đáng.</em></h2>
                        <div className="museum-rule" />
                        <p className="museum-archive-description">
                            Nghệ thuật, thời trang, công nghệ, xe, đồ sưu tầm hay tài sản giá trị đều được kiểm duyệt thông tin trước khi mở phiên.
                        </p>

                        <div className="museum-assurance">
                            <div><ShieldCheck /><span><strong>Thông tin minh bạch</strong>Hồ sơ sản phẩm được xác thực</span></div>
                            <div><Award /><span><strong>Kiểm duyệt rõ ràng</strong>Tiêu chuẩn phù hợp từng danh mục</span></div>
                        </div>

                        <div className="museum-seal">
                            <span>TC</span>
                            <p>Verified listings<br />The Curator</p>
                        </div>
                    </aside>

                    <div className="museum-album">
                        {[
                            ['lot-porcelain.webp', 'Bình nguyệt men lam', 'Gốm sứ · Thế kỷ XIX', 'TC–CER–019'],
                            ['lot-lacquer-cabinet.webp', 'Tủ sơn son thếp vàng', 'Nội thất cung đình', 'TC–FUR–041'],
                            ['lot-bronze-statue.webp', 'Tượng Quan Âm đồng cổ', 'Điêu khắc · Đồng patina', 'TC–SCU–026'],
                            ['lot-bronze-drum.webp', 'Trống đồng Đông Sơn', 'Khảo cổ · Văn hóa Việt', 'TC–ARC–008'],
                            ['lot-lacquer-art.webp', 'Sơn mài phong cảnh', 'Mỹ thuật Đông Dương', 'TC–ART–057'],
                            ['lot-scholar-chair.webp', 'Ghế học giả gỗ trắc', 'Thiết kế · Đầu thế kỷ XX', 'TC–FUR–063'],
                        ].map(([image, title, category, code], index) => (
                            <figure key={image} className={`museum-plate museum-plate-${index + 1}`}>
                                <div className="museum-plate-image">
                                    <img src={`/images/home/${image}`} alt={title} loading="lazy" />
                                    <span>{String(index + 1).padStart(2, '0')}</span>
                                </div>
                                <figcaption>
                                    <p>{category}</p>
                                    <h3>{title}</h3>
                                    <span>Listing No. {code}</span>
                                </figcaption>
                            </figure>
                        ))}

                        <div className="museum-provenance-card museum-plate">
                            <p>Featured selection</p>
                            <strong>06</strong>
                            <span>sản phẩm nổi bật<br />trong bộ sưu tập số 01</span>
                            <div>
                                <span>Documented</span>
                                <span>Inspected</span>
                                <span>Protected</span>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            <section className="home-catalogue relative overflow-hidden py-20 md:py-28" id="auction-floor">
                <div className="absolute -right-32 top-10 h-80 w-80 rounded-full bg-[#b96f52]/10 blur-3xl" aria-hidden="true" />
                <div className="absolute -left-20 bottom-20 h-72 w-72 rounded-full bg-[#647d76]/10 blur-3xl" aria-hidden="true" />

                <div className="relative mx-auto max-w-[90rem] px-6 md:px-8">
                    <div className="grid gap-10 border-b border-[#1c1815]/12 pb-12 lg:grid-cols-12 lg:items-end">
                        <div className="lg:col-span-8">
                            <p className="mb-5 text-[10px] font-bold uppercase tracking-[0.34em] text-[#9A6A2F]">Phiên đấu nổi bật · 2026</p>
                            <h2 className="home-editorial-title max-w-4xl text-[clamp(3.3rem,7vw,6.6rem)] font-medium leading-[0.82] tracking-[-0.045em] text-[#1c1815]">
                                Những phiên đấu<br />
                                <span className="italic text-[#9A6A2F]">đáng được chờ đợi.</span>
                            </h2>
                        </div>
                        <div className="lg:col-span-4 lg:pb-1">
                            <p className="max-w-sm text-sm leading-7 text-[#746b62]">
                                Từ nghệ thuật, thời trang, công nghệ đến phương tiện và đồ sưu tầm — cơ hội mới luôn được cập nhật mỗi ngày.
                            </p>
                            <div className="mt-6 flex items-center justify-between border-t border-[#1c1815]/12 pt-4">
                                <span className="text-[10px] font-bold uppercase tracking-[0.2em] text-[#746b62]">
                                    {displayAuctions.length} phiên
                                </span>
                                <span className="text-[10px] font-bold uppercase tracking-[0.2em] text-[#9A6A2F]">Đang cập nhật</span>
                            </div>
                        </div>
                    </div>

                    <div className="flex gap-2 overflow-x-auto py-8">
                        {statusFilters.map(sf => (
                            <button key={sf.value} onClick={() => updateFilter('status', sf.value)} className={`shrink-0 rounded-full px-5 py-2.5 text-xs font-bold transition-colors ${currentStatus === sf.value ? 'bg-[#1c1815] text-white' : 'border border-[#1c1815]/10 bg-white/45 text-[#746b62] hover:border-[#9A6A2F]/30 hover:text-[#9A6A2F]'}`}>
                                {sf.label}
                            </button>
                        ))}
                        {categories.slice(0, 4).map(c => (
                            <button key={c.id} onClick={() => updateFilter('category_id', c.id)} className={`shrink-0 rounded-full px-5 py-2.5 text-xs font-bold transition-colors ${String(currentCategory) === String(c.id) ? 'bg-[#9A6A2F] text-white' : 'border border-[#1c1815]/10 bg-white/45 text-[#746b62] hover:border-[#9A6A2F]/30 hover:text-[#9A6A2F]'}`}>
                                {c.name}
                            </button>
                        ))}
                    </div>

                    <div ref={sectionRef}>
                        {loading ? (
                            <Skeleton.List count={6} />
                        ) : (
                            <div className="space-y-5">
                                {displayAuctions.map((auc, index) => (
                                    <AuctionListItem key={auc.id} auc={auc} index={index} />
                                ))}
                            </div>
                        )}
                    </div>
                </div>
            </section>

            <section ref={ctaRef} className="bg-[#f2ece2] px-6 pb-24 md:px-8 md:pb-32">
                <div className={`relative mx-auto grid max-w-[90rem] overflow-hidden rounded-[2rem] bg-[#1c1815] text-white transition-all duration-700 lg:grid-cols-2 ${ctaVisible ? 'translate-y-0 opacity-100' : 'translate-y-6 opacity-0'}`}>
                    <div className="relative min-h-[360px] overflow-hidden lg:order-2">
                        <img src="/images/home/auction-hero-v2.jpg" alt="Không gian thẩm định và ký gửi cổ vật" className="absolute inset-0 h-full w-full object-cover" />
                        <div className="absolute inset-0 bg-gradient-to-r from-[#1c1815] via-transparent to-transparent lg:hidden" />
                    </div>
                    <div className="flex flex-col justify-center px-8 py-14 sm:px-14 lg:px-16 lg:py-20">
                        <p className="mb-6 text-[10px] font-bold uppercase tracking-[0.34em] text-[#d5b47a]">Private Consignment</p>
                        <h2 className="home-editorial-title text-4xl font-medium leading-[0.92] text-white sm:text-6xl">
                            Một hiện vật quý<br />xứng đáng một sân khấu đúng.
                        </h2>
                        <p className="mt-6 max-w-lg text-sm leading-7 text-white/58">
                            Đội ngũ giám tuyển đồng hành từ thẩm định, định giá đến khi phiên đấu khép lại.
                        </p>
                        <Link to="/auctions/create" className="magnetic-button mt-9 inline-flex w-fit items-center gap-4 rounded-full bg-[#faf7f1] px-7 py-4 text-[11px] font-bold uppercase tracking-[0.18em] text-[#1c1815] transition-all hover:bg-[#d5b47a]" onClick={ripple} {...magneticProps}>
                            Bắt đầu ký gửi <ArrowRight className="h-4 w-4" />
                        </Link>
                    </div>
                </div>
            </section>
        </div>
    );
}
