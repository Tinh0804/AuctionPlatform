import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { AlertCircle, Upload, X, ArrowLeft } from 'lucide-react';
import apiClient from '@/services/apiClient';

export default function CreateDisputePage() {
    const { orderId } = useParams();
    const navigate = useNavigate();
    const [order, setOrder] = useState(null);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);

    const [reason, setReason] = useState('');
    const [description, setDescription] = useState('');
    const [files, setFiles] = useState([]);
    const [previews, setPreviews] = useState([]);

    useEffect(() => {
        apiClient.get(`/orders/${orderId}`)
            .then(res => {
                setOrder(res.data?.result || res.data);
                setLoading(false);
            })
            .catch(err => {
                console.error(err);
                alert("Không thể tải thông tin đơn hàng");
                navigate(-1);
            });
    }, [orderId, navigate]);

    const handleFileChange = (e) => {
        const selectedFiles = Array.from(e.target.files);
        if (files.length + selectedFiles.length > 5) {
            alert("Chỉ được upload tối đa 5 ảnh.");
            return;
        }

        const newFiles = [...files, ...selectedFiles];
        setFiles(newFiles);

        // Generate previews
        const newPreviews = selectedFiles.map(file => URL.createObjectURL(file));
        setPreviews([...previews, ...newPreviews]);
    };

    const removeFile = (index) => {
        const newFiles = files.filter((_, i) => i !== index);
        const newPreviews = previews.filter((_, i) => i !== index);
        URL.revokeObjectURL(previews[index]); // free memory
        setFiles(newFiles);
        setPreviews(newPreviews);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!reason) {
            alert("Vui lòng chọn lý do khiếu nại");
            return;
        }

        setSubmitting(true);
        const formData = new FormData();
        formData.append('orderId', orderId);
        formData.append('reason', reason);
        formData.append('description', description);
        files.forEach(file => {
            formData.append('files', file);
        });

        try {
            await apiClient.post('/disputes', formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
            });
            alert("Đã gửi khiếu nại thành công. Ban quản trị sẽ xem xét sớm nhất.");
            navigate('/bidder/profile/orders?sub=purchases');
        } catch (error) {
            alert(error.response?.data?.detail || "Lỗi gửi khiếu nại");
        } finally {
            setSubmitting(false);
        }
    };

    if (loading) return <div className="p-8 text-center text-[#2F2418]/60">Đang tải...</div>;

    return (
        <div className="max-w-3xl mx-auto py-12 px-4">
            <button onClick={() => navigate(-1)} className="flex items-center gap-2 text-[#9A6A2F] mb-6 hover:underline font-medium">
                <ArrowLeft className="w-4 h-4" /> Quay lại
            </button>
            
            <div className="bg-[#FFF8ED] border border-[#9A6A2F]/20 p-8 shadow-[0_28px_90px_rgba(47,36,24,0.10)]">
                <h1 className="font-serif text-3xl text-[#2F2418] mb-6 flex items-center gap-2">
                    <AlertCircle className="w-7 h-7 text-orange-500" /> Yêu cầu khiếu nại
                </h1>
                
                <div className="bg-[#F8F1E6] p-4 border border-[#9A6A2F]/15 mb-8 flex gap-4">
                    {order?.productImageUrl && <img src={order.productImageUrl} alt="Product" className="w-16 h-16 object-cover rounded border border-[#9A6A2F]/20" />}
                    <div>
                        <p className="font-semibold text-[#2F2418]">{order?.productName || "Sản phẩm"}</p>
                        <p className="text-sm text-[#2F2418]/60">Mã đơn: {order?.trackingCode || orderId}</p>
                        <p className="text-sm font-bold text-[#9A6A2F]">{(order?.totalAmount || 0).toLocaleString('vi-VN')} đ</p>
                    </div>
                </div>

                <form onSubmit={handleSubmit} className="space-y-6">
                    <div>
                        <label className="text-sm font-bold text-[#9A6A2F] mb-2 block">Lý do khiếu nại *</label>
                        <select 
                            value={reason} 
                            onChange={(e) => setReason(e.target.value)}
                            className="w-full bg-[#F8F1E6] border border-[#9A6A2F]/25 px-4 py-3 text-sm text-[#2F2418] focus:outline-none focus:ring-2 focus:ring-[#9A6A2F]/20"
                            required
                        >
                            <option value="">-- Chọn lý do --</option>
                            <option value="Hàng không đúng mô tả">Hàng không đúng mô tả</option>
                            <option value="Hàng bị hư hỏng, vỡ">Hàng bị hư hỏng, vỡ</option>
                            <option value="Thiếu hàng / Không nhận được hàng">Thiếu hàng / Không nhận được hàng</option>
                            <option value="Hàng giả, nhái">Hàng giả, nhái</option>
                            <option value="Lý do khác">Lý do khác</option>
                        </select>
                    </div>

                    <div>
                        <label className="text-sm font-bold text-[#9A6A2F] mb-2 block">Mô tả chi tiết</label>
                        <textarea 
                            rows="4" 
                            placeholder="Mô tả rõ vấn đề bạn gặp phải..."
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            className="w-full bg-[#F8F1E6] border border-[#9A6A2F]/25 px-4 py-3 text-sm text-[#2F2418] focus:outline-none focus:ring-2 focus:ring-[#9A6A2F]/20"
                        />
                    </div>

                    <div>
                        <label className="text-sm font-bold text-[#9A6A2F] mb-2 block">Ảnh bằng chứng (Tối đa 5 ảnh)</label>
                        <div className="flex flex-wrap gap-4 mb-4">
                            {previews.map((preview, index) => (
                                <div key={index} className="relative w-24 h-24 border border-[#9A6A2F]/25 rounded overflow-hidden">
                                    <img src={preview} alt="preview" className="w-full h-full object-cover" />
                                    <button 
                                        type="button" 
                                        onClick={() => removeFile(index)}
                                        className="absolute top-1 right-1 bg-red-500 text-white rounded-full p-1"
                                    >
                                        <X className="w-3 h-3" />
                                    </button>
                                </div>
                            ))}
                            {files.length < 5 && (
                                <label className="w-24 h-24 border-2 border-dashed border-[#9A6A2F]/30 flex flex-col items-center justify-center text-[#9A6A2F] hover:bg-[#9A6A2F]/5 cursor-pointer transition-colors">
                                    <Upload className="w-6 h-6 mb-1" />
                                    <span className="text-xs">Tải ảnh</span>
                                    <input type="file" multiple accept="image/*" onChange={handleFileChange} className="hidden" />
                                </label>
                            )}
                        </div>
                        <p className="text-xs text-[#2F2418]/45">Hỗ trợ JPG, PNG. Bằng chứng rõ ràng giúp quản trị viên phân xử chính xác hơn.</p>
                    </div>

                    <div className="pt-4 border-t border-[#9A6A2F]/15">
                        <button 
                            type="submit" 
                            disabled={submitting}
                            className="w-full bg-orange-600 text-white font-bold py-3.5 hover:bg-orange-700 transition-colors disabled:opacity-50"
                        >
                            {submitting ? 'Đang gửi...' : 'Gửi Khiếu Nại'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
