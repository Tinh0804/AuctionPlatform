// ============================================================
// src/pages/NotFound.jsx
// Trang 404 mặc định
// ============================================================
import { Link } from 'react-router-dom';
import { Gavel, ArrowLeft } from 'lucide-react';

const NotFound = () => {
  return (
    <div className="min-h-[60vh] flex flex-col items-center justify-center text-center px-4 py-20">
      <div className="w-20 h-20 border-2 border-[#9A6A2F]/40 bg-[#FFF8ED] flex items-center justify-center mb-6">
        <Gavel className="w-9 h-9 text-[#9A6A2F]/50" />
      </div>
      <h1 className="font-serif text-6xl font-bold text-[#2F2418]/20 mb-4 tracking-[0.08em]">404</h1>
      <h2 className="text-xl font-bold text-[#2F2418] mb-3">Trang không tồn tại</h2>
      <p className="text-[#2F2418]/55 mb-8 max-w-sm leading-relaxed">
        Hiện vật bạn tìm kiếm đã không còn ở đây, hoặc đường dẫn này không hợp lệ.
      </p>
      <Link
        to="/"
        className="inline-flex items-center gap-2 bg-[#9A6A2F] text-[#F8F1E6] px-6 py-3 text-sm font-bold uppercase tracking-[0.14em] hover:bg-[#2F2418] transition-colors"
      >
        <ArrowLeft className="w-4 h-4" />
        Về trang chủ
      </Link>
    </div>
  );
};

export default NotFound;
