import { Link, useLocation, useParams } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { AlertTriangle, ArrowLeft, CheckCircle2, Clock, CreditCard, RefreshCw, XCircle } from 'lucide-react';
import { API_URL } from '../api/client';

const resultConfig = {
    success: {
        eyebrow: 'Thanh toán hoàn tất',
        title: 'Giao dịch thành công',
        message: 'Khoản thanh toán của bạn đã được ghi nhận. Bạn có thể quay lại hồ sơ để kiểm tra trạng thái đơn hàng hoặc số dư.',
        icon: CheckCircle2,
        iconClass: 'bg-emerald-500 text-white shadow-[0_18px_42px_rgba(16,185,129,0.28)]',
        accent: 'from-emerald-500/18 via-[#FFF8ED] to-amber-100/70',
    },
    failed: {
        eyebrow: 'Thanh toán chưa hoàn tất',
        title: 'Giao dịch MoMo thất bại',
        message: 'MoMo chưa xác nhận khoản thanh toán này. Bạn chưa bị ghi nhận giao dịch thành công trên hệ thống.',
        icon: XCircle,
        iconClass: 'bg-rose-500 text-white shadow-[0_18px_42px_rgba(244,63,94,0.25)]',
        accent: 'from-rose-500/16 via-[#FFF8ED] to-amber-100/70',
    },
    cancelled: {
        eyebrow: 'Thanh toán đã hủy',
        title: 'Bạn đã hủy giao dịch',
        message: 'Giao dịch chưa được xử lý. Bạn có thể thử thanh toán lại khi sẵn sàng.',
        icon: AlertTriangle,
        iconClass: 'bg-amber-500 text-white shadow-[0_18px_42px_rgba(245,158,11,0.25)]',
        accent: 'from-amber-500/18 via-[#FFF8ED] to-stone-100',
    },
    pending: {
        eyebrow: 'Đang kiểm tra thanh toán',
        title: 'Giao dịch đang chờ xác nhận',
        message: 'Hệ thống đang chờ phản hồi từ cổng thanh toán. Vui lòng kiểm tra lại sau ít phút.',
        icon: Clock,
        iconClass: 'bg-sky-500 text-white shadow-[0_18px_42px_rgba(14,165,233,0.24)]',
        accent: 'from-sky-500/16 via-[#FFF8ED] to-amber-100/70',
    },
};

export default function PaymentResult() {
    const { status = 'failed' } = useParams();
    const location = useLocation();
    const [backendSyncStatus, setBackendSyncStatus] = useState('idle');
    const params = new URLSearchParams(location.search);
    const ref = params.get('ref') || params.get('orderId') || params.get('requestId');
    const momoResultCode = params.get('resultCode');
    const resolvedStatus = momoResultCode !== null
        ? (momoResultCode === '0' ? 'success' : 'failed')
        : status;
    const config = resultConfig[resolvedStatus] || resultConfig.failed;
    const Icon = config.icon;

    useEffect(() => {
        if (!location.pathname.includes('/wallets/deposit/momo-return') || backendSyncStatus !== 'idle') {
            return;
        }

        const syncMomoReturn = async () => {
            setBackendSyncStatus('syncing');
            try {
                const response = await fetch(`${API_URL}/wallets/deposit/momo-return${location.search}`, {
                    headers: { 'ngrok-skip-browser-warning': 'true' },
                    redirect: 'manual',
                });
                setBackendSyncStatus(response.ok || response.type === 'opaqueredirect' ? 'synced' : 'failed');
            } catch (error) {
                console.error('Không thể đồng bộ callback MoMo về backend:', error);
                setBackendSyncStatus('failed');
            }
        };

        syncMomoReturn();
    }, [backendSyncStatus, location.pathname, location.search]);

    return (
        <section className="min-h-[82vh] bg-[radial-gradient(circle_at_20%_10%,rgba(154,106,47,0.18),transparent_28rem),linear-gradient(180deg,#F8F1E6,#FFF8ED)] px-4 py-16">
            <div className="mx-auto max-w-3xl">
                <div className={`relative overflow-hidden border border-[#2F2418]/10 bg-gradient-to-br ${config.accent} p-6 shadow-[0_32px_90px_rgba(47,36,24,0.13)] md:p-10`}>
                    <div className="absolute -right-16 -top-16 h-44 w-44 rounded-full bg-white/45 blur-3xl" />
                    <div className="absolute bottom-0 left-0 h-px w-full bg-gradient-to-r from-transparent via-[#9A6A2F]/35 to-transparent" />

                    <div className="relative flex flex-col gap-6 md:flex-row md:items-start">
                        <div className={`flex h-16 w-16 shrink-0 items-center justify-center rounded-full ${config.iconClass}`}>
                            <Icon className="h-8 w-8" />
                        </div>

                        <div className="min-w-0 flex-1">
                            <p className="text-xs font-bold uppercase tracking-[0.32em] text-[#9A6A2F]">{config.eyebrow}</p>
                            <h1 className="mt-3 font-serif text-4xl font-medium leading-tight text-[#2F2418] md:text-5xl">
                                {config.title}
                            </h1>
                            <p className="mt-4 max-w-2xl text-base leading-7 text-[#2F2418]/68">
                                {config.message}
                            </p>

                            {ref && (
                                <div className="mt-6 border border-[#2F2418]/10 bg-white/55 p-4 backdrop-blur-sm">
                                    <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-[0.22em] text-[#2F2418]/45">
                                        <CreditCard className="h-4 w-4 text-[#9A6A2F]" />
                                        Mã tham chiếu
                                    </div>
                                    <p className="mt-2 break-all font-mono text-sm font-semibold text-[#2F2418]">{ref}</p>
                                </div>
                            )}

                            {location.pathname.includes('/wallets/deposit/momo-return') && (
                                <p className="mt-4 text-sm font-semibold text-[#2F2418]/60">
                                    {backendSyncStatus === 'syncing' && 'Đang đồng bộ kết quả MoMo về ví...'}
                                    {backendSyncStatus === 'synced' && 'Đã gửi kết quả MoMo về backend để cập nhật ví.'}
                                    {backendSyncStatus === 'failed' && 'Chưa gửi được kết quả về backend. Vui lòng mở lại hồ sơ hoặc thử callback IPN.'}
                                </p>
                            )}

                            <div className="mt-8 flex flex-col gap-3 sm:flex-row">
                                <Link
                                    to="/profile"
                                    className="inline-flex items-center justify-center gap-2 bg-[#9A6A2F] px-5 py-3 text-sm font-bold text-[#FFF8ED] transition-colors hover:bg-[#2F2418]"
                                >
                                    <ArrowLeft className="h-4 w-4" />
                                    Về hồ sơ
                                </Link>
                                <Link
                                    to="/"
                                    className="inline-flex items-center justify-center gap-2 border border-[#2F2418]/15 bg-white/45 px-5 py-3 text-sm font-bold text-[#2F2418] transition-colors hover:bg-white/75"
                                >
                                    <RefreshCw className="h-4 w-4" />
                                    Quay về trang chủ
                                </Link>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    );
}