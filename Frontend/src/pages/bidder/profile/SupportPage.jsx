import { Headphones } from 'lucide-react';

export default function SupportPage() {
    return (
        <div className="bg-[#FFF8ED] border border-[#9A6A2F]/20 p-8 shadow-[0_28px_90px_rgba(47,36,24,0.10)] animate-fade-in text-center min-h-[400px] flex flex-col items-center justify-center">
            <Headphones className="w-16 h-16 text-[#9A6A2F]/40 mb-4" />
            <h3 className="font-serif text-2xl text-[#2F2418] mb-2">Trung Tâm Hỗ Trợ</h3>
            <p className="text-[#2F2418]/60 max-w-md mx-auto mb-6">Bạn có gặp vấn đề gì không? Xin vui lòng liên hệ bộ phận hỗ trợ khách hàng thông qua Hotline hoặc Email để được giải quyết nhanh nhất.</p>
            <div className="flex gap-4">
                <a href="mailto:support@thecurator.vn" className="px-6 py-2.5 bg-[#9A6A2F] text-white font-bold text-sm">Gửi Email</a>
                <a href="tel:19001234" className="px-6 py-2.5 border border-[#9A6A2F] text-[#9A6A2F] font-bold text-sm hover:bg-[#9A6A2F]/5">Gọi 1900 1234</a>
            </div>
        </div>
    );
}
