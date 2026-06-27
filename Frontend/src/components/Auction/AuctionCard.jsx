import { useEffect, useState, useRef } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, TrendingUp, Timer, Eye, Users } from 'lucide-react';

const statusConfig = {
    ACTIVE: { label: 'LIVE', className: 'bg-emerald-500 text-white', dot: true },
    PENDING: { label: 'Sắp diễn ra', className: 'bg-amber-500 text-white', dot: false },
    CLOSED: { label: 'Đã kết thúc', className: 'bg-slate-600 text-white', dot: false },
    ENDED: { label: 'Đã kết thúc', className: 'bg-slate-600 text-white', dot: false },
    FAILED: { label: 'Thất bại', className: 'bg-red-500 text-white', dot: false },
    CANCELLED: { label: 'Đã hủy', className: 'bg-slate-400 text-white', dot: false },
};

const fallbackImages = [
    'https://images.unsplash.com/photo-1565193566173-7a0ee3dbe261?w=900&h=1200&fit=crop&q=80',
    'https://images.unsplash.com/photo-1509048191080-d2984bad6ae5?w=900&h=1200&fit=crop&q=80',
    'https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=900&h=1200&fit=crop&q=80',
    'https://images.unsplash.com/photo-1513519245088-0e12902e35ca?w=900&h=1200&fit=crop&q=80',
];

const getAuctionImage = (auc, index) => {
    const candidates = [
        auc.cover_image,
        auc.image_url,
        auc.image,
        auc.thumbnail,
        auc.product_image,
        auc.product?.image_url,
        auc.product?.cover_image,
        auc.images?.[0]?.url,
        auc.images?.[0]?.image_url,
        auc.images?.[0],
        auc.product?.images?.[0]?.url,
        auc.product?.images?.[0]?.image_url,
        auc.product?.images?.[0],
    ];

    const image = candidates.find(value => typeof value === 'string' && value.trim());
    return image || fallbackImages[index % fallbackImages.length];
};

