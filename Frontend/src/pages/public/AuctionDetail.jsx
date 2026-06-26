import { useEffect, useMemo, useRef, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ArrowUpRight, ChevronLeft, Clock3, Gavel, Info, Minus, Plus, Radio, TrendingUp } from 'lucide-react';
import apiClient, { WS_URL } from '@/services/apiClient';
import Skeleton from '@/components/Elements/Skeleton';
import Confetti from '@/components/Elements/Confetti';
import { useToast } from '@/components/Elements/Toast';

const money = value => Number(value || 0).toLocaleString('vi-VN');
const statusLabel = { ACTIVE: 'Đang diễn ra', PENDING: 'Sắp bắt đầu', CLOSED: 'Đã chốt phiên', ENDED: 'Đã kết thúc', FAILED: 'Thất bại', CANCELLED: 'Đã hủy' };

function getCurrentUserId() {
    const token = localStorage.getItem('token');
    if (!token) return null;
    try {
        return JSON.parse(atob(token.split('.')[1]))?.user_id || null;
    } catch {
        return null;
    }
}

function getParts(label) {
    const match = label?.match(/(\d+)d\s+(\d+)h\s+(\d+)m\s+(\d+)s/);
    return match ? [match[1], match[2], match[3], match[4]] : null;
}

export default function AuctionDetail() {
    const { id } = useParams();
    const [auction, setAuction] = useState(null);
    const [bids, setBids] = useState([]);
    const [bidAmount, setBidAmount] = useState(0);
    const [errorMsg, setErrorMsg] = useState('');
    const [activeImage, setActiveImage] = useState('');
    const [timeLeft, setTimeLeft] = useState('');
    const [viewerCount, setViewerCount] = useState(0);
    const [showConfetti, setShowConfetti] = useState(false);
    const { toast } = useToast();
    const ws = useRef(null);
    const closeSyncTriggered = useRef(false);
    const latestOwnBid = useRef(null);
    const currentUserId = useMemo(getCurrentUserId, []);

    const loadData = () => {
        apiClient.get(`/auctions/${id}`).then(res => {
            const data = res.data?.result || res.data;
            if (data) {
                const cover = data.images?.find(img => img.is_cover)?.url || data.images?.[0]?.url || '';
                setAuction(data);
                setActiveImage(cover);
                setBidAmount((data.current_price || 0) + (data.step_price || 0));
            }
        }).catch(console.error);
        apiClient.get(`/auctions/${id}/bids`)
            .then(res => setBids(res.data?.result || (Array.isArray(res.data) ? res.data : [])))
            .catch(console.error);
    };

    useEffect(() => {
        closeSyncTriggered.current = false;
        setViewerCount(0);
        loadData();

        import('@stomp/stompjs').then(({ Client }) => {
            import('sockjs-client').then(({ default: SockJS }) => {
                const stompClient = new Client({
                    webSocketFactory: () => new SockJS(`${WS_URL}/ws`),
                    debug: function (str) {
                        console.log(str);
                    },
                    reconnectDelay: 5000,
                    heartbeatIncoming: 4000,
                    heartbeatOutgoing: 4000,
                });

                stompClient.onConnect = function (frame) {
                    console.log('Connected: ' + frame);
                    stompClient.subscribe(`/topic/auction/${id}`, function (messageOutput) {
                        const data = JSON.parse(messageOutput.body);
                        if (data.type === 'new_bid') {
                            const bidValue = Number(data.bid_amount || 0);
                            if (data.end_time) {
                                setAuction(prev => prev ? { ...prev, end_time: data.end_time } : prev);
                                closeSyncTriggered.current = false;
                            }
                            const isOwnBidEcho = data.bidder_id
                                ? String(data.bidder_id) === String(currentUserId)
                                : latestOwnBid.current === bidValue;
                            if (isOwnBidEcho) {
                                latestOwnBid.current = null;
                            } else if (bidValue > 0) {
                                toast.warning(`Bạn vừa bị vượt giá: ${money(bidValue)} đ. Hãy đặt giá mới nếu muốn tiếp tục dẫn đầu.`, 6500);
                            }
                            if (data.extended) {
                                toast.info('Có lượt đặt giá trong 10 giây cuối, phiên đã được gia hạn thêm 1 phút.', 6500);
                            }
                            loadData();
                        }
                        if (data.type === 'auction_ended') loadData();
                    });

                    stompClient.subscribe(`/topic/auction/${id}/status`, function (messageOutput) {
                        const data = JSON.parse(messageOutput.body);
                        if (data.type === 'viewer_count') {
                            setViewerCount(Number(data.viewer_count || 0));
                        }
                    });
                };

                stompClient.activate();
                ws.current = stompClient;
            });
        });

        return () => {
            if (ws.current) {
                ws.current.deactivate();
            }
        };
    }, [currentUserId, id, toast]);

    useEffect(() => {
        if (!auction || auction.status !== 'ACTIVE') {
            setTimeLeft(auction?.status === 'PENDING' ? 'Chưa bắt đầu' : 'Đã kết thúc');
            return undefined;
        }
        const tick = () => {
            const distance = new Date(auction.end_time).getTime() - Date.now();
            if (distance < 0) {
                setTimeLeft('Đang chờ chốt phiên...');
                if (!closeSyncTriggered.current) {
                    closeSyncTriggered.current = true;
                    loadData();
                }
                return;
            }
            const d = Math.floor(distance / 86400000);
            const h = Math.floor((distance % 86400000) / 3600000);
            const m = Math.floor((distance % 3600000) / 60000);
            const s = Math.floor((distance % 60000) / 1000);
            setTimeLeft(`${d}d ${h}h ${m}m ${s}s`);
        };
        tick();
        const timer = setInterval(tick, 1000);
        return () => clearInterval(timer);
    }, [auction]);

    const handlePlaceBid = async () => {
        setErrorMsg('');
        try {
            await apiClient.post(`/auctions/${id}/bid`, { bid_amount: bidAmount });
            latestOwnBid.current = Number(bidAmount);
            toast.success(`Đặt giá thành công: ${money(bidAmount)} đ. Bạn đang tạm dẫn đầu!`, 4500);
            setShowConfetti(true);
            setTimeout(() => setShowConfetti(false), 4500);
        } catch (error) {
            const message = error.response?.data?.detail || 'Lỗi tham gia đấu giá';
            setErrorMsg(message);
            toast.error(message, 5500);
        }
    };

    const stats = useMemo(() => {
        if (!auction) return null;
        const bidderCount = new Set(bids.map(bid => bid.bidder_name).filter(Boolean)).size;
        const uplift = auction.start_price ? Math.max(0, Math.round(((auction.current_price - auction.start_price) / auction.start_price) * 100)) : 0;
        return { bidders: bidderCount, bidCount: bids.length, uplift, viewers: viewerCount };
    }, [auction, bids, viewerCount]);

    if (!auction) return <Skeleton.Detail />;

    const isActive = auction.status === 'ACTIVE';
    const parts = getParts(timeLeft);
    const topBid = bids[0];
    const minBid = auction.current_price + auction.step_price;

    return (
        <div className="curator-detail-page">
            {showConfetti && <Confetti />}
            <main className="curator-detail-shell">
                <Link to="/" className="curator-back"><ChevronLeft className="h-4 w-4" /> Trang chủ</Link>

                <section className="curator-detail-grid">
                    <div>
                        <div className="curator-status-row">
                            <span className={`curator-status ${isActive ? 'live' : ''}`}>{isActive && <span />} {statusLabel[auction.status]}</span>
                            <span className="curator-room"><Radio className="h-3.5 w-3.5" /> Live bidding room</span>
                        </div>

                        <p className="curator-kicker">Lô đấu giá</p>
                        <h1 className="curator-title">{auction.product_name}</h1>
                        <p className="curator-lead">Tất cả số liệu bên dưới lấy trực tiếp từ phiên đấu giá và lịch sử đặt giá hiện tại.</p>

                        <div className="curator-stats-row">
                            <Stat label="Đang xem phiên" value={stats.viewers} suffix="người" dark />
                            <Stat label="Lượt đặt giá" value={stats.bidCount} suffix="lượt" />
                            <Stat label="Tăng giá" value={stats.uplift} suffix="%" icon={<ArrowUpRight className="h-4 w-4" />} />
                        </div>

                        <div className="curator-main-lot">
                            <ImageBox auction={auction} activeImage={activeImage} setActiveImage={setActiveImage} />
                            <BidPanel auction={auction} parts={parts} timeLeft={timeLeft} topBid={topBid} bidAmount={bidAmount} setBidAmount={setBidAmount} minBid={minBid} errorMsg={errorMsg} isActive={isActive} handlePlaceBid={handlePlaceBid} />
                        </div>

                        <section className="curator-description">
                            <p className="curator-kicker">Thông tin vật phẩm</p>
                            <p>{auction.description}</p>
                        </section>
                    </div>

                    <aside className="curator-side">
                        <ActivityFeed bids={bids} auction={auction} />
                        <AuctionSpecs auction={auction} stats={stats} />
                    </aside>
                </section>
            </main>
        </div>
    );
}

