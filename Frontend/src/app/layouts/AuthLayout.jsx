// ============================================================
// src/layouts/AuthLayout.jsx
// Layout tối giản cho trang đăng nhập / đăng ký
// ============================================================
import { Outlet, Link, useLocation } from 'react-router-dom';

const AuthLayout = () => {
  const { pathname } = useLocation();

  if (pathname === '/login') return <Outlet />;

  return (
    <div className="min-h-screen bg-[#F8F1E6] flex flex-col">
      {/* Minimal Header */}
      <header className="fixed top-0 left-0 right-0 z-50 border-b border-[#9A6A2F]/20 bg-[#F8F1E6]/95 backdrop-blur-sm">
        <div className="max-w-7xl mx-auto px-4 h-16 flex items-center">
          <Link to="/" className="group flex items-center gap-2.5">
            <img src="/brand/curator-mark.svg" alt="" className="h-9 w-9 transition-transform duration-300 group-hover:scale-105" />
            <span className="flex flex-col items-start leading-none">
              <span className="font-serif text-base font-semibold uppercase tracking-[0.14em] text-[#2F2418]">The Curator</span>
              <span className="mt-1 text-[7px] font-semibold uppercase tracking-[0.25em] text-[#9A6A2F]">Antique Auction House</span>
            </span>
          </Link>
        </div>
      </header>

      {/* Page Content */}
      <main className="flex-grow pt-16 flex items-center justify-center px-4">
        <Outlet />
      </main>

      {/* Minimal Footer */}
      <footer className="border-t border-[#9A6A2F]/15 py-4 flex flex-col items-center justify-center gap-2">
        <div className="text-xs text-[#2F2418]/40">© 2026 The Curator. Antique Auction House.</div>
        <div className="flex items-center gap-2 text-[10px] text-[#2F2418]/40">
          <span className="px-1.5 py-0.5 border border-red-800/20 bg-red-900/5 font-bold text-red-800 rounded-sm">18+</span>
          <span>Nền tảng đấu giá dành cho người từ đủ 18 tuổi trở lên.</span>
        </div>
      </footer>
    </div>
  );
};

export default AuthLayout;
