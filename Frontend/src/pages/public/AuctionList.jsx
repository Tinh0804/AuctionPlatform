import { useEffect, useMemo, useRef, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { Activity, ArrowUpRight, ChevronRight, Clock3, Filter, Gavel, Search, SlidersHorizontal, Users, X, Grid, List } from 'lucide-react';
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
    ACTIVE: 'bg-green-600/20 text-green-400 border-green-500/30',
    PENDING: 'bg-yellow-600/20 text-yellow-400 border-yellow-500/30',
    CLOSED: 'bg-gray-600/20 text-gray-400 border-gray-500/30',
    ENDED: 'bg-red-600/20 text-red-400 border-red-500/30',
    FAILED: 'bg-red-600/20 text-red-400 border-red-500/30',
    CANCELLED: 'bg-gray-600/20 text-gray-400 border-gray-500/30',
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
            <div className="bg-[#1A140F] rounded-xl border border-[#9A6A2F]/20 p-6 sticky top-24">
                <div className="flex items-center justify-between mb-6">
                    <div className="flex items-center gap-3">
                        <Filter className="h-5 w-5 text-[#E8C58F]" />
                        <h3 className="text-sm font-medium text-[#FFF8ED]">Bộ lọc</h3>
                    </div>
                    {activeFilterCount > 0 && (
                        <button 
                            onClick={clearAllFilters}
                            className="text-xs text-[#9A6A2F] hover:text-[#E8C58F] transition-colors"
                        >
                            Xóa tất cả
                        </button>
                    )}
                </div>

                {/* Status Filter */}
                <div className="mb-6">
                    <label className="block text-xs uppercase tracking-wider text-[#9A6A2F] mb-3">
                        Trạng thái
                    </label>
                    <div className="space-y-1.5">
                        {statusOptions.map(opt => (
                            <button
                                key={opt.value}
                                onClick={() => updateFilter('status', opt.value)}
                                className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-all ${
                                    currentStatus === opt.value
                                        ? 'bg-[#9A6A2F]/20 text-[#E8C58F] border border-[#9A6A2F]/40'
                                        : 'text-[#FFF8ED]/60 hover:bg-[#2F2418]/50 hover:text-[#FFF8ED]'
                                }`}
                            >
                                {opt.label}
                                {currentStatus === opt.value && (
                                    <span className="float-right text-[#E8C58F]">●</span>
                                )}
                            </button>
                        ))}
                    </div>
                </div>

                {/* Category Filter */}
                <div>
                    <label className="block text-xs uppercase tracking-wider text-[#9A6A2F] mb-3">
                        Danh mục
                    </label>
                    <div className="space-y-1.5">
                        <button
                            onClick={() => updateFilter('category_id', '')}
                            className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-all ${
                                currentCategory === ''
                                    ? 'bg-[#9A6A2F]/20 text-[#E8C58F] border border-[#9A6A2F]/40'
                                    : 'text-[#FFF8ED]/60 hover:bg-[#2F2418]/50 hover:text-[#FFF8ED]'
                            }`}
                        >
                            Tất cả danh mục
                            {currentCategory === '' && (
                                <span className="float-right text-[#E8C58F]">●</span>
                            )}
                        </button>
                        {categories.map(category => (
                            <button
                                key={category.id}
                                onClick={() => updateFilter('category_id', category.id)}
                                className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-all ${
                                    currentCategory === category.id
                                        ? 'bg-[#9A6A2F]/20 text-[#E8C58F] border border-[#9A6A2F]/40'
                                        : 'text-[#FFF8ED]/60 hover:bg-[#2F2418]/50 hover:text-[#FFF8ED]'
                                }`}
                            >
                                {category.name}
                                {currentCategory === category.id && (
                                    <span className="float-right text-[#E8C58F]">●</span>
                                )}
                            </button>
                        ))}
                    </div>
                </div>

                {/* Results count */}
                <div className="mt-6 pt-6 border-t border-[#9A6A2F]/10">
                    <p className="text-sm text-[#FFF8ED]/50">
                        <span className="text-[#E8C58F] font-medium">{auctions.length}</span> phiên đấu giá
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
        <Link to={`/auctions/${auc.id}`} className="group block bg-[#1A140F] rounded-xl overflow-hidden border border-[#9A6A2F]/10 hover:border-[#9A6A2F]/40 transition-all duration-300 hover:shadow-xl hover:shadow-[#9A6A2F]/5">
            <div className="relative aspect-[16/10] overflow-hidden bg-[#0E0A07]">
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
                    <div className="absolute bottom-3 left-3 right-3 bg-black/70 backdrop-blur-sm rounded-lg px-3 py-2 flex items-center justify-between">
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

            <div className="p-4">
                {(auc.categoryName || auc.category_name) && (
                    <p className="text-xs uppercase tracking-wider text-[#9A6A2F] mb-1">
                        {auc.categoryName || auc.category_name}
                    </p>
                )}
                <h3 className="text-sm font-medium text-[#FFF8ED] line-clamp-1 group-hover:text-[#E8C58F] transition-colors">
                    {auc.productName || auc.product_name}
                </h3>
                
                <div className="mt-3 flex items-end justify-between">
                    <div>
                        <p className="text-[10px] uppercase tracking-wider text-[#FFF8ED]/40">
                            Giá hiện tại
                        </p>
                        <p className="text-lg font-semibold text-[#E8C58F]">
                            {formatCurrency(auc.currentPrice != null ? auc.currentPrice : auc.current_price)}
                        </p>
                    </div>
                    <div className="flex items-center gap-1.5 text-xs text-[#FFF8ED]/50">
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
        <Link to={`/auctions/${auc.id}`} className="group block bg-[#1A140F] rounded-xl overflow-hidden border border-[#9A6A2F]/10 hover:border-[#9A6A2F]/40 transition-all duration-300">
            <div className="flex flex-col sm:flex-row">
                <div className="relative w-full sm:w-48 h-48 sm:h-auto flex-shrink-0 overflow-hidden bg-[#0E0A07]">
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

                <div className="flex-1 p-4 flex flex-col justify-between">
                    <div>
                        {(auc.categoryName || auc.category_name) && (
                            <p className="text-xs uppercase tracking-wider text-[#9A6A2F] mb-1">
                                {auc.categoryName || auc.category_name}
                            </p>
                        )}
                        <h3 className="text-base font-medium text-[#FFF8ED] group-hover:text-[#E8C58F] transition-colors">
                            {auc.productName || auc.product_name}
                        </h3>
                        
                        {(auc.status === 'ACTIVE' || auc.status === 'PENDING') && (
                            <div className="mt-2 flex items-center gap-2 text-sm text-[#FFF8ED]/60">
                                <Clock3 className="h-4 w-4 text-[#E8C58F]" />
                                <span>{auc.status === 'ACTIVE' ? 'Còn lại' : 'Mở sau'}:</span>
                                <span className="font-mono font-bold text-[#E8C58F]">
                                    {getCountdown(auc)}
                                </span>
                            </div>
                        )}
                    </div>

                    <div className="mt-3 flex items-end justify-between">
                        <div>
                            <p className="text-[10px] uppercase tracking-wider text-[#FFF8ED]/40">
                                Giá hiện tại
                            </p>
                            <p className="text-xl font-semibold text-[#E8C58F]">
                                {formatCurrency(auc.currentPrice != null ? auc.currentPrice : auc.current_price)}
                            </p>
                        </div>
                        <div className="flex items-center gap-4">
                            <div className="flex items-center gap-1.5 text-sm text-[#FFF8ED]/50">
                                <Users className="h-4 w-4" />
                                <span>{Number(auc.bidCount || auc.bid_count || 0)} lượt</span>
                            </div>
                            <ArrowUpRight className="h-5 w-5 text-[#9A6A2F] group-hover:text-[#E8C58F] transition-colors" />
                        </div>
                    </div>
                </div>
            </div>
        </Link>
        );
    };

    return (
        <main ref={pageRef} className="min-h-screen bg-[#0E0A07]">
            {/* Header */}
            <div className="relative bg-gradient-to-b from-[#1A140F] to-[#0E0A07] border-b border-[#9A6A2F]/10">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 sm:py-12">
                    <div className="flex flex-col sm:flex-row sm:items-end justify-between gap-4">
                        <div>
                            <h1 className="text-2xl sm:text-3xl lg:text-4xl font-serif text-[#FFF8ED]">
                                Phiên đấu giá
                            </h1>
                            <p className="mt-2 text-sm text-[#FFF8ED]/50">
                                Khám phá các phiên đấu giá đang diễn ra và sắp tới
                            </p>
                        </div>
                        <div className="flex items-center gap-3">
                            {/* View Toggle */}
                            <div className="flex rounded-lg border border-[#9A6A2F]/20 p-1">
                                <button
                                    onClick={() => setViewMode('grid')}
                                    className={`p-1.5 rounded transition-colors ${
                                        viewMode === 'grid' 
                                            ? 'bg-[#9A6A2F]/20 text-[#E8C58F]' 
                                            : 'text-[#FFF8ED]/40 hover:text-[#FFF8ED]'
                                    }`}
                                >
                                    <Grid className="h-4 w-4" />
                                </button>
                                <button
                                    onClick={() => setViewMode('list')}
                                    className={`p-1.5 rounded transition-colors ${
                                        viewMode === 'list' 
                                            ? 'bg-[#9A6A2F]/20 text-[#E8C58F]' 
                                            : 'text-[#FFF8ED]/40 hover:text-[#FFF8ED]'
                                    }`}
                                >
                                    <List className="h-4 w-4" />
                                </button>
                            </div>
                            {/* Mobile Filter Button */}
                            <button
                                onClick={() => setShowMobileFilter(true)}
                                className="lg:hidden flex items-center gap-2 px-4 py-2 rounded-lg border border-[#9A6A2F]/20 text-[#FFF8ED]/70 hover:bg-[#1A140F] transition-colors"
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
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <div className="flex flex-col lg:flex-row gap-6">
                    {/* Filter Panel - Desktop */}
                    <div className="hidden lg:block w-72 flex-shrink-0">
                        <FilterPanel />
                    </div>

                    {/* Auction List */}
                    <div className="flex-1">
                        {/* Results Header */}
                        <div className="flex items-center justify-between mb-6">
                            <p className="text-sm text-[#FFF8ED]/50">
                                Hiển thị <span className="text-[#E8C58F] font-medium">{auctions.length}</span> phiên
                            </p>
                            {activeFilterCount > 0 && (
                                <div className="flex items-center gap-2">
                                    <span className="text-xs text-[#FFF8ED]/40">Đang lọc:</span>
                                    <div className="flex items-center gap-2">
                                        {currentStatus && (
                                            <span className="px-2 py-1 text-xs bg-[#9A6A2F]/20 text-[#E8C58F] rounded">
                                                {statusOptions.find(o => o.value === currentStatus)?.label}
                                            </span>
                                        )}
                                        {currentCategory && (
                                            <span className="px-2 py-1 text-xs bg-[#9A6A2F]/20 text-[#E8C58F] rounded">
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
                    <div className="absolute bottom-0 left-0 right-0 max-h-[85vh] overflow-y-auto bg-[#1A140F] rounded-t-2xl shadow-2xl">
                        <div className="sticky top-0 bg-[#1A140F] border-b border-[#9A6A2F]/10 p-4 flex items-center justify-between">
                            <h3 className="text-sm font-medium text-[#FFF8ED]">Bộ lọc</h3>
                            <button 
                                onClick={() => setShowMobileFilter(false)}
                                className="p-2 hover:bg-[#2F2418] rounded-lg transition-colors"
                            >
                                <X className="h-5 w-5 text-[#FFF8ED]/60" />
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