function ImageBox({ auction, activeImage, setActiveImage }) {
    const thumbs = auction.images || [];
    return (
        <div className="curator-image-block">
            <div className="curator-image-main">
                {activeImage && <img src={activeImage} alt={auction.product_name} />}
                <div className="curator-image-caption"><span>Hình ảnh sản phẩm</span><strong>{auction.product_name}</strong></div>
            </div>
            <div className="curator-thumbs">
                {thumbs.slice(0, 3).map((img, idx) => <button key={idx} onClick={() => setActiveImage(img.url)} className={activeImage === img.url ? 'active' : ''}><img src={img.url} alt="" /></button>)}
                {thumbs.length > 3 && <button className="more" onClick={() => setActiveImage(thumbs[3]?.url || activeImage)}>+{thumbs.length - 3}</button>}
            </div>
        </div>
    );
}

function BidPanel({ auction, parts, timeLeft, topBid, bidAmount, setBidAmount, minBid, errorMsg, isActive, handlePlaceBid }) {
    return (
        <div className="curator-bid-card">
            <div className="curator-current-price"><span>Giá hiện tại</span><strong>{money(auction.current_price)} <em>đ</em></strong></div>
            <div className="curator-countdown">
                <div className="curator-panel-title"><Clock3 className="h-4 w-4" /> <span>Thời gian còn lại</span> {isActive && <b>Đếm ngược trực tiếp</b>}</div>
                {parts ? <div className="curator-time-grid">{['Ngày', 'Giờ', 'Phút', 'Giây'].map((label, i) => <div key={label}><strong>{parts[i]}</strong><span>{label}</span></div>)}</div> : <p className="curator-time-text">{timeLeft}</p>}
            </div>
            <div className="curator-mini-grid"><Mini label="Người dẫn đầu" value={topBid?.bidder_name || 'Chưa có'} /><Mini label="Tối thiểu" value={`${money(minBid)}đ`} /></div>
            {isActive ? <div className="curator-bid-actions"><div className="curator-bid-input"><button onClick={() => setBidAmount(prev => Math.max(minBid, prev - auction.step_price))}><Minus className="h-4 w-4" /></button><input type="number" value={bidAmount} onChange={e => setBidAmount(Number(e.target.value))} /><button onClick={() => setBidAmount(prev => prev + auction.step_price)}><Plus className="h-4 w-4" /></button></div>{errorMsg && <p className="curator-error"><Info className="h-3.5 w-3.5" /> {errorMsg}</p>}<button onClick={handlePlaceBid} className="curator-submit"><Gavel className="h-4 w-4" /> Đặt giá và vượt lên</button></div> : <div className="curator-disabled">Phiên đấu giá không khả dụng</div>}
        </div>
    );
}

