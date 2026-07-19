import React, { useEffect } from 'react';
import { Outlet, useNavigate, useLocation, Link } from 'react-router-dom';
import useAuthStore from '@/store/useAuthStore';
import { LayoutDashboard, Users, ShoppingBag, FolderOpen, Scale, Bell, Settings, LogOut, ShieldAlert } from 'lucide-react';
export default function AdminLayout() {
    const { user, logout } = useAuthStore();
    const navigate = useNavigate();
    const location = useLocation();

    if (!user) return null;

    const navItems = [
        { name: 'Dashboard', path: '/admin', icon: LayoutDashboard },
        { name: 'Người dùng', path: '/admin/users', icon: Users },
        { name: 'Đấu giá', path: '/admin/auctions', icon: Scale },
        { name: 'Danh mục', path: '/admin/categories', icon: FolderOpen },
        { name: 'Khiếu nại', path: '/admin/disputes', icon: ShieldAlert },
        { name: 'Thông báo', path: '/admin/notifications', icon: Bell },
        { name: 'Cài đặt', path: '/admin/settings', icon: Settings },
    ];

    const handleLogout = () => {
        logout();
        navigate('/login');
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