const AuctionCard = ({ auc, index = 0, variant = 'medium' }) => {
    const [timeLeft, setTimeLeft] = useState('');
    const [isVisible, setIsVisible] = useState(false);
    const cardRef = useRef(null);

    // Intersection Observer for scroll animation
    useEffect(() => {
        const observer = new IntersectionObserver(
            ([entry]) => {
                if (entry.isIntersecting) {
                    // Stagger delay based on index
                    setTimeout(() => setIsVisible(true), index * 100);
                    observer.unobserve(entry.target);
                }
            },
            { threshold: 0.1, rootMargin: '50px' }
        );
        if (cardRef.current) observer.observe(cardRef.current);
        return () => observer.disconnect();
    }, [index]);

    useEffect(() => {
        const calculateTimeLeft = () => {
            const startTime = auc.startTime || auc.start_time;
            const endTime = auc.endTime || auc.end_time;
            const targetDate = auc.status === 'PENDING' ? new Date(startTime) : new Date(endTime);
            const difference = targetDate - new Date();

            if (difference <= 0) return '00:00:00';

            const days = Math.floor(difference / (1000 * 60 * 60 * 24));
            const hours = Math.floor((difference / (1000 * 60 * 60)) % 24);
            const minutes = Math.floor((difference / 1000 / 60) % 60);
            const seconds = Math.floor((difference / 1000) % 60);

            let res = '';
            if (days > 0) res += `${days}d `;
            res += `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
            return res;
        };

        if (auc.status === 'PENDING' || auc.status === 'ACTIVE') {
            setTimeLeft(calculateTimeLeft());
            const timer = setInterval(() => setTimeLeft(calculateTimeLeft()), 1000);
            return () => clearInterval(timer);
        }
    }, [auc.start_time, auc.startTime, auc.end_time, auc.endTime, auc.status]);

    const status = statusConfig[auc.status] || statusConfig.CLOSED;
    const isLive = auc.status === 'ACTIVE';
    const bidCount = auc.bidCount || auc.bid_count || 0;
    const [imageSrc, setImageSrc] = useState(() => getAuctionImage(auc, index));

    useEffect(() => {
        setImageSrc(getAuctionImage(auc, index));
    }, [auc, index]);

    return (
        <div
            ref={cardRef}
            className={`auction-card-shell auction-card-${variant} rounded-[24px] bg-transparent transition-all duration-700 ease-out ${
                isVisible 
                    ? 'opacity-100 translate-y-0' 
                    : 'opacity-0 translate-y-10'
            }`}
        >
            <Link 
                to={`/auctions/${auc.id}`} 
                className="group auction-gallery-card isolate block overflow-hidden rounded-[24px] border border-[#172536]/18 bg-[#F7F1E5] shadow-[0_18px_45px_rgba(12,22,34,0.14)] transition-all duration-300 hover:-translate-y-1 hover:border-[#172536]/28 hover:shadow-[0_24px_65px_rgba(12,22,34,0.20)]"
            >
                {/* Image Container */}
                <div className="auction-gallery-image museum-light-sweep bg-[#213448] overflow-hidden relative rounded-t-[24px]">
                    <img 
                        src={imageSrc} 
                        className="object-cover w-full h-full group-hover:scale-[1.08] transition-transform duration-700 ease-out" 
                        alt={auc.product_name}
                        loading="lazy"
                        referrerPolicy="no-referrer"
                        onError={() => setImageSrc(fallbackImages[index % fallbackImages.length])}
                    />
                    
                    {/* Dark gradient overlay on hover */}
                    <div className="absolute inset-0 bg-gradient-to-t from-black/40 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500" />

                    {/* Status Badge */}
                    <div className="absolute top-3 left-3">
                        <div className={`flex items-center gap-1.5 px-3 py-1.5 text-[11px] font-bold uppercase tracking-wider rounded-full shadow-lg backdrop-blur-sm ${status.className}`}>
                            {status.dot && (
                                <span className="relative flex h-2 w-2">
                                    <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-white opacity-75"></span>
                                    <span className="relative inline-flex rounded-full h-2 w-2 bg-white"></span>
                                </span>
                            )}
                            {status.label}
                        </div>
                    </div>

                    {/* Countdown Timer */}
                    {(auc.status === 'PENDING' || auc.status === 'ACTIVE') && timeLeft && (
                        <div className="absolute bottom-3 left-3 right-3">
                            <div className={`flex items-center justify-between py-2 px-3 rounded-lg backdrop-blur-md ${isLive ? 'bg-black/60 live-timer-pulse' : 'bg-black/50'}`}>
                                <div className="flex items-center gap-1.5">
                                    <Timer className="w-3.5 h-3.5 text-amber-400" />
                                    <span className="text-[11px] text-slate-300 font-medium">
                                        {auc.status === 'PENDING' ? 'Bắt đầu sau' : 'Kết thúc sau'}
                                    </span>
                                </div>
                                <span className="text-sm font-mono font-bold text-white tracking-wider">{timeLeft}</span>
                            </div>
                        </div>
                    )}

                    {/* Quick view button on hover */}
                    <div className="absolute top-3 right-3 opacity-0 group-hover:opacity-100 transition-all duration-300 translate-y-1 group-hover:translate-y-0">
                        <div className="bg-[#F7F1E5]/90 backdrop-blur-sm rounded-full p-2 shadow-lg">
                            <Eye className="w-4 h-4 text-slate-700" />
                        </div>
                    </div>
                </div>

                {/* Content */}
                <div className="auction-gallery-content p-5 rounded-b-[24px] bg-gradient-to-b from-[#F7F1E5] to-[#E8DDCB]">
                    {/* Category tag if available */}
                    {(auc.categoryName || auc.category_name) && (
                        <p className="text-[11px] font-semibold text-amber-600 uppercase tracking-wider mb-2">{auc.categoryName || auc.category_name}</p>
                    )}

                    <h3 className="font-semibold text-slate-900 leading-snug line-clamp-2 min-h-[2.75rem] group-hover:text-amber-700 transition-colors duration-300 text-[15px]">
                        {auc.productName || auc.product_name}
                    </h3>
                    
                    {/* Price & Stats */}
                    <div className="mt-4 pt-4 border-t border-[#213448]/12">
                        <div className="flex items-end justify-between">
                            <div>
                                <p className="text-[11px] text-slate-400 font-medium uppercase tracking-wider mb-1 flex items-center gap-1">
                                    {isLive && <TrendingUp className="w-3 h-3 text-emerald-500" />}
                                    {auc.status === 'PENDING' ? 'Giá khởi điểm' : 'Giá hiện tại'}
                                </p>
                                <p className="text-xl font-bold text-slate-900 tracking-tight">
                                    {((auc.currentPrice != null ? auc.currentPrice : auc.current_price) || 0).toLocaleString('vi-VN')}
                                    <span className="text-sm font-medium text-slate-400 ml-0.5">đ</span>
                                </p>
                            </div>
                            
                            {/* Bid count */}
                            {bidCount > 0 && (
                                <div className="flex items-center gap-1 text-[#31465C] bg-[#D8CCB8]/70 px-2.5 py-1 rounded-full">
                                    <Users className="w-3 h-3" />
                                    <span className="text-xs font-semibold">{bidCount} lượt</span>
                                </div>
                            )}
                        </div>

                        {/* CTA on hover */}
                        <div className="mt-3 overflow-hidden">
                            <div className="flex items-center gap-1.5 text-sm font-semibold text-amber-600 transform translate-y-8 group-hover:translate-y-0 opacity-0 group-hover:opacity-100 transition-all duration-300 ease-out">
                                {isLive ? 'Đặt giá ngay' : 'Xem chi tiết'} 
                                <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition-transform duration-300" />
                            </div>
                        </div>
                    </div>
                </div>
            </Link>
        </div>
    );
};

export default AuctionCard;
