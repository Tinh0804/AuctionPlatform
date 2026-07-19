// ============================================================
// src/layouts/MainLayout.jsx
// Layout chính: Header + Footer + <Outlet />
// ============================================================
import { useEffect, useState } from 'react';
import { Link, Outlet, useNavigate, useLocation } from 'react-router-dom';
import {
  Search, User, Bell, Wallet, ChevronDown, LogOut, ShieldCheck,
  ClipboardList, Menu, X, Gavel, PlusCircle, Home, Package,
  ArrowRight, Instagram, Facebook, Twitter, Mail, LockKeyhole,
  Award, Gem, ChevronRight, Headphones, Settings,
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
  const [activeNavItem, setActiveNavItem] = useState(null);

  // Đóng mobile menu khi chuyển route
  useEffect(() => {
    setMobileMenuOpen(false);
  }, [location.pathname]);

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
      <header className="site-header fixed top-0 left-0 right-0 z-50 border-b bg-[#F8F1E6] border-[#9A6A2F]/30 shadow-[0_18px_60px_rgba(47,36,24,0.12)]">
        <div className="max-w-7xl mx-auto px-4 md:px-6 h-16 md:h-[76px] flex items-center justify-between">
          {/* Logo */}
          <button type="button" onClick={handleGoHome} className="site-logo flex items-center gap-2.5 shrink-0 group cursor-pointer bg-transparent border-none p-0">
            <div className="site-logo-mark w-10 h-10 border border-[#9A6A2F]/55 bg-[#FFF8ED] flex items-center justify-center shadow-[0_0_30px_rgba(154,106,47,0.12)] group-hover:border-[#9A6A2F] transition-all">
              <Gavel className="w-5 h-5 text-[#9A6A2F]" />
            </div>
            <span className="hidden sm:flex flex-col items-start leading-none">
              <span className="font-serif text-[1.35rem] tracking-[0.16em] text-[#2F2418] uppercase">The Curator</span>
              <span className="mt-1 text-[9px] tracking-[0.34em] uppercase text-[#9A6A2F]/80">Antique Auction House</span>
            </span>
          </button>

          {/* Main Navigation - Desktop */}
          <nav className="site-nav hidden lg:flex flex-nowrap items-center gap-1 mx-4 xl:mx-8 shrink-0 whitespace-nowrap border border-[#2F2418]/10 bg-white/[0.03] px-1.5 py-1.5">
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
                <div className="bg-[#FFF8ED] shadow-[0_25px_80px_rgba(47,36,24,0.12)] border border-[#9A6A2F]/20 overflow-hidden">
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
                <div className="bg-[#FFF8ED] shadow-[0_25px_80px_rgba(47,36,24,0.12)] border border-[#9A6A2F]/20 overflow-hidden">
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
            <div className="hidden xl:flex items-center bg-white/[0.04] px-4 py-2.5 w-56 border border-[#2F2418]/10 focus-within:border-[#9A6A2F]/60 focus-within:bg-white/[0.07] transition-all">
              <Search className="w-4 h-4 text-[#9A6A2F]/70 mr-2.5" />
              <input type="text" placeholder="Tìm hiện vật..." className="bg-transparent border-none outline-none text-sm w-full text-[#2F2418] placeholder-[#2F2418]/35" />
            </div>

            {profile ? (
              <div className="flex items-center gap-2 md:gap-3">
                {/* Wallet Balance */}
                <div className="hidden lg:flex items-center gap-2 bg-[#9A6A2F]/10 px-4 py-2 border border-[#9A6A2F]/25">
                  <Wallet className="w-4 h-4 text-[#9A6A2F]" />
                  <span className="text-sm font-bold text-[#2F2418]">
                    {walletBalance > 0 ? walletBalance.toLocaleString('vi-VN') : 0}đ
                  </span>
                </div>

                {/* Notifications */}
                <div className="relative group">
                  <button className="relative p-2.5 text-[#2F2418]/60 hover:text-[#9A6A2F] hover:bg-white/5 transition-all" aria-label="Thông báo">
                    <Bell className="w-5 h-5" />
                    {unreadCount > 0 && (
                      <span className="absolute top-1 right-1 bg-danger text-white text-[10px] w-4.5 h-4.5 min-w-[18px] min-h-[18px] flex items-center justify-center rounded-full font-bold ring-2 ring-white animate-pulse">
                        {unreadCount}
                      </span>
                    )}
                  </button>
                  <div className="absolute top-full right-0 pt-2 w-80 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 transform -translate-y-1 group-hover:translate-y-0 z-50">
                    <div className="bg-[#FFF8ED] shadow-[0_25px_80px_rgba(47,36,24,0.12)] border border-[#9A6A2F]/20 overflow-hidden">
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
                    <div className="bg-[#FFF8ED] shadow-[0_25px_80px_rgba(47,36,24,0.12)] border border-[#9A6A2F]/20 overflow-hidden">
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
              <Link to="/login" className="hidden md:flex items-center gap-2 border border-[#9A6A2F] bg-[#9A6A2F] px-5 py-2.5 text-xs font-bold uppercase tracking-[0.18em] text-[#F8F1E6] hover:bg-[#2F2418] transition-colors">
                <User className="w-4 h-4" /> Đăng Nhập
              </Link>
            )}

            {/* Mobile Hamburger */}
            <button onClick={() => setMobileMenuOpen(!mobileMenuOpen)} className="lg:hidden p-2 text-[#2F2418]/72 hover:text-[#9A6A2F] hover:bg-white/5 transition-colors" aria-label="Menu">
              {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>
          </div>
        </div>
      </header>

      {/* Mobile Drawer */}
      {mobileMenuOpen && (
        <>
          <div className="fixed inset-0 bg-black/70 backdrop-blur-sm z-40 lg:hidden" onClick={() => setMobileMenuOpen(false)} />
          <div className="fixed top-16 md:top-[76px] right-0 bottom-0 w-80 max-w-[85vw] bg-[#FFF8ED] z-50 lg:hidden shadow-[0_25px_80px_rgba(47,36,24,0.18)] animate-slide-in-right overflow-y-auto border-l border-[#9A6A2F]/20">
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
      <main className="flex-grow bg-[#F8F1E6] pt-16 md:pt-[76px]">
        <Outlet />
      </main>

      {/* Footer */}
      <footer className="relative overflow-hidden bg-gradient-to-br from-[#E8D5B7] via-[#EFE2CF] to-[#F5E6D3] text-[#2F2418] border-t-2 border-[#9A6A2F]/25 mt-32">
        <div className="absolute inset-0 opacity-[0.15]" style={{ backgroundImage: `radial-gradient(circle at 20% 20%, rgba(154,106,47,0.25), transparent 40%), radial-gradient(circle at 80% 80%, rgba(154,106,47,0.15), transparent 40%)` }} />
        <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-transparent via-[#9A6A2F]/40 to-transparent" />
        <div className="relative max-w-[90rem] mx-auto px-6 md:px-12 py-16 md:py-24">
          <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-12 gap-y-12 gap-x-8 xl:gap-x-16">
            <div className="md:col-span-2 xl:col-span-4 xl:pr-6">
              <div className="flex items-center gap-3.5 mb-6">
                <div className="w-12 h-12 border-2 border-[#9A6A2F]/70 bg-[#FFF8ED] flex items-center justify-center">
                  <Gavel className="w-6 h-6 text-[#9A6A2F]" />
                </div>
                <div>
                  <p className="font-serif text-2xl tracking-[0.18em] uppercase text-[#2F2418] leading-none">The Curator</p>
                  <p className="text-[9px] tracking-[0.36em] uppercase text-[#9A6A2F] mt-1.5 font-semibold">Luxury Antique Auction House</p>
                </div>
              </div>
              <p className="text-[15px] text-[#2F2418]/70 leading-[1.8] max-w-sm mb-8">
                Không gian tuyển chọn hiện vật cổ cao cấp, kết nối nhà sưu tầm với những tác phẩm có provenance, giá trị và câu chuyện riêng.
              </p>
              <div className="flex items-center gap-3">
                {[Instagram, Facebook, Twitter].map((Icon, idx) => (
                  <a key={idx} href="/" className="w-11 h-11 border-2 border-[#9A6A2F]/30 bg-[#FFF8ED]/50 flex items-center justify-center text-[#2F2418]/60 hover:text-[#9A6A2F] hover:border-[#9A6A2F] transition-all duration-300" aria-label="Social link">
                    <Icon className="w-[18px] h-[18px]" />
                  </a>
                ))}
              </div>
            </div>
            <div className="min-w-0 xl:col-span-2">
              <h4 className="text-[11px] font-bold text-[#9A6A2F] uppercase tracking-[0.24em] mb-6 pb-2 border-b border-[#9A6A2F]/20">Đấu giá</h4>
              <ul className="space-y-3.5">
                <li><button type="button" onClick={handleGoAuctionFloor} className="text-[14px] text-[#2F2418]/65 hover:text-[#9A6A2F] hover:translate-x-1 transition-all duration-200 inline-flex items-center gap-2 group"><ChevronRight className="w-3.5 h-3.5 opacity-0 group-hover:opacity-100 transition-opacity" />Catalogue hiện tại</button></li>
                <li><Link to="/auctions/create" className="text-[14px] text-[#2F2418]/65 hover:text-[#9A6A2F] hover:translate-x-1 transition-all duration-200 inline-flex items-center gap-2 group"><ChevronRight className="w-3.5 h-3.5 opacity-0 group-hover:opacity-100 transition-opacity" />Gửi hiện vật thẩm định</Link></li>
                <li><Link to="/" className="text-[14px] text-[#2F2418]/65 hover:text-[#9A6A2F] hover:translate-x-1 transition-all duration-200 inline-flex items-center gap-2 group"><ChevronRight className="w-3.5 h-3.5 opacity-0 group-hover:opacity-100 transition-opacity" />Lịch phiên sắp tới</Link></li>
              </ul>
            </div>
            <div className="min-w-0 xl:col-span-2">
              <h4 className="text-[11px] font-bold text-[#9A6A2F] uppercase tracking-[0.24em] mb-6 pb-2 border-b border-[#9A6A2F]/20">Dịch vụ</h4>
              <ul className="space-y-3.5">
                <li><Link to="/" className="text-[14px] text-[#2F2418]/65 hover:text-[#9A6A2F] hover:translate-x-1 transition-all duration-200 inline-flex items-center gap-2 group"><ChevronRight className="w-3.5 h-3.5 opacity-0 group-hover:opacity-100 transition-opacity" />Xác thực provenance</Link></li>
                <li><Link to="/" className="text-[14px] text-[#2F2418]/65 hover:text-[#9A6A2F] hover:translate-x-1 transition-all duration-200 inline-flex items-center gap-2 group"><ChevronRight className="w-3.5 h-3.5 opacity-0 group-hover:opacity-100 transition-opacity" />Vận chuyển bảo hiểm</Link></li>
                <li><Link to="/" className="text-[14px] text-[#2F2418]/65 hover:text-[#9A6A2F] hover:translate-x-1 transition-all duration-200 inline-flex items-center gap-2 group"><ChevronRight className="w-3.5 h-3.5 opacity-0 group-hover:opacity-100 transition-opacity" />Tư vấn bộ sưu tập</Link></li>
              </ul>
            </div>
            <div className="md:col-span-2 xl:col-span-4 xl:pl-6">
              <h4 className="text-[11px] font-bold text-[#9A6A2F] uppercase tracking-[0.24em] mb-4">Private Viewing</h4>
              <p className="text-[14px] text-[#2F2418]/65 leading-relaxed mb-6">Nhận catalogue tuyển chọn và lời mời xem trước các phiên đấu giá kín.</p>
              <div className="flex border-2 border-[#9A6A2F]/30 bg-[#FFF8ED]/60 overflow-hidden">
                <div className="flex items-center pl-4 text-[#9A6A2F]/70"><Mail className="w-[18px] h-[18px]" /></div>
                <input className="min-w-0 flex-1 bg-transparent px-3.5 py-3.5 text-sm text-[#2F2418] placeholder-[#2F2418]/40 outline-none" placeholder="Email của bạn" />
                <button className="px-5 text-[#F8F1E6] bg-[#9A6A2F] hover:bg-[#2F2418] transition-all duration-300" aria-label="Subscribe">
                  <ArrowRight className="w-[18px] h-[18px]" />
                </button>
              </div>
              <div className="mt-6 grid grid-cols-3 gap-4">
                {[[LockKeyhole, 'Escrow'], [Award, 'Verified'], [Gem, 'Curated']].map(([Icon, label]) => (
                  <div key={label} className="flex flex-col items-center text-center">
                    <div className="w-10 h-10 bg-[#9A6A2F]/10 border border-[#9A6A2F]/30 flex items-center justify-center mb-2"><Icon className="w-4 h-4 text-[#9A6A2F]" /></div>
                    <span className="text-[10px] uppercase tracking-[0.12em] text-[#2F2418]/60 font-semibold">{label}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
        <div className="relative border-t-2 border-[#9A6A2F]/20 bg-[#E8D5B7]/50">
          <div className="max-w-7xl mx-auto px-6 md:px-12 py-6 flex flex-col md:flex-row gap-4 md:items-center md:justify-between">
            <div className="flex items-center gap-3">
              <span className="text-xs text-[#2F2418]/50 font-medium">© 2026 The Curator. Bảo lưu mọi quyền.</span>
              <span className="px-2 py-0.5 border border-red-800/30 bg-red-900/5 text-[10px] font-bold text-red-800 rounded-sm">18+</span>
              <span className="text-[10px] text-[#2F2418]/50 font-medium hidden md:inline">Nền tảng đấu giá dành cho người từ đủ 18 tuổi trở lên.</span>
            </div>
            <div className="flex gap-6 text-xs">
              <Link to="/" className="text-[#2F2418]/50 hover:text-[#9A6A2F] transition-colors font-medium">Terms</Link>
              <Link to="/" className="text-[#2F2418]/50 hover:text-[#9A6A2F] transition-colors font-medium">Privacy</Link>
              <Link to="/" className="text-[#2F2418]/50 hover:text-[#9A6A2F] transition-colors font-medium">Contact</Link>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default MainLayout;
