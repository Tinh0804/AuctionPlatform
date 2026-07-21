import React, { useEffect, useState, useRef, useCallback } from 'react';
import { Outlet, useNavigate, useLocation, Link } from 'react-router-dom';
import useAuthStore from '@/store/useAuthStore';
import { adminApi } from '@/features/admin/api';
import { useStomp } from '@/hooks/useStomp';
import { LayoutDashboard, Users, ShoppingBag, FolderOpen, Scale, Bell, Settings, LogOut, ShieldAlert, CheckCheck } from 'lucide-react';

export default function AdminLayout() {
    const { user, logout } = useAuthStore();
    const navigate = useNavigate();
    const location = useLocation();

    // ── Notification state ──
    const [notifications, setNotifications] = useState([]);
    const [unreadCount, setUnreadCount] = useState(0);
    const [showDropdown, setShowDropdown] = useState(false);
    const dropdownRef = useRef(null);

    if (!user) return null;

    const profile = user?.profile || user;
    const adminUserId = profile?.id;

    const navItems = [
        { name: 'Dashboard', path: '/admin', icon: LayoutDashboard },
        { name: 'Người dùng', path: '/admin/users', icon: Users },
        { name: 'Đấu giá', path: '/admin/auctions', icon: Scale },
        { name: 'Danh mục', path: '/admin/categories', icon: FolderOpen },
        { name: 'Đơn hàng', path: '/admin/orders', icon: ShoppingBag },
        { name: 'Khiếu nại', path: '/admin/disputes', icon: ShieldAlert },
        { name: 'Thông báo', path: '/admin/notifications', icon: Bell },
        { name: 'Cài đặt', path: '/admin/settings', icon: Settings },
    ];

    // ── Fetch admin's own notifications ──
    const fetchNotifications = useCallback(async () => {
        try {
            const [notifRes, countRes] = await Promise.all([
                adminApi.getAdminNotifications(),
                adminApi.getUnreadCount(),
            ]);
            const notifList = notifRes.result || notifRes || [];
            setNotifications(Array.isArray(notifList) ? notifList.slice(0, 5) : []);
            setUnreadCount(countRes.result ?? countRes ?? 0);
        } catch (err) {
            console.error('Failed to fetch admin notifications', err);
        }
    }, []);

    // Initial fetch
    useEffect(() => {
        fetchNotifications();
    }, [fetchNotifications]);

    // Polling every 30s
    useEffect(() => {
        const interval = setInterval(fetchNotifications, 30000);
        return () => clearInterval(interval);
    }, [fetchNotifications]);

    // ── WebSocket realtime ──
    useStomp({
        deps: [adminUserId],
        onConnect: (stompClient) => {
            if (!adminUserId) return;
            stompClient.subscribe(`/topic/notification/${adminUserId}`, (messageOutput) => {
                try {
                    const incoming = JSON.parse(messageOutput.body);
                    if (!incoming?.title) return;

                    setNotifications(prev => {
                        if (incoming.id && prev.some(n => n.id === incoming.id)) return prev;
                        return [{
                            id: incoming.id || `${Date.now()}`,
                            title: incoming.title,
                            message: incoming.message,
                            type: incoming.type,
                            isRead: false,
                            createdAt: incoming.createdAt || new Date().toISOString(),
                        }, ...prev].slice(0, 5);
                    });
                    setUnreadCount(prev => prev + 1);
                } catch (e) {
                    console.error('Invalid notification payload', e);
                }
            });
        }
    });

    // ── Close dropdown on outside click ──
    useEffect(() => {
        const handleClickOutside = (e) => {
            if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
                setShowDropdown(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    // ── Mark all as read ──
    const handleMarkAllRead = async () => {
        try {
            await adminApi.markAllNotificationsRead();
            setUnreadCount(0);
            setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
        } catch (err) {
            console.error('Mark all read failed', err);
        }
    };

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const formatTimeAgo = (dateStr) => {
        if (!dateStr) return '';
        const diff = Date.now() - new Date(dateStr).getTime();
        const mins = Math.floor(diff / 60000);
        if (mins < 1) return 'Vừa xong';
        if (mins < 60) return `${mins} phút trước`;
        const hours = Math.floor(mins / 60);
        if (hours < 24) return `${hours} giờ trước`;
        const days = Math.floor(hours / 24);
        return `${days} ngày trước`;
    };

    return (
        <div className="flex h-screen bg-gray-50">
            {/* Sidebar */}
            <div className="w-64 bg-white border-r border-gray-200 flex flex-col">
                <div className="h-16 flex items-center px-6 border-b border-gray-200">
                    <img className="w-10 h-10 mr-2" src="/favicon.svg" alt="Logo" />
                    <span className="text-xl font-black tracking-tighter text-[#111111]">AUCTION</span>
                </div>
                <nav className="flex-1 overflow-y-auto py-4">
                    <ul className="space-y-1 px-3">
                        {navItems.map(item => {
                            const Icon = item.icon;
                            const isActive = location.pathname === item.path || (item.path !== '/admin' && location.pathname.startsWith(item.path));
                            return (
                                <li key={item.path}>
                                    <Link
                                        to={item.path}
                                        className={`flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                                            isActive 
                                                ? 'bg-[#111111] text-white' 
                                                : 'text-gray-600 hover:bg-gray-100 hover:text-[#111111]'
                                        }`}
                                    >
                                        <Icon className={`h-5 w-5 ${isActive ? 'text-white' : 'text-gray-400'}`} />
                                        {item.name}
                                        {/* Badge for notification sidebar item */}
                                        {item.path === '/admin/notifications' && unreadCount > 0 && (
                                            <span className="ml-auto bg-red-500 text-white text-[10px] font-bold rounded-full min-w-[18px] h-[18px] flex items-center justify-center px-1">
                                                {unreadCount > 99 ? '99+' : unreadCount}
                                            </span>
                                        )}
                                    </Link>
                                </li>
                            );
                        })}
                    </ul>
                </nav>
                <div className="p-4 border-t border-gray-200">
                    <button
                        onClick={handleLogout}
                        className="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-red-600 hover:bg-red-50 w-full transition-colors"
                    >
                        <LogOut className="h-5 w-5" />
                        Đăng xuất
                    </button>
                </div>
            </div>

            {/* Main Content */}
            <div className="flex-1 flex flex-col overflow-hidden">
                <header className="h-16 bg-white border-b border-gray-200 flex items-center justify-between px-6">
                    <h1 className="text-xl font-bold text-gray-800">
                        {navItems.find(i => location.pathname === i.path || (i.path !== '/admin' && location.pathname.startsWith(i.path)))?.name || 'Admin'}
                    </h1>
                    <div className="flex items-center gap-4">
                        {/* ── Notification Bell ── */}
                        <div className="relative" ref={dropdownRef}>
                            <button
                                onClick={() => setShowDropdown(!showDropdown)}
                                className="relative p-2 rounded-lg hover:bg-gray-100 transition-colors"
                            >
                                <Bell className="w-5 h-5 text-gray-600" />
                                {unreadCount > 0 && (
                                    <span className="absolute -top-0.5 -right-0.5 bg-red-500 text-white text-[10px] font-bold rounded-full min-w-[18px] h-[18px] flex items-center justify-center px-1 ring-2 ring-white">
                                        {unreadCount > 99 ? '99+' : unreadCount}
                                    </span>
                                )}
                            </button>

                            {/* Dropdown */}
                            {showDropdown && (
                                <div className="absolute right-0 top-full mt-2 w-80 bg-white rounded-xl shadow-2xl border border-gray-200 overflow-hidden z-50">
                                    <div className="flex items-center justify-between px-4 py-3 border-b border-gray-100">
                                        <span className="text-sm font-bold text-gray-900">Thông báo</span>
                                        {unreadCount > 0 && (
                                            <button
                                                onClick={handleMarkAllRead}
                                                className="flex items-center gap-1 text-xs text-blue-600 hover:text-blue-800 font-medium"
                                            >
                                                <CheckCheck className="w-3.5 h-3.5" />
                                                Đọc tất cả
                                            </button>
                                        )}
                                    </div>
                                    <div className="max-h-80 overflow-y-auto">
                                        {notifications.length > 0 ? notifications.map(n => (
                                            <div
                                                key={n.id}
                                                className={`px-4 py-3 border-b border-gray-50 hover:bg-gray-50 transition-colors cursor-pointer ${!n.isRead ? 'bg-blue-50/40' : ''}`}
                                                onClick={() => {
                                                    if (!n.isRead) {
                                                        adminApi.markNotificationRead(n.id).then(() => {
                                                            setNotifications(prev => prev.map(item => item.id === n.id ? { ...item, isRead: true } : item));
                                                            setUnreadCount(prev => Math.max(0, prev - 1));
                                                        });
                                                    }
                                                }}
                                            >
                                                <div className="flex items-start gap-3">
                                                    <div className={`mt-0.5 w-2 h-2 rounded-full flex-shrink-0 ${n.isRead ? 'bg-transparent' : 'bg-blue-500'}`} />
                                                    <div className="flex-1 min-w-0">
                                                        <p className="text-sm font-medium text-gray-900 truncate">{n.title}</p>
                                                        <p className="text-xs text-gray-500 truncate mt-0.5">{n.message}</p>
                                                        <p className="text-[10px] text-gray-400 mt-1">{formatTimeAgo(n.createdAt)}</p>
                                                    </div>
                                                </div>
                                            </div>
                                        )) : (
                                            <div className="py-8 text-center text-sm text-gray-400">
                                                Không có thông báo
                                            </div>
                                        )}
                                    </div>
                                    <Link
                                        to="/admin/notifications"
                                        onClick={() => setShowDropdown(false)}
                                        className="block px-4 py-2.5 text-center text-xs font-medium text-blue-600 hover:bg-gray-50 border-t border-gray-100 transition-colors"
                                    >
                                        Xem tất cả thông báo →
                                    </Link>
                                </div>
                            )}
                        </div>

                        {/* User avatar */}
                        <div className="flex items-center gap-3">
                            <div className="w-8 h-8 rounded-full bg-gray-200 flex items-center justify-center text-sm font-bold text-gray-600">
                                {user.name?.charAt(0) || 'A'}
                            </div>
                            <span className="text-sm font-medium text-gray-700">{user.name || 'Admin'}</span>
                        </div>
                    </div>
                </header>
                <main className="flex-1 overflow-y-auto p-6">
                    <Outlet />
                </main>
            </div>
        </div>
    );
}
