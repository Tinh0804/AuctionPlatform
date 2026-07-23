// ============================================================
// src/layouts/MainLayout.jsx
// Layout chính: Header + Footer + <Outlet />
// ============================================================
import { useEffect, useRef, useState } from 'react';
import gsap from 'gsap';
import { Link, Outlet, useNavigate, useLocation } from 'react-router-dom';
import {
  Search, User, Bell, Wallet, ChevronDown, LogOut, ShieldCheck,
  ClipboardList, Menu, X, Gavel, PlusCircle, Home, Package,
  ArrowRight, Instagram, Facebook, Twitter, Mail, LockKeyhole,
  Headphones, CircleHelp, Settings,
} from 'lucide-react';
import { useStomp } from '@/hooks/useStomp';
import { authApi } from '@/features/auth/api';
import useAuthStore from '@/store/useAuthStore';

const MainLayout = () => {
  const navigate = useNavigate();
  const location = useLocation();

  // Lấy user từ Zustand store (reactive)
  const { token, user, setUser, logout: storeLogout } = useAuthStore();

  // Dùng user từ store làm nguồn hiển thị, aliased thành "profile" để tương thích JSX cũ
  const profile = user;

  const [notifications, setNotifications] = useState([]);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [helpOpen, setHelpOpen] = useState(false);
  const [activeNavItem, setActiveNavItem] = useState(null);
  const helpCordRef = useRef(null);
  const helpHandleRef = useRef(null);

  // Đóng mobile menu khi chuyển route
  useEffect(() => {
    setMobileMenuOpen(false);
    setHelpOpen(false);
  }, [location.pathname]);

  useEffect(() => {
    const media = gsap.matchMedia();
    media.add('(prefers-reduced-motion: no-preference)', () => {
      const cord = helpCordRef.current;
      const handle = helpHandleRef.current;
      if (!cord || !handle) return undefined;

      const swing = gsap.timeline({ repeat: -1, repeatDelay: 7, delay: 2 });
      gsap.set(cord, { transformOrigin: '50% 0%', transformBox: 'fill-box' });
      swing
        .to(cord, { skewX: 7, duration: 1.2, ease: 'sine.inOut' }, 0)
        .to(handle, { x: 6, rotation: 6, duration: 1.2, ease: 'sine.inOut', force3D: true }, 0)
        .to(cord, { skewX: -7, duration: 2.4, ease: 'sine.inOut' })
        .to(handle, { x: -6, rotation: -6, duration: 2.4, ease: 'sine.inOut', force3D: true }, '<')
        .to(cord, { skewX: 0, duration: 1.2, ease: 'sine.inOut' })
        .to(handle, { x: 0, rotation: 0, duration: 1.2, ease: 'sine.inOut', force3D: true }, '<');

      return () => {
        swing.kill();
        gsap.set([cord, handle], { clearProps: 'transform' });
      };
    });
    return () => media.revert();
  }, []);

  // Nếu có token nhưng chưa có user (ví dụ layout render trước App.jsx), tự fetch
  useEffect(() => {
    if (token && !user) {
      authApi.getMyInfo()
        .then(res => setUser(res.result || res))
        .catch(() => { });
    }
  }, [token, user, setUser]);

  // Lấy thông báo khi đã có user
  useEffect(() => {
    if (token) {
      authApi.getNotifications()
        .then(res => setNotifications(Array.isArray(res.result) ? res.result : (Array.isArray(res) ? res : [])))
        .catch(console.error);
    }
  }, [token]);

  // WebSocket thông báo realtime
  useStomp({
    deps: [profile?.id],
    onConnect: (stompClient) => {
      if (!profile?.id) return;

      stompClient.subscribe(`/topic/notification/${profile.id}`, (messageOutput) => {
        try {
          const incoming = JSON.parse(messageOutput.body);
          if (!incoming?.title) return;

          setNotifications(prev => {
            if (incoming.id && prev.some(n => n.id === incoming.id)) return prev;
            return [{
              id: incoming.id || `${incoming.type}-${incoming.referenceId || incoming.reference_id}-${Date.now()}`,
              title: incoming.title,
              message: incoming.message,
              type: incoming.type,
              reference_id: incoming.referenceId || incoming.reference_id,
              is_read: incoming.isRead != null ? incoming.isRead : (incoming.is_read ?? false),
              created_at: incoming.createdAt || incoming.created_at || new Date().toISOString(),
            }, ...prev].slice(0, 20);
          });
        } catch (error) {
          console.error('Invalid notification socket payload', error);
        }
      });
    }
  });

  const handleLogout = () => {
    storeLogout();
    window.location.href = '/login';
  };

  const unreadCount = notifications.filter(n => !n.is_read).length;
  const isActive = (path) => location.pathname === path;

  const handleGoHome = () => {
    setActiveNavItem('home');
    setMobileMenuOpen(false);
    if (window.location.hash) window.history.replaceState(null, '', window.location.pathname);
    if (location.pathname === '/') {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } else {
      navigate('/');
    }
  };

  const handleGoAuctionFloor = () => {
    setActiveNavItem('auction-floor');
    setMobileMenuOpen(false);
    if (location.pathname === '/') {
      const el = document.getElementById('auction-floor');
      if (el) el.scrollIntoView({ behavior: 'smooth' });
    } else {
      navigate('/');
      setTimeout(() => {
        const el = document.getElementById('auction-floor');
        if (el) el.scrollIntoView({ behavior: 'smooth' });
      }, 300);
    }
  };

  const handleGoActiveAuctions = () => {
    setActiveNavItem('shopping');
    setMobileMenuOpen(false);
    navigate('/?status=ACTIVE');
    setTimeout(() => {
      const el = document.getElementById('auction-floor');
      if (el) el.scrollIntoView({ behavior: 'smooth' });
    }, 120);
  };

  // Tên hiển thị: UserResponse trả về field "name" (không phải full_name)
  const displayName = profile?.name || profile?.full_name || '';
  const displayEmail = profile?.email || '';
  const walletBalance = profile?.wallet?.available_balance || 0;
  const isAdmin = profile?.account?.role?.name === 'ADMIN';

  return (
    <div className="min-h-screen flex flex-col bg-[#F8F1E6] text-[#2F2418]">
      {/* Header / Navbar */}
      <header className="site-header fixed left-0 right-0 top-0 z-50 border-b border-[#1c1815]/10 bg-[#fbfaf7]/92 shadow-[0_8px_30px_rgba(28,24,21,0.06)] backdrop-blur-xl">
        <div className="site-header-shell mx-auto flex h-[72px] max-w-[94rem] items-center justify-between bg-[#fbfaf7] px-5 md:h-[78px] md:px-8">
          {/* Logo */}
          <button type="button" onClick={handleGoHome} className="site-logo group flex shrink-0 cursor-pointer items-center gap-2.5 border-none bg-transparent p-0">
            <img src="/brand/curator-mark.svg" alt="" className="h-9 w-9 rounded-xl bg-[#eee8de] p-1 transition-transform duration-300 group-hover:scale-105" />
            <span className="hidden flex-col items-start leading-none sm:flex">
              <span className="text-[0.9rem] font-bold uppercase tracking-[0.1em] text-[#151715]">The Curator</span>
              <span className="mt-1 text-[6px] font-bold uppercase tracking-[0.24em] text-[#8b5d27]">Online Auction Platform</span>
            </span>
          </button>

          {/* Main Navigation - Desktop */}
          <nav className="site-nav mx-4 hidden shrink-0 flex-nowrap items-center gap-1 whitespace-nowrap border-0 bg-transparent p-1 lg:flex xl:mx-8">
            <button type="button" onMouseEnter={() => setActiveNavItem('home')} onClick={handleGoHome} className={`site-nav-item px-3 xl:px-4 py-2 text-xs font-semibold uppercase tracking-[0.12em] xl:tracking-[0.18em] whitespace-nowrap transition-all cursor-pointer ${activeNavItem === 'home' ? 'is-active text-[#F8F1E6]' : 'text-[#2F2418]/68 hover:text-[#2F2418] hover:bg-white/5'}`}>
              Trang Chủ
            </button>
            <button type="button" onMouseEnter={() => setActiveNavItem('auction-floor')} onClick={handleGoAuctionFloor} className={`site-nav-item px-3 xl:px-4 py-2 text-xs font-semibold uppercase tracking-[0.12em] xl:tracking-[0.18em] whitespace-nowrap transition-all cursor-pointer ${activeNavItem === 'auction-floor' ? 'is-active text-[#F8F1E6]' : 'text-[#2F2418]/68 hover:text-[#2F2418] hover:bg-white/5'}`}>
              Sàn Đấu Giá
            </button>

            {/* Dropdown Mua Sắm */}
            <div className="relative group">
              <button onMouseEnter={() => setActiveNavItem('shopping')} onClick={() => setActiveNavItem('shopping')} className={`site-nav-item flex items-center gap-1.5 px-3 xl:px-4 py-2 text-xs font-semibold uppercase tracking-[0.12em] xl:tracking-[0.18em] whitespace-nowrap transition-all ${activeNavItem === 'shopping' ? 'is-active text-[#F8F1E6]' : 'text-[#2F2418]/68 hover:text-[#2F2418] hover:bg-white/5'}`}>
                Mua Sắm <ChevronDown className="w-3.5 h-3.5 transition-transform group-hover:rotate-180" />
              </button>
              <div className="absolute top-full left-0 pt-2 w-56 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 transform -translate-y-1 group-hover:translate-y-0">
                <div className="overflow-hidden rounded-2xl border border-white/80 bg-[#faf7f1]/95 p-1.5 shadow-[0_24px_70px_rgba(28,24,21,0.14)] backdrop-blur-xl">
                  <button type="button" onClick={handleGoActiveAuctions} className="block w-full text-left px-4 py-3 text-sm text-[#2F2418]/75 hover:bg-[#9A6A2F]/10 hover:text-[#9A6A2F] transition-colors cursor-pointer">
                    Sản phẩm đang đấu giá
                  </button>
                  <Link to="/profile/orders?sub=purchases" className="block px-4 py-3 text-sm text-[#2F2418]/75 hover:bg-[#9A6A2F]/10 hover:text-[#9A6A2F] transition-colors">
                    Đơn hàng đã mua
                  </Link>
                </div>
              </div>
            </div>

            {/* Dropdown Bán Hàng */}
            <div className="relative group">
              <button onMouseEnter={() => setActiveNavItem('selling')} onClick={() => setActiveNavItem('selling')} className={`site-nav-item flex items-center gap-1.5 px-3 xl:px-4 py-2 text-xs font-semibold uppercase tracking-[0.12em] xl:tracking-[0.18em] whitespace-nowrap transition-all ${activeNavItem === 'selling' ? 'is-active text-[#F8F1E6]' : 'text-[#2F2418]/68 hover:text-[#2F2418] hover:bg-white/5'}`}>
                Bán Hàng <ChevronDown className="w-3.5 h-3.5 transition-transform group-hover:rotate-180" />
              </button>
              <div className="absolute top-full left-0 pt-2 w-60 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 transform -translate-y-1 group-hover:translate-y-0">
                <div className="overflow-hidden rounded-2xl border border-white/80 bg-[#faf7f1]/95 p-1.5 shadow-[0_24px_70px_rgba(28,24,21,0.14)] backdrop-blur-xl">
                  <Link to="/auctions/create" className="block px-4 py-3 text-sm text-[#2F2418]/75 hover:bg-[#9A6A2F]/10 hover:text-[#9A6A2F] transition-colors">
                    Tạo phiên đấu giá mới
                  </Link>
                  <Link to="/profile/orders?sub=sales" className="block px-4 py-3 text-sm text-[#2F2418]/75 hover:bg-[#9A6A2F]/10 hover:text-[#9A6A2F] transition-colors">
                    Quản lý phiên đấu giá
                  </Link>
                  <Link to="/profile/orders?sub=sales" className="block px-4 py-3 text-sm text-[#2F2418]/75 hover:bg-[#9A6A2F]/10 hover:text-[#9A6A2F] transition-colors">
                    Đơn hàng cần giao
                  </Link>
                </div>
              </div>
            </div>
          </nav>

          {/* Right Section */}
          <div className="flex items-center gap-3">
            {/* Search Bar - Desktop */}
            <div className="header-search hidden w-52 items-center rounded-full border border-[#1c1815]/10 bg-[#f3efe8] px-4 py-2.5 transition-all focus-within:border-[#8b5d27]/50 focus-within:bg-white xl:flex">
              <Search className="mr-2.5 h-4 w-4 text-[#8b5d27]" />
              <input type="text" placeholder="Tìm sản phẩm..." className="w-full border-none bg-transparent text-sm text-[#151715] outline-none placeholder:text-[#151715]/35" />
            </div>

            {profile ? (
              <div className="flex items-center gap-2 md:gap-3">
                {/* Wallet Balance */}
                <div className="hidden items-center gap-2 rounded-full border border-[#9A6A2F]/20 bg-[#9A6A2F]/8 px-4 py-2 lg:flex">
                  <Wallet className="w-4 h-4 text-[#9A6A2F]" />
                  <span className="text-sm font-bold text-[#2F2418]">
                    {walletBalance > 0 ? walletBalance.toLocaleString('vi-VN') : 0}đ
                  </span>
                </div>

                {/* Notifications */}
                <div className="relative group">
                  <button className="relative rounded-full border border-transparent p-2.5 text-[#2F2418]/60 transition-all hover:border-[#1c1815]/10 hover:bg-white/60 hover:text-[#9A6A2F]" aria-label="Thông báo">
                    <Bell className="w-5 h-5" />
                    {unreadCount > 0 && (
                      <span className="absolute top-1 right-1 bg-danger text-white text-[10px] w-4.5 h-4.5 min-w-[18px] min-h-[18px] flex items-center justify-center rounded-full font-bold ring-2 ring-white animate-pulse">
                        {unreadCount}
                      </span>
                    )}
                  </button>
                  <div className="absolute top-full right-0 pt-2 w-80 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 transform -translate-y-1 group-hover:translate-y-0 z-50">
                    <div className="overflow-hidden rounded-2xl border border-white/80 bg-[#faf7f1]/95 shadow-[0_24px_70px_rgba(28,24,21,0.14)] backdrop-blur-xl">
                      <div className="px-4 py-3 border-b border-[#2F2418]/10 flex justify-between items-center">
                        <h4 className="font-bold text-[#2F2418] text-sm">Thông báo</h4>
                        {unreadCount > 0 && <span className="text-xs bg-danger/10 text-danger px-2.5 py-0.5 rounded-full font-semibold">{unreadCount} mới</span>}
                      </div>
                      <ul className="max-h-72 overflow-y-auto scrollbar-thin divide-y divide-slate-50">
                        {notifications.length > 0 ? notifications.map(n => (
                          <li key={n.id} className={`p-4 hover:bg-white/5 transition-colors relative ${n.is_read ? 'opacity-60' : ''}`}>
                            {!n.is_read && <span className="absolute top-4 right-4 w-2 h-2 bg-primary rounded-full"></span>}
                            <p className={`text-sm font-semibold mb-1 pr-6 ${n.is_read ? 'text-[#2F2418]/45' : 'text-[#2F2418]'}`}>{n.title}</p>
                            <p className="text-xs text-[#2F2418]/50 leading-relaxed line-clamp-2 mb-2">{n.message}</p>
                            <Link
                              to={n.type === 'auction_won' ? `/invoices/${n.reference_id}/checkout` : n.type === 'auction_failed' ? `/auctions/create?relist_id=${n.reference_id}` : '/profile'}
                              onClick={() => {
                                if (!n.is_read) {
                                  authApi.markNotificationRead(n.id).then(() => {
                                    setNotifications(notifications.map(notif => notif.id === n.id ? { ...notif, is_read: true } : notif));
                                  });
                                }
                              }}
                              className="text-xs text-[#9A6A2F] font-semibold hover:underline"
                            >
                              Xem chi tiết →
                            </Link>
                          </li>
                        )) : (
                          <li className="p-8 text-center text-sm text-[#2F2418]/45">Chưa có thông báo mới</li>
                        )}
                      </ul>
                    </div>
                  </div>
                </div>

                {/* Avatar Dropdown */}
                <div className="relative group hidden md:block z-[60]">
                  <button className="flex items-center justify-center w-10 h-10 border border-[#9A6A2F]/40 bg-[#9A6A2F]/10 text-[#2F2418] font-bold text-sm hover:border-[#9A6A2F] transition-all cursor-pointer overflow-hidden rounded-full">
                    {profile?.avatarImage ? (
                        <img src={profile.avatarImage} alt="Avatar" className="w-full h-full object-cover" />
                    ) : displayName ? (
                        displayName.charAt(0).toUpperCase()
                    ) : (
                        <User className="w-4 h-4" />
                    )}
                  </button>
                  <div className="absolute top-full right-0 pt-2 w-64 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 transform -translate-y-1 group-hover:translate-y-0">
                    <div className="overflow-hidden rounded-2xl border border-white/80 bg-[#faf7f1]/95 p-1.5 shadow-[0_24px_70px_rgba(28,24,21,0.14)] backdrop-blur-xl">
                      <div className="p-4 border-b border-[#2F2418]/10 bg-[#9A6A2F]/10">
                        <p className="font-bold text-[#2F2418]">{displayName}</p>
                        <p className="text-xs text-[#2F2418]/50 mt-0.5">{displayEmail}</p>
                      </div>
                      <div className="py-1">
                        <Link to="/profile/personal" className="flex items-center gap-3 px-4 py-2.5 text-sm text-[#2F2418]/72 hover:bg-[#9A6A2F]/10 hover:text-[#9A6A2F] transition-colors">
                          <User className="w-4 h-4" /> Hồ sơ cá nhân
                        </Link>
                        <Link to="/profile/wallet" className="flex items-center gap-3 px-4 py-2.5 text-sm text-[#2F2418]/72 hover:bg-[#9A6A2F]/10 hover:text-[#9A6A2F] transition-colors">
                          <Wallet className="w-4 h-4" /> Ví của tôi
                        </Link>
                        <Link to="/profile/orders" className="flex items-center gap-3 px-4 py-2.5 text-sm text-[#2F2418]/72 hover:bg-[#9A6A2F]/10 hover:text-[#9A6A2F] transition-colors">
                          <Package className="w-4 h-4" /> Quản lý Đơn hàng
                        </Link>
                        <Link to="/profile/support" className="flex items-center gap-3 px-4 py-2.5 text-sm text-[#2F2418]/72 hover:bg-[#9A6A2F]/10 hover:text-[#9A6A2F] transition-colors">
                          <Headphones className="w-4 h-4" /> Trung tâm hỗ trợ
                        </Link>
                        <Link to="/profile/settings" className="flex items-center gap-3 px-4 py-2.5 text-sm text-[#2F2418]/72 hover:bg-[#9A6A2F]/10 hover:text-[#9A6A2F] transition-colors">
                          <Settings className="w-4 h-4" /> Cài đặt hệ thống
                        </Link>
                      </div>
                      <div className="border-t border-[#2F2418]/10 p-1">
                        <button onClick={handleLogout} className="flex items-center gap-3 w-full px-4 py-2.5 text-sm text-danger font-semibold hover:bg-red-50 transition-colors rounded-lg">
                          <LogOut className="w-4 h-4" /> Đăng xuất
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            ) : (
              <Link to="/login" className="hidden items-center gap-2 rounded-full bg-[#1c1815] px-5 py-2.5 text-xs font-bold uppercase tracking-[0.16em] text-white transition-all hover:-translate-y-0.5 hover:bg-[#8b5d27] md:flex">
                <User className="w-4 h-4" /> Đăng Nhập
              </Link>
            )}

            {/* Mobile Hamburger */}
            <button onClick={() => setMobileMenuOpen(!mobileMenuOpen)} className="rounded-full border border-[#1c1815]/10 bg-[#eef0eb] p-2 text-[#1c1815]/70 transition-colors hover:bg-white hover:text-[#8b5d27] lg:hidden" aria-label="Menu">
              {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>
          </div>
        </div>

        <button
          type="button"
          className={`header-help-pull ${helpOpen ? 'is-open' : ''}`}
          onClick={() => setHelpOpen(open => !open)}
          aria-expanded={helpOpen}
          aria-controls="header-help-panel"
          aria-label="Mở bảng hỗ trợ"
        >
          <svg ref={helpCordRef} className="header-help-cord" viewBox="0 0 44 60" preserveAspectRatio="none" aria-hidden="true">
            <path d="M22 0 C21 18 23 40 22 60" />
          </svg>
          <span ref={helpHandleRef} className="header-help-handle"><CircleHelp /></span>
        </button>

        <aside id="header-help-panel" className={`header-help-panel ${helpOpen ? 'is-open' : ''}`} aria-hidden={!helpOpen}>
          <button type="button" className="header-help-close" onClick={() => setHelpOpen(false)} aria-label="Đóng bảng hỗ trợ">
            <X />
          </button>
          <div className="header-help-icon"><CircleHelp /></div>
          <p>Curator Support</p>
          <h3>Bạn cần trợ giúp ư?</h3>
          <span>Đội ngũ hỗ trợ sẵn sàng đồng hành trong quá trình tạo phiên, đặt giá và thanh toán.</span>
          <div>
            <Link to="/profile/support">Liên hệ ngay <ArrowRight /></Link>
            <a href="mailto:support@thecurator.vn"><Mail /> Gửi email</a>
          </div>
        </aside>
      </header>

      {/* Mobile Drawer */}
      {mobileMenuOpen && (
        <>
          <div className="fixed inset-0 bg-black/70 backdrop-blur-sm z-40 lg:hidden" onClick={() => setMobileMenuOpen(false)} />
          <div className="fixed bottom-0 right-0 top-[72px] z-50 w-80 max-w-[85vw] animate-slide-in-right overflow-y-auto border-l border-[#9A6A2F]/20 bg-[#FFF8ED] shadow-[0_25px_80px_rgba(47,36,24,0.18)] md:top-[78px] lg:hidden">
            {profile && (
              <div className="p-5 border-b border-[#2F2418]/10 bg-[#9A6A2F]/10">
                <div className="flex items-center gap-3 mb-3">
                  <div className="w-11 h-11 border border-[#9A6A2F]/40 bg-[#FFF8ED] text-[#2F2418] font-bold flex items-center justify-center text-lg overflow-hidden rounded-full">
                    {profile?.avatarImage ? (
                        <img src={profile.avatarImage} alt="Avatar" className="w-full h-full object-cover" />
                    ) : displayName ? (
                        displayName.charAt(0).toUpperCase()
                    ) : (
                        <User className="w-5 h-5" />
                    )}
                  </div>
                  <div>
                    <p className="font-bold text-[#2F2418]">{displayName}</p>
                    <p className="text-xs text-[#2F2418]/50">{displayEmail}</p>
                  </div>
                </div>
                <div className="flex items-center gap-2 bg-black/20 px-3 py-2 text-sm border border-[#9A6A2F]/20">
                  <Wallet className="w-4 h-4 text-[#9A6A2F]" />
                  <span className="font-bold text-[#2F2418]">{walletBalance.toLocaleString('vi-VN')} đ</span>
                </div>
              </div>
            )}
            <nav className="p-3 space-y-1">
              <button type="button" onClick={handleGoHome} className="flex items-center gap-3 px-4 py-3 text-sm font-medium text-[#2F2418]/72 hover:bg-[#9A6A2F]/10 hover:text-[#9A6A2F] transition-colors cursor-pointer w-full text-left">
                <Home className="w-4.5 h-4.5 text-[#9A6A2F]/60" /> Trang Chủ
              </button>
              <button type="button" onClick={handleGoAuctionFloor} className="flex items-center gap-3 px-4 py-3 text-sm font-medium text-[#2F2418]/72 hover:bg-[#9A6A2F]/10 hover:text-[#9A6A2F] transition-colors cursor-pointer w-full text-left">
                <Gavel className="w-4.5 h-4.5 text-[#9A6A2F]/60" /> Sàn Đấu Giá
              </button>
              <Link to="/auctions/create" className="flex items-center gap-3 px-4 py-3 text-sm font-medium text-[#2F2418]/72 hover:bg-[#9A6A2F]/10 hover:text-[#9A6A2F] transition-colors">
                <PlusCircle className="w-4.5 h-4.5 text-[#9A6A2F]/60" /> Tạo Phiên Đấu Giá
              </Link>
              {profile && (
                <>
                  <div className="border-t border-[#2F2418]/10 my-2" />
                  <Link to="/profile/personal" className="flex items-center gap-3 px-4 py-3 text-sm font-medium text-[#2F2418]/72 hover:bg-[#9A6A2F]/10 hover:text-[#9A6A2F] transition-colors">
                    <User className="w-4.5 h-4.5 text-[#9A6A2F]/60" /> Hồ sơ cá nhân
                  </Link>
                  <Link to="/profile/wallet" className="flex items-center gap-3 px-4 py-3 text-sm font-medium text-[#2F2418]/72 hover:bg-[#9A6A2F]/10 hover:text-[#9A6A2F] transition-colors">
                    <Wallet className="w-4.5 h-4.5 text-[#9A6A2F]/60" /> Ví của tôi
                  </Link>
                  <Link to="/profile/orders" className="flex items-center gap-3 px-4 py-3 text-sm font-medium text-[#2F2418]/72 hover:bg-[#9A6A2F]/10 hover:text-[#9A6A2F] transition-colors">
                    <Package className="w-4.5 h-4.5 text-[#9A6A2F]/60" /> Quản lý Đơn hàng
                  </Link>
                  <Link to="/profile/support" className="flex items-center gap-3 px-4 py-3 text-sm font-medium text-[#2F2418]/72 hover:bg-[#9A6A2F]/10 hover:text-[#9A6A2F] transition-colors">
                    <Headphones className="w-4.5 h-4.5 text-[#9A6A2F]/60" /> Trung tâm hỗ trợ
                  </Link>
                  <Link to="/profile/settings" className="flex items-center gap-3 px-4 py-3 text-sm font-medium text-[#2F2418]/72 hover:bg-[#9A6A2F]/10 hover:text-[#9A6A2F] transition-colors">
                    <Settings className="w-4.5 h-4.5 text-[#9A6A2F]/60" /> Cài đặt hệ thống
                  </Link>
                  <div className="border-t border-[#2F2418]/10 my-2" />
                  <button onClick={handleLogout} className="flex items-center gap-3 px-4 py-3 text-sm font-medium text-danger hover:bg-red-50 rounded-xl transition-colors w-full text-left">
                    <LogOut className="w-4.5 h-4.5" /> Đăng Xuất
                  </button>
                </>
              )}
              {!profile && (
                <>
                  <div className="border-t border-[#2F2418]/10 my-2" />
                  <Link to="/login" className="flex items-center gap-3 px-4 py-3 text-sm font-medium text-[#9A6A2F] hover:bg-[#9A6A2F]/10 transition-colors">
                    <User className="w-4.5 h-4.5" /> Đăng Nhập / Đăng Ký
                  </Link>
                </>
              )}
            </nav>
          </div>
        </>
      )}

      {/* Main Content */}
      <main className="flex-grow bg-[#F8F1E6] pt-[72px] md:pt-[78px]">
        <Outlet />
      </main>

      {/* Footer */}
      <footer className="mt-24 border-t border-[#1c1815]/10 bg-[#eee7dc] text-[#1c1815]">
        <div className="mx-auto max-w-[94rem] px-6 py-16 md:px-10 md:py-20">
          <div className="grid gap-12 lg:grid-cols-12 lg:gap-8">
            <div className="lg:col-span-4">
              <button type="button" onClick={handleGoHome} className="flex items-center gap-3 text-left">
                <img src="/brand/curator-mark.svg" alt="" className="h-11 w-11 rounded-xl bg-[#dfcfb8] p-1.5" />
                <span>
                  <strong className="block text-base uppercase tracking-[0.12em]">The Curator</strong>
                  <small className="mt-1 block text-[7px] font-bold uppercase tracking-[0.24em] text-[#8b5d27]">Online Auction Platform</small>
                </span>
              </button>
              <p className="mt-6 max-w-sm text-sm leading-7 text-[#6f655c]">
                Nền tảng đấu giá trực tuyến đa lĩnh vực, kết nối người mua và người bán bằng trải nghiệm minh bạch, an toàn.
              </p>
              <div className="mt-7 flex gap-2">
                {[Instagram, Facebook, Twitter].map((Icon, index) => (
                  <a key={index} href="/" aria-label="Mạng xã hội" className="grid h-10 w-10 place-items-center rounded-full border border-[#1c1815]/10 bg-[#faf7f1]/65 text-[#6f655c] transition hover:border-[#8b5d27]/40 hover:bg-[#faf7f1] hover:text-[#8b5d27]">
                    <Icon className="h-4 w-4" />
                  </a>
                ))}
              </div>
            </div>

            <div className="lg:col-span-2">
              <h4 className="text-[10px] font-bold uppercase tracking-[0.2em] text-[#8b5d27]">Khám phá</h4>
              <ul className="mt-5 space-y-3 text-sm text-[#6f655c]">
                <li><button type="button" onClick={handleGoAuctionFloor} className="transition hover:text-[#1c1815]">Phiên đang mở</button></li>
                <li><button type="button" onClick={handleGoActiveAuctions} className="transition hover:text-[#1c1815]">Sản phẩm đấu giá</button></li>
                <li><Link to="/auctions/create" className="transition hover:text-[#1c1815]">Tạo phiên mới</Link></li>
              </ul>
            </div>

            <div className="lg:col-span-2">
              <h4 className="text-[10px] font-bold uppercase tracking-[0.2em] text-[#8b5d27]">Hỗ trợ</h4>
              <ul className="mt-5 space-y-3 text-sm text-[#6f655c]">
                <li><Link to="/profile/support" className="transition hover:text-[#1c1815]">Trung tâm hỗ trợ</Link></li>
                <li><Link to="/profile/orders" className="transition hover:text-[#1c1815]">Quản lý giao dịch</Link></li>
                <li><a href="mailto:support@thecurator.vn" className="transition hover:text-[#1c1815]">Liên hệ The Curator</a></li>
              </ul>
            </div>

            <div className="lg:col-span-4">
              <p className="text-[10px] font-bold uppercase tracking-[0.2em] text-[#8b5d27]">Không bỏ lỡ phiên mới</p>
              <h3 className="mt-3 text-2xl font-bold tracking-[-0.04em] text-[#1c1815]">Nhận cập nhật đấu giá mỗi tuần.</h3>
              <div className="mt-6 flex overflow-hidden rounded-full border border-[#1c1815]/12 bg-[#faf7f1]/75 p-1.5 focus-within:border-[#8b5d27]/40">
                <Mail className="ml-3 h-4 w-4 shrink-0 self-center text-[#8b5d27]" />
                <input className="min-w-0 flex-1 bg-transparent px-3 text-sm outline-none placeholder:text-[#6f655c]/55" placeholder="Email của bạn" type="email" />
                <button className="grid h-10 w-10 shrink-0 place-items-center rounded-full bg-[#1c1815] text-white transition hover:bg-[#8b5d27]" aria-label="Đăng ký nhận tin">
                  <ArrowRight className="h-4 w-4" />
                </button>
              </div>
            </div>
          </div>

          <div className="mt-14 grid gap-px overflow-hidden rounded-2xl border border-[#1c1815]/10 bg-[#1c1815]/10 sm:grid-cols-3">
            {[
              [LockKeyhole, 'Thanh toán bảo vệ', 'Escrow cho mọi giao dịch'],
              [ShieldCheck, 'Tài khoản xác thực', 'Thông tin người dùng rõ ràng'],
              [Headphones, 'Hỗ trợ xuyên suốt', 'Đồng hành trước và sau phiên'],
            ].map(([Icon, title, copy]) => (
              <div key={title} className="flex items-center gap-3 bg-[#faf7f1] px-5 py-4">
                <Icon className="h-5 w-5 shrink-0 text-[#8b5d27]" />
                <span><strong className="block text-xs">{title}</strong><small className="mt-1 block text-[10px] text-[#6f655c]">{copy}</small></span>
              </div>
            ))}
          </div>
        </div>

        <div className="border-t border-[#1c1815]/10">
          <div className="mx-auto flex max-w-[94rem] flex-col gap-4 px-6 py-5 text-[11px] text-[#6f655c] md:flex-row md:items-center md:justify-between md:px-10">
            <div className="flex items-center gap-3">
              <span>© 2026 The Curator</span>
              <span className="rounded border border-[#1c1815]/15 px-1.5 py-0.5 text-[9px] font-bold text-[#1c1815]">18+</span>
              <span className="hidden sm:inline">Nền tảng dành cho người đủ 18 tuổi.</span>
            </div>
            <div className="flex gap-5">
              <Link to="/">Điều khoản</Link><Link to="/">Quyền riêng tư</Link><Link to="/profile/support">Liên hệ</Link>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default MainLayout;