function ActivityFeed({ bids, auction }) {
    return (
        <div className="curator-feed">
            <header><TrendingUp className="h-7 w-7" /><div><h2>Lịch sử đấu giá</h2><p>{bids.length} lượt đặt giá thực tế</p></div>{auction.status === 'ACTIVE' && <span>Live</span>}</header>
            <div className="curator-feed-list">{bids.length === 0 ? <p className="curator-empty-bids">Chưa có lượt đặt giá</p> : bids.slice(0, 8).map((bid, idx) => <BidRow key={bid.id} bid={bid} idx={idx} prev={bids[idx + 1]} auction={auction} />)}</div>
        </div>
    );
}

function BidRow({ bid, idx, prev, auction }) {
    const inc = prev ? bid.bid_amount - prev.bid_amount : bid.bid_amount - auction.start_price;
    return <div className={`curator-bid-row ${idx === 0 ? 'leader' : ''}`}><div className="curator-avatar">{bid.bidder_name?.charAt(0)?.toUpperCase() || '?'}</div><div className="curator-bidder"><p>{bid.bidder_name || 'Người tham gia'} {idx === 0 && <span>Dẫn đầu</span>} {inc > 0 && <em>+{money(inc)} đ</em>}</p><small>{new Date(bid.bid_time).toLocaleString('vi-VN', { dateStyle: 'short', timeStyle: 'medium' })}</small></div><strong>{money(bid.bid_amount)}<small>đ</small></strong></div>;
}

function AuctionSpecs({ auction, stats }) {
    return <div className="curator-specs"><p>Thông số đấu giá</p><div><Metric label="Giá mở" value={`${money(auction.start_price)} đ`} /><Metric label="Bước giá" value={`${money(auction.step_price)} đ`} gold /><Metric label="Tiền cọc" value={`${money(auction.deposit_amount)} đ`} /><Metric label="Đang xem phiên" value={`${stats.viewers} người`} /><Metric label="Người đã đặt giá" value={`${stats.bidders} người`} /></div></div>;
}

function Stat({ label, value, suffix, dark, icon }) { return <div className={`curator-stat ${dark ? 'dark' : ''}`}><span>{label}</span><strong>{value}<small>{suffix}</small>{icon}</strong></div>; }
function Mini({ label, value }) { return <div className="curator-mini"><span>{label}</span><strong>{value}</strong></div>; }
function Metric({ label, value, gold }) { return <div className={gold ? 'gold' : ''}><span>{label}</span><strong>{value}</strong></div>; }