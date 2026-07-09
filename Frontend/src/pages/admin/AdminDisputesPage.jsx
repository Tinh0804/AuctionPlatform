import { useState, useEffect } from 'react';
import { ShieldAlert, CheckCircle, XCircle, Search, Eye } from 'lucide-react';
import apiClient from '@/services/apiClient';

export default function AdminDisputesPage() {
    const [disputes, setDisputes] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedDispute, setSelectedDispute] = useState(null);
    const [resolution, setResolution] = useState('');
    const [outcome, setOutcome] = useState('');
    const [submitting, setSubmitting] = useState(false);

    const [filterStatus, setFilterStatus] = useState('ALL');

    useEffect(() => {
        fetchDisputes();
    }, []);

    const fetchDisputes = () => {
        setLoading(true);
        apiClient.get('/admin/disputes')
            .then(res => {
                setDisputes(res.data?.result || []);
                setLoading(false);
            })
            .catch(err => {
                console.error(err);
                alert("Lỗi tải danh sách khiếu nại");
                setLoading(false);
            });
    };

    const handleResolve = async (e) => {
        e.preventDefault();
        if (!resolution || !outcome) {
            alert("Vui lòng điền lý do phân xử và chọn kết quả.");
            return;
        }
        
        if (!window.confirm("Hành động này sẽ giải ngân hoặc hoàn tiền và không thể hoàn tác. Bạn chắc chắn chứ?")) return;

        setSubmitting(true);
        try {
            await apiClient.post(`/admin/disputes/${selectedDispute.id}/resolve`, {
                resolution,
                outcome
            });
            alert("Phân xử thành công!");
            setSelectedDispute(null);
            fetchDisputes();
        } catch (error) {
            alert(error.response?.data?.detail || "Lỗi phân xử");
        } finally {
            setSubmitting(false);
        }
    };

    const StatusBadge = ({ status }) => {
        const map = {
            'OPEN':         { label: 'Chờ xử lý', cls: 'bg-orange-500/10 text-orange-600 border-orange-500/20' },
            'UNDER_REVIEW': { label: 'Đang xem xét', cls: 'bg-blue-500/10 text-blue-600 border-blue-500/20' },
            'RESOLVED':     { label: 'Đã giải quyết', cls: 'bg-emerald-500/10 text-emerald-600 border-emerald-500/20' },
            'CLOSED':       { label: 'Đã đóng', cls: 'bg-gray-500/10 text-gray-600 border-gray-500/20' }
        };
        const { label, cls } = map[status] || { label: status, cls: 'bg-gray-100 text-gray-600 border-gray-200' };
        return <span className={`px-2 py-1 text-xs font-semibold rounded border ${cls}`}>{label}</span>;
    };

    const filteredDisputes = filterStatus === 'ALL' 
        ? disputes 
        : disputes.filter(d => d.status === filterStatus);

    return (
        <div className="p-6">
            <h1 className="text-2xl font-bold text-gray-800 mb-6 flex items-center gap-2">
                <ShieldAlert className="w-7 h-7 text-red-500" /> Quản lý Khiếu Nại (Disputes)
            </h1>

            <div className="bg-white rounded-lg shadow-sm border border-gray-100 p-4 mb-6 flex gap-4">
                <select 
                    value={filterStatus}
                    onChange={(e) => setFilterStatus(e.target.value)}
                    className="border border-gray-300 rounded px-4 py-2 text-sm focus:ring-blue-500 focus:border-blue-500"
                >
                    <option value="ALL">Tất cả trạng thái</option>
                    <option value="OPEN">Chờ xử lý</option>
                    <option value="UNDER_REVIEW">Đang xem xét</option>
                    <option value="RESOLVED">Đã giải quyết</option>
                    <option value="CLOSED">Đã đóng</option>
                </select>
                <div className="relative flex-1 max-w-md">
                    <Search className="absolute left-3 top-2.5 text-gray-400 w-4 h-4" />
                    <input type="text" placeholder="Tìm mã đơn hàng hoặc người khiếu nại..." className="w-full pl-9 pr-4 py-2 border border-gray-300 rounded text-sm focus:ring-blue-500 focus:border-blue-500" />
                </div>
            </div>

            <div className="bg-white rounded-lg shadow-sm border border-gray-100 overflow-hidden">
                {loading ? (
                    <div className="p-8 text-center text-gray-500">Đang tải...</div>
                ) : (
                    <table className="w-full text-left border-collapse">
                        <thead>
                            <tr className="bg-gray-50 text-gray-600 text-sm border-b">
                                <th className="p-4 font-semibold">Sản phẩm</th>
                                <th className="p-4 font-semibold">Người khiếu nại (Buyer)</th>
                                <th className="p-4 font-semibold">Người bán (Seller)</th>
                                <th className="p-4 font-semibold">Lý do</th>
                                <th className="p-4 font-semibold">Trạng thái</th>
                                <th className="p-4 font-semibold text-center">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filteredDisputes.map(d => (
                                <tr key={d.id} className="border-b hover:bg-gray-50 transition-colors">
                                    <td className="p-4">
                                        <div className="flex items-center gap-3">
                                            {d.productImageUrl && <img src={d.productImageUrl} alt="Product" className="w-10 h-10 rounded object-cover" />}
                                            <div>
                                                <p className="font-medium text-gray-800 text-sm max-w-[200px] truncate">{d.productName || 'N/A'}</p>
                                                <p className="text-xs text-gray-500 font-bold text-red-500">{(d.orderAmount || 0).toLocaleString('vi-VN')} đ</p>
                                            </div>
                                        </div>
                                    </td>
                                    <td className="p-4 text-sm font-medium text-gray-800">{d.claimantName}</td>
                                    <td className="p-4 text-sm text-gray-600">{d.sellerName}</td>
                                    <td className="p-4 text-sm text-gray-800">
                                        <span className="font-semibold text-orange-600">{d.reason}</span>
                                        <p className="text-xs text-gray-500 mt-1 max-w-[250px] truncate">{d.description}</p>
                                    </td>
                                    <td className="p-4"><StatusBadge status={d.status} /></td>
                                    <td className="p-4 text-center">
                                        <button onClick={() => setSelectedDispute(d)} className="p-2 text-blue-600 hover:bg-blue-50 rounded transition-colors inline-flex items-center gap-1">
                                            <Eye className="w-4 h-4" /> <span className="text-sm font-medium">Chi tiết</span>
                                        </button>
                                    </td>
                                </tr>
                            ))}
                            {filteredDisputes.length === 0 && (
                                <tr><td colSpan="6" className="p-8 text-center text-gray-500">Không có dữ liệu khiếu nại.</td></tr>
                            )}
                        </tbody>
                    </table>
                )}
            </div>

            {/* Resolve Modal */}
            {selectedDispute && (
                <div className="fixed inset-0 z-50 bg-black/60 flex items-center justify-center p-4">
                    <div className="bg-white max-w-4xl w-full rounded-xl shadow-2xl flex flex-col max-h-[90vh]">
                        <div className="p-6 border-b flex justify-between items-center bg-gray-50 rounded-t-xl">
                            <h2 className="text-xl font-bold text-gray-800 flex items-center gap-2">
                                <ShieldAlert className="w-6 h-6 text-red-500" /> Chi Tiết Khiếu Nại
                            </h2>
                            <button onClick={() => { setSelectedDispute(null); setResolution(''); setOutcome(''); }} className="text-gray-400 hover:text-gray-600 p-1">
                                <XCircle className="w-6 h-6" />
                            </button>
                        </div>
                        
                        <div className="p-6 overflow-y-auto flex-1 grid grid-cols-1 md:grid-cols-2 gap-8">
                            {/* Left: Info */}
                            <div className="space-y-6">
                                <div>
                                    <h3 className="text-sm font-bold text-gray-500 uppercase tracking-wider mb-3">Thông tin đơn hàng</h3>
                                    <div className="bg-gray-50 p-4 rounded-lg border">
                                        <p className="font-semibold text-gray-800 mb-1">{selectedDispute.productName}</p>
                                        <div className="flex justify-between text-sm mb-1">
                                            <span className="text-gray-500">Giá trị:</span>
                                            <span className="font-bold text-red-500">{(selectedDispute.orderAmount || 0).toLocaleString('vi-VN')} đ</span>
                                        </div>
                                        <div className="flex justify-between text-sm mb-1">
                                            <span className="text-gray-500">Người mua:</span>
                                            <span className="font-medium">{selectedDispute.buyerName}</span>
                                        </div>
                                        <div className="flex justify-between text-sm">
                                            <span className="text-gray-500">Người bán:</span>
                                            <span className="font-medium">{selectedDispute.sellerName}</span>
                                        </div>
                                    </div>
                                </div>

                                <div>
                                    <h3 className="text-sm font-bold text-gray-500 uppercase tracking-wider mb-3">Nội dung khiếu nại</h3>
                                    <div className="bg-orange-50 p-4 rounded-lg border border-orange-100">
                                        <div className="mb-2"><span className="text-xs font-semibold bg-orange-200 text-orange-800 px-2 py-1 rounded">Lý do</span></div>
                                        <p className="font-bold text-orange-800 mb-3">{selectedDispute.reason}</p>
                                        <div className="mb-2"><span className="text-xs font-semibold bg-orange-200 text-orange-800 px-2 py-1 rounded">Mô tả chi tiết</span></div>
                                        <p className="text-sm text-gray-700 whitespace-pre-wrap">{selectedDispute.description || 'Không có mô tả chi tiết.'}</p>
                                    </div>
                                </div>

                                <div>
                                    <h3 className="text-sm font-bold text-gray-500 uppercase tracking-wider mb-3">Ảnh Bằng Chứng ({selectedDispute.evidences?.length || 0})</h3>
                                    <div className="flex gap-2 flex-wrap">
                                        {selectedDispute.evidences?.map((img, i) => (
                                            <a key={i} href={img.url} target="_blank" rel="noreferrer" className="block w-24 h-24 rounded border overflow-hidden hover:opacity-80 transition-opacity">
                                                <img src={img.url} alt={`evidence-${i}`} className="w-full h-full object-cover" />
                                            </a>
                                        ))}
                                        {(!selectedDispute.evidences || selectedDispute.evidences.length === 0) && (
                                            <p className="text-sm text-gray-500 italic">Không có ảnh bằng chứng</p>
                                        )}
                                    </div>
                                </div>
                            </div>

                            {/* Right: Resolution */}
                            <div className="bg-gray-50 p-6 rounded-xl border flex flex-col h-full">
                                <h3 className="text-lg font-bold text-gray-800 mb-4">Phân Xử</h3>
                                
                                {selectedDispute.status === 'RESOLVED' || selectedDispute.status === 'CLOSED' ? (
                                    <div className="bg-white p-4 rounded border border-gray-200 flex-1">
                                        <div className="mb-4">
                                            <span className="text-xs font-bold text-gray-400 block mb-1">Kết quả phân xử</span>
                                            <span className="px-3 py-1 bg-green-100 text-green-700 rounded-full text-sm font-bold">Đã phân xử</span>
                                        </div>
                                        <div className="mb-4">
                                            <span className="text-xs font-bold text-gray-400 block mb-1">Admin xử lý</span>
                                            <p className="font-medium text-gray-800">{selectedDispute.resolvedByName || 'Hệ thống'}</p>
                                        </div>
                                        <div>
                                            <span className="text-xs font-bold text-gray-400 block mb-1">Lý do giải quyết</span>
                                            <p className="text-sm text-gray-700 bg-gray-50 p-3 rounded">{selectedDispute.resolution}</p>
                                        </div>
                                    </div>
                                ) : (
                                    <form onSubmit={handleResolve} className="flex-1 flex flex-col">
                                        <div className="mb-6">
                                            <label className="text-sm font-bold text-gray-700 block mb-3">Quyết định (Kết quả)</label>
                                            <div className="space-y-3">
                                                <label className={`flex items-center gap-3 p-4 rounded-lg border-2 cursor-pointer transition-colors ${outcome === 'BUYER_WIN' ? 'border-green-500 bg-green-50' : 'border-gray-200 bg-white hover:border-green-200'}`}>
                                                    <input type="radio" name="outcome" value="BUYER_WIN" checked={outcome === 'BUYER_WIN'} onChange={(e) => setOutcome(e.target.value)} className="w-5 h-5 text-green-600 focus:ring-green-500" />
                                                    <div>
                                                        <p className="font-bold text-green-700 flex items-center gap-1"><CheckCircle className="w-4 h-4" /> Người mua (Buyer) Thắng</p>
                                                        <p className="text-xs text-gray-600 mt-1">Hoàn {selectedDispute.orderAmount?.toLocaleString()}đ cho Buyer. Seller bị trừ 20 điểm uy tín.</p>
                                                    </div>
                                                </label>
                                                <label className={`flex items-center gap-3 p-4 rounded-lg border-2 cursor-pointer transition-colors ${outcome === 'SELLER_WIN' ? 'border-blue-500 bg-blue-50' : 'border-gray-200 bg-white hover:border-blue-200'}`}>
                                                    <input type="radio" name="outcome" value="SELLER_WIN" checked={outcome === 'SELLER_WIN'} onChange={(e) => setOutcome(e.target.value)} className="w-5 h-5 text-blue-600 focus:ring-blue-500" />
                                                    <div>
                                                        <p className="font-bold text-blue-700 flex items-center gap-1"><ShieldAlert className="w-4 h-4" /> Người bán (Seller) Thắng</p>
                                                        <p className="text-xs text-gray-600 mt-1">Giải ngân cho Seller (trừ phí). Buyer bị trừ 10 điểm uy tín (khiếu nại vô căn cứ).</p>
                                                    </div>
                                                </label>
                                            </div>
                                        </div>
                                        
                                        <div className="mb-6 flex-1">
                                            <label className="text-sm font-bold text-gray-700 block mb-2">Lý do phân xử / Ghi chú cho 2 bên</label>
                                            <textarea 
                                                rows="5"
                                                value={resolution}
                                                onChange={(e) => setResolution(e.target.value)}
                                                className="w-full border-gray-300 rounded-lg shadow-sm focus:border-blue-500 focus:ring-blue-500 text-sm p-3 border"
                                                placeholder="VD: Sau khi xem xét ảnh bằng chứng, nhận thấy hàng hóa bị hỏng trong quá trình vận chuyển, quyết định hoàn tiền cho người mua..."
                                                required
                                            />
                                        </div>

                                        <div className="flex gap-3">
                                            <button type="button" onClick={() => { setSelectedDispute(null); setResolution(''); setOutcome(''); }} className="flex-1 py-3 bg-white border border-gray-300 text-gray-700 font-semibold rounded-lg hover:bg-gray-50 transition-colors">
                                                Hủy bỏ
                                            </button>
                                            <button type="submit" disabled={submitting || !outcome || !resolution} className="flex-1 py-3 bg-red-600 text-white font-bold rounded-lg hover:bg-red-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed shadow-md">
                                                {submitting ? 'Đang xử lý...' : 'Xác nhận phân xử'}
                                            </button>
                                        </div>
                                    </form>
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
