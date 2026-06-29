import { Settings } from 'lucide-react';

export default function SettingsPage() {
    return (
        <div className="bg-[#FFF8ED] border border-[#9A6A2F]/20 p-8 shadow-[0_28px_90px_rgba(47,36,24,0.10)] animate-fade-in text-center min-h-[400px] flex flex-col items-center justify-center">
            <Settings className="w-16 h-16 text-[#9A6A2F]/40 mb-4 animate-spin-slow" />
            <h3 className="font-serif text-2xl text-[#2F2418] mb-2">Cài Đặt Hệ Thống</h3>
            <p className="text-[#2F2418]/60">Tính năng đang được phát triển. Bạn sẽ có thể thay đổi giao diện sáng tối, nhận thông báo, và ngôn ngữ tại đây.</p>
        </div>
    );
}
