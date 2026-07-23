import { useEffect, useRef, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { ArrowUpRight, Clock3, Filter, Gavel, Search, SlidersHorizontal, Users, X, Grid, List } from 'lucide-react';
import apiClient from '@/services/apiClient';
import EmptyState from '@/components/Elements/EmptyState';

const statusOptions = [
    { value: '', label: 'Tất cả phiên' },
    { value: 'ACTIVE', label: 'Đang đấu giá' },
    { value: 'PENDING', label: 'Sắp mở sàn' },
    { value: 'CLOSED', label: 'Đã chốt phiên' },
];

const formatCurrency = value => `${Number(value || 0).toLocaleString('vi-VN')} đ`;
const statusText = { 
    ACTIVE: 'Đang đấu giá', 
    PENDING: 'Chờ mở sàn', 
    CLOSED: 'Đã chốt phiên', 
    ENDED: 'Đã kết thúc', 
    FAILED: 'Thất bại', 
    CANCELLED: 'Đã hủy' 
};

const statusColors = {
    ACTIVE: 'bg-emerald-900/82 text-white border-white/20',
    PENDING: 'bg-[#9A6A2F]/90 text-white border-white/20',
    CLOSED: 'bg-[#1c1815]/76 text-white border-white/20',
    ENDED: 'bg-[#1c1815]/76 text-white border-white/20',
    FAILED: 'bg-red-800/82 text-white border-white/20',
    CANCELLED: 'bg-[#746b62]/82 text-white border-white/20',
};

const getCountdown = auc => {
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

function AuctionList() {
    const [auctions, setAuctions] = useState([]);
    const [categories, setCategories] = useState([]);
    const [searchParams, setSearchParams] = useSearchParams();
    const [showMobileFilter, setShowMobileFilter] = useState(false);
    const [viewMode, setViewMode] = useState('grid'); // 'grid' | 'list'
    const pageRef = useRef(null);

    const currentStatus = searchParams.get('status') || '';
    const currentCategory = searchParams.get('category_id') || '';

    useEffect(() => {
        apiClient.get('/auctions/categories')
            .then(res => setCategories(res.data?.result || (Array.isArray(res.data) ? res.data : [])))
            .catch(console.error);
    }, []);

    useEffect(() => {
        const params = new URLSearchParams();
        if (currentStatus) params.append('status', currentStatus);
        if (currentCategory) params.append('category_id', currentCategory);

        const qs = params.toString();
        apiClient.get(`/auctions${qs ? '?' + qs : ''}`)
            .then(res => {
                const data = res.data;
                const auctionsList = Array.isArray(data) ? data : (data?.content || data?.result || []);
                setAuctions(auctionsList);
            })
            .catch(console.error);
    }, [currentStatus, currentCategory]);

    const updateFilter = (key, value) => {
        const newParams = new URLSearchParams(searchParams);
        value ? newParams.set(key, value) : newParams.delete(key);
        setSearchParams(newParams);
    };

    const activeFilterCount = (currentStatus ? 1 : 0) + (currentCategory ? 1 : 0);

    const clearAllFilters = () => {
        setSearchParams({});
    };

    // Filter Panel Component
    const FilterPanel = ({ className = '' }) => (
        <aside className={`${className}`}>
            <div className="sticky top-24 rounded-2xl border border-[#1c1815]/10 bg-[#fffdf8] p-6 shadow-[0_18px_50px_rgba(28,24,21,0.06)]">
                <div className="flex items-center justify-between mb-6">
                    <div className="flex items-center gap-3">
                        <Filter className="h-5 w-5 text-[#9A6A2F]" />
                        <h3 className="text-sm font-bold text-[#1c1815]">Bộ lọc</h3>
                    </div>
                    {activeFilterCount > 0 && (
                        <button 
                            onClick={clearAllFilters}
                            className="text-xs text-[#9A6A2F] hover:text-[#1c1815] transition-colors"
                        >
                            Xóa tất cả
                        </button>
                    )}
                </div>

                {/* Status Filter */}
                <div className="mb-6">
                    <label className="mb-3 block text-[10px] font-bold uppercase tracking-[0.2em] text-[#9A6A2F]">
                        Trạng thái
                    </label>
                    <div className="space-y-1.5">
                        {statusOptions.map(opt => (
                            <button
                                key={opt.value}
                                onClick={() => updateFilter('status', opt.value)}
                                className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-all ${
                                    currentStatus === opt.value
                                        ? 'bg-[#1c1815] text-white'
                                        : 'text-[#746b62] hover:bg-[#f2ece2] hover:text-[#1c1815]'
                                }`}
                            >
                                {opt.label}
                                {currentStatus === opt.value && (
                                    <span className="float-right text-[#d5b47a]">●</span>
                                )}
                            </button>
                        ))}
                    </div>
                </div>

                {/* Category Filter */}
                <div>
                    <label className="mb-3 block text-[10px] font-bold uppercase tracking-[0.2em] text-[#9A6A2F]">
                        Danh mục
                    </label>
                    <div className="space-y-1.5">
                        <button
                            onClick={() => updateFilter('category_id', '')}
                            className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-all ${
                                currentCategory === ''
                                    ? 'bg-[#1c1815] text-white'
                                    : 'text-[#746b62] hover:bg-[#f2ece2] hover:text-[#1c1815]'
                            }`}
                        >
                            Tất cả danh mục
                            {currentCategory === '' && (
                                <span className="float-right text-[#d5b47a]">●</span>
                            )}
                        </button>
                        {categories.map(category => (
                            <button
                                key={category.id}
                                onClick={() => updateFilter('category_id', category.id)}
                                className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-all ${
                                    String(currentCategory) === String(category.id)
                                        ? 'bg-[#1c1815] text-white'
                                        : 'text-[#746b62] hover:bg-[#f2ece2] hover:text-[#1c1815]'
                                }`}
                            >
                                {category.name}
                                {String(currentCategory) === String(category.id) && (
                                    <span className="float-right text-[#d5b47a]">●</span>
                                )}
                            </button>
                        ))}
                    </div>
                </div>

                {/* Results count */}
                <div className="mt-6 border-t border-[#1c1815]/10 pt-6">
                    <p className="text-sm text-[#746b62]">
                        <span className="font-bold text-[#9A6A2F]">{auctions.length}</span> phiên đấu giá
                    </p>
                </div>
            </div>
        </aside>
    );

    // Lot Card Component
    const LotCard = ({ auc }) => {
        const coverImageUrl = (auc.images && auc.images.length > 0) 
            ? (auc.images.find(img => img.isCover)?.url || auc.images[0].url)
            : (auc.coverImage || auc.cover_image);

        return (
        <Link to={`/auctions/${auc.id}`} className="group block overflow-hidden rounded-2xl border border-[#1c1815]/10 bg-[#fffdf8] shadow-[0_14px_45px_rgba(28,24,21,0.055)] transition-all duration-500 hover:-translate-y-1 hover:border-[#9A6A2F]/30 hover:shadow-[0_22px_60px_rgba(28,24,21,0.11)]">
            <div className="relative aspect-[4/3] overflow-hidden bg-[#eee6db]">
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
                    {statusText[auc.status] || auc.status}
                </div>

                {/* Countdown */}
                {(auc.status === 'ACTIVE' || auc.status === 'PENDING') && (
                    <div className="absolute bottom-3 left-3 right-3 flex items-center justify-between rounded-xl border border-white/15 bg-[#1c1815]/72 px-3 py-2 backdrop-blur-md">
                        <span className="flex items-center gap-2 text-xs text-white/70">
                            <Clock3 className="h-3.5 w-3.5 text-[#E8C58F]" />
                            {auc.status === 'ACTIVE' ? 'Còn lại' : 'Mở sau'}
                        </span>
                        <span className="font-mono text-sm font-bold text-[#E8C58F]">
                            {getCountdown(auc)}
                        </span>
                    </div>
                )}
            </div>

            <div className="p-5">
                {(auc.categoryName || auc.category_name) && (
                    <p className="text-xs uppercase tracking-wider text-[#9A6A2F] mb-1">
                        {auc.categoryName || auc.category_name}
                    </p>
                )}
                <h3 className="line-clamp-1 font-serif text-xl font-medium text-[#1c1815] transition-colors group-hover:text-[#9A6A2F]">
                    {auc.productName || auc.product_name}
                </h3>
                
                <div className="mt-3 flex items-end justify-between">
                    <div>
                        <p className="text-[9px] font-bold uppercase tracking-[0.16em] text-[#746b62]">
                            Giá hiện tại
                        </p>
                        <p className="text-lg font-bold text-[#1c1815]">
                            {formatCurrency(auc.currentPrice != null ? auc.currentPrice : auc.current_price)}
                        </p>
                    </div>
                    <div className="flex items-center gap-1.5 text-xs text-[#746b62]">
                        <Users className="h-3.5 w-3.5" />
                        <span>{Number(auc.bidCount || auc.bid_count || 0)}</span>
                    </div>
                </div>
            </div>
        </Link>
        );
    };

    // List View Card
    const LotListItem = ({ auc }) => {
        const coverImageUrl = (auc.images && auc.images.length > 0) 
            ? (auc.images.find(img => img.isCover)?.url || auc.images[0].url)
            : (auc.coverImage || auc.cover_image);

        return (
        <Link to={`/auctions/${auc.id}`} className="group block overflow-hidden rounded-2xl border border-[#1c1815]/10 bg-[#fffdf8] shadow-[0_12px_38px_rgba(28,24,21,0.05)] transition-all duration-500 hover:border-[#9A6A2F]/30 hover:shadow-[0_20px_50px_rgba(28,24,21,0.1)]">
            <div className="flex flex-col sm:flex-row">
                <div className="relative h-52 w-full flex-shrink-0 overflow-hidden bg-[#eee6db] sm:h-auto sm:w-56">
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
                        {statusText[auc.status] || auc.status}
                    </div>
                </div>

                <div className="flex flex-1 flex-col justify-between p-5 sm:p-6">
                    <div>
                        {(auc.categoryName || auc.category_name) && (
                            <p className="text-xs uppercase tracking-wider text-[#9A6A2F] mb-1">
                                {auc.categoryName || auc.category_name}
                            </p>
                        )}
                        <h3 className="font-serif text-2xl font-medium text-[#1c1815] transition-colors group-hover:text-[#9A6A2F]">
                            {auc.productName || auc.product_name}
                        </h3>
                        
                        {(auc.status === 'ACTIVE' || auc.status === 'PENDING') && (
                            <div className="mt-3 flex items-center gap-2 text-sm text-[#746b62]">
                                <Clock3 className="h-4 w-4 text-[#9A6A2F]" />
                                <span>{auc.status === 'ACTIVE' ? 'Còn lại' : 'Mở sau'}:</span>
                                <span className="font-mono font-bold text-[#1c1815]">
                                    {getCountdown(auc)}
                                </span>
                            </div>
                        )}
                    </div>

                    <div className="mt-3 flex items-end justify-between">
                        <div>
                            <p className="text-[9px] font-bold uppercase tracking-[0.16em] text-[#746b62]">
                                Giá hiện tại
                            </p>
                            <p className="text-xl font-bold text-[#1c1815]">
                                {formatCurrency(auc.currentPrice != null ? auc.currentPrice : auc.current_price)}
                            </p>
                        </div>
                        <div className="flex items-center gap-4">
                            <div className="flex items-center gap-1.5 text-sm text-[#746b62]">
                                <Users className="h-4 w-4" />
                                <span>{Number(auc.bidCount || auc.bid_count || 0)} lượt</span>
                            </div>
                            <ArrowUpRight className="h-5 w-5 text-[#9A6A2F] transition-colors group-hover:text-[#1c1815]" />
                        </div>
                    </div>
                </div>
            </div>
        </Link>
        );
    };

    return (
        <main ref={pageRef} className="min-h-screen bg-[#f2ece2] text-[#1c1815]">
            {/* Header */}
            <div className="relative overflow-hidden border-b border-[#1c1815]/10 bg-[#ece4d8]">
                <div className="absolute inset-0 opacity-40 [background-image:linear-gradient(rgba(154,106,47,0.06)_1px,transparent_1px),linear-gradient(90deg,rgba(154,106,47,0.05)_1px,transparent_1px)] [background-size:72px_72px]" />
                <div className="relative mx-auto max-w-[90rem] px-4 py-12 sm:px-6 sm:py-16 lg:px-8">
                    <div className="flex flex-col sm:flex-row sm:items-end justify-between gap-4">
                        <div>
                            <p className="mb-4 text-[10px] font-bold uppercase tracking-[0.3em] text-[#9A6A2F]">Live catalogue · The Curator</p>
                            <h1 className="font-serif text-4xl font-medium text-[#1c1815] sm:text-5xl lg:text-6xl">
                                Phiên đấu giá tuyển chọn
                            </h1>
                            <p className="mt-4 text-sm text-[#746b62]">
                                Khám phá các phiên đấu giá đang diễn ra và sắp tới
                            </p>
                        </div>
                        <div className="flex items-center gap-3">
                            {/* View Toggle */}
                            <div className="flex rounded-full border border-[#1c1815]/10 bg-white/45 p-1">
                                <button
                                    onClick={() => setViewMode('grid')}
                                    className={`p-1.5 rounded transition-colors ${
                                        viewMode === 'grid' 
                                            ? 'bg-[#1c1815] text-white'
                                            : 'text-[#746b62] hover:text-[#1c1815]'
                                    }`}
                                >
                                    <Grid className="h-4 w-4" />
                                </button>
                                <button
                                    onClick={() => setViewMode('list')}
                                    className={`p-1.5 rounded transition-colors ${
                                        viewMode === 'list' 
                                            ? 'bg-[#1c1815] text-white'
                                            : 'text-[#746b62] hover:text-[#1c1815]'
                                    }`}
                                >
                                    <List className="h-4 w-4" />
                                </button>
                            </div>
                            {/* Mobile Filter Button */}
                            <button
                                onClick={() => setShowMobileFilter(true)}
                                className="flex items-center gap-2 rounded-full border border-[#1c1815]/10 bg-white/45 px-4 py-2 text-[#746b62] transition-colors hover:bg-white hover:text-[#1c1815] lg:hidden"
                            >
                                <SlidersHorizontal className="h-4 w-4" />
                                <span className="text-sm">Lọc</span>
                                {activeFilterCount > 0 && (
                                    <span className="ml-1 px-1.5 py-0.5 bg-[#9A6A2F] text-xs rounded-full text-white">
                                        {activeFilterCount}
                                    </span>
                                )}
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            {/* Main Content */}
            <div className="mx-auto max-w-[90rem] px-4 py-10 sm:px-6 lg:px-8">
                <div className="flex flex-col gap-8 lg:flex-row">
                    {/* Filter Panel - Desktop */}
                    <div className="hidden lg:block w-72 flex-shrink-0">
                        <FilterPanel />
                    </div>

                    {/* Auction List */}
                    <div className="flex-1">
                        {/* Results Header */}
                        <div className="flex items-center justify-between mb-6">
                            <p className="text-sm text-[#746b62]">
                                Hiển thị <span className="font-bold text-[#9A6A2F]">{auctions.length}</span> phiên
                            </p>
                            {activeFilterCount > 0 && (
                                <div className="flex items-center gap-2">
                                    <span className="text-xs text-[#746b62]">Đang lọc:</span>
                                    <div className="flex items-center gap-2">
                                        {currentStatus && (
                                            <span className="rounded-full bg-[#9A6A2F]/10 px-3 py-1 text-xs text-[#9A6A2F]">
                                                {statusOptions.find(o => o.value === currentStatus)?.label}
                                            </span>
                                        )}
                                        {currentCategory && (
                                            <span className="rounded-full bg-[#9A6A2F]/10 px-3 py-1 text-xs text-[#9A6A2F]">
                                                {categories.find(c => c.id === currentCategory)?.name}
                                            </span>
                                        )}
                                    </div>
                                </div>
                            )}
                        </div>

                        {/* Results */}
                        {auctions.length > 0 ? (
                            viewMode === 'grid' ? (
                                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 xl:gap-6">
                                    {auctions.map(auc => (
                                        <LotCard key={auc.id} auc={auc} />
                                    ))}
                                </div>
                            ) : (
                                <div className="space-y-4">
                                    {auctions.map(auc => (
                                        <LotListItem key={auc.id} auc={auc} />
                                    ))}
                                </div>
                            )
                        ) : (
                            <EmptyState 
                                icon={Search} 
                                title="Không tìm thấy kết quả" 
                                description="Thử thay đổi bộ lọc hoặc quay lại sau" 
                            />
                        )}
                    </div>
                </div>
            </div>

            {/* Mobile Filter Modal */}
            {showMobileFilter && (
                <div className="fixed inset-0 z-50 lg:hidden">
                    <div 
                        className="absolute inset-0 bg-black/60 backdrop-blur-sm" 
                        onClick={() => setShowMobileFilter(false)} 
                    />
                    <div className="absolute bottom-0 left-0 right-0 max-h-[85vh] overflow-y-auto rounded-t-2xl bg-[#f2ece2] shadow-2xl">
                        <div className="sticky top-0 flex items-center justify-between border-b border-[#1c1815]/10 bg-[#fffdf8] p-4">
                            <h3 className="text-sm font-bold text-[#1c1815]">Bộ lọc</h3>
                            <button 
                                onClick={() => setShowMobileFilter(false)}
                                className="rounded-lg p-2 transition-colors hover:bg-[#f2ece2]"
                            >
                                <X className="h-5 w-5 text-[#746b62]" />
                            </button>
                        </div>
                        <div className="p-4 pb-8">
                            <FilterPanel />
                        </div>
                    </div>
                </div>
            )}
        </main>
    );
}

export default AuctionList;
