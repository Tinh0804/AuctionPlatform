// ============================================================
// src/layouts/AuthLayout.jsx
// Layout tối giản cho trang đăng nhập / đăng ký
// ============================================================
import { Outlet, Link } from 'react-router-dom';
import { Gavel } from 'lucide-react';

const AuthLayout = () => {
  return (
    <div className="min-h-screen bg-[#F8F1E6] flex flex-col">
      {/* Minimal Header */}
      <header className="fixed top-0 left-0 right-0 z-50 border-b border-[#9A6A2F]/20 bg-[#F8F1E6]/95 backdrop-blur-sm">
        <div className="max-w-7xl mx-auto px-4 h-16 flex items-center">
          <Link to="/" className="flex items-center gap-2.5 group">
            <div className="w-9 h-9 border border-[#9A6A2F]/50 bg-[#FFF8ED] flex items-center justify-center">
              <Gavel className="w-4 h-4 text-[#9A6A2F]" />
            </div>
            <span className="font-serif text-lg tracking-[0.16em] text-[#2F2418] uppercase">
              The Curator
            </span>
          </Link>
        </div>
      </header>

      {/* Page Content */}
      <main className="flex-grow pt-16 flex items-center justify-center px-4">
        <Outlet />
      </main>

      {/* Minimal Footer */}
      <footer className="border-t border-[#9A6A2F]/15 py-4 text-center text-xs text-[#2F2418]/40">
        © 2026 The Curator. Antique Auction House.
      </footer>
    </div>
  );
};

export default AuthLayout;
