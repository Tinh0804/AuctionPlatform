import React, { useState, useEffect } from 'react';
import { Save } from 'lucide-react';
import toast from 'react-hot-toast';
import { updateAuction, getAllCategories } from '@/features/admin/api';

export default function AdminEditAuctionModal({ auctionDetail, onClose, onSuccess }) {
    const [loading, setLoading] = useState(false);
    const [categories, setCategories] = useState([]);
    const [formData, setFormData] = useState({
        name: '',
        description: '',
        origin: '',
        categoryId: '',
        condition: 'NEW',
        manufactureYear: '',
        startPrice: '',
        stepPrice: '',
        depositAmount: '',
        startTime: '',
        endTime: ''
    });

    useEffect(() => {
        getAllCategories().then(res => {
            if (res.result) setCategories(res.result);
        });
        
        if (auctionDetail) {
            // Find category ID based on name since backend only gave us name in some endpoints, 
            // but wait, AdminAuctionUpdateRequest needs categoryId. We might need to map it back or just use the current category name to find ID.
            // Actually, we should just let them re-select if we don't have ID.
            
            setFormData({
                name: auctionDetail.productName || '',
                description: auctionDetail.description || '',
                origin: auctionDetail.productOrigin || '',
                categoryId: '', // will be set after categories load if matched
                condition: auctionDetail.productCondition || 'NEW',
                manufactureYear: auctionDetail.productManufactureYear || '',
                startPrice: auctionDetail.startPrice || '',
                stepPrice: auctionDetail.stepPrice || '',
                depositAmount: auctionDetail.depositAmount || '',
                startTime: auctionDetail.startTime ? auctionDetail.startTime.slice(0, 16) : '',
                endTime: auctionDetail.endTime ? auctionDetail.endTime.slice(0, 16) : ''
            });
        }
    }, [auctionDetail]);

    useEffect(() => {
        if (categories.length > 0 && auctionDetail && !formData.categoryId) {
            const cat = categories.find(c => c.name === auctionDetail.categoryName);
            if (cat) setFormData(prev => ({ ...prev, categoryId: cat.id }));
        }
    }, [categories, auctionDetail]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            setLoading(true);
            await updateAuction(auctionDetail.id, formData);
            toast.success('Cập nhật phiên đấu giá thành công');
            onSuccess();
        } catch (error) {
            toast.error(error.response?.data?.message || 'Có lỗi xảy ra');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 z-[60] flex items-center justify-center p-4">
            <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" onClick={onClose}></div>
            
            <div className="bg-white rounded-2xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-hidden flex flex-col relative animate-fade-in-up">
                <div className="px-6 py-4 border-b border-gray-100 flex items-center justify-between bg-gray-50/50">
                    <h3 className="text-lg font-bold text-gray-900">Sửa Phiên Đấu Giá</h3>
                    <button onClick={onClose} className="text-gray-400 hover:text-gray-600 transition-colors">✕</button>
                </div>

                <div className="p-6 overflow-y-auto">
                    <form id="auction-edit-form" onSubmit={handleSubmit} className="space-y-4">
                        <div className="grid grid-cols-2 gap-4">
                            <div className="col-span-2">
                                <label className="block text-sm font-medium text-gray-700 mb-1">Tên sản phẩm</label>
                                <input type="text" value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} className="w-full px-4 py-2.5 rounded-xl border border-gray-200" required />
                            </div>
                            
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Danh mục</label>
                                <select value={formData.categoryId} onChange={e => setFormData({...formData, categoryId: e.target.value})} className="w-full px-4 py-2.5 rounded-xl border border-gray-200" required>
                                    <option value="">Chọn danh mục</option>
                                    {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                                </select>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Tình trạng</label>
                                <select value={formData.condition} onChange={e => setFormData({...formData, condition: e.target.value})} className="w-full px-4 py-2.5 rounded-xl border border-gray-200" required>
                                    <option value="NEW">Mới</option>
                                    <option value="USED">Đã sử dụng</option>
                                </select>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Xuất xứ</label>
                                <input type="text" value={formData.origin} onChange={e => setFormData({...formData, origin: e.target.value})} className="w-full px-4 py-2.5 rounded-xl border border-gray-200" />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Năm sản xuất</label>
                                <input type="text" value={formData.manufactureYear} onChange={e => setFormData({...formData, manufactureYear: e.target.value})} className="w-full px-4 py-2.5 rounded-xl border border-gray-200" />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Giá khởi điểm</label>
                                <input type="number" value={formData.startPrice} onChange={e => setFormData({...formData, startPrice: e.target.value})} className="w-full px-4 py-2.5 rounded-xl border border-gray-200" required />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Bước giá</label>
                                <input type="number" value={formData.stepPrice} onChange={e => setFormData({...formData, stepPrice: e.target.value})} className="w-full px-4 py-2.5 rounded-xl border border-gray-200" required />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Thời gian bắt đầu</label>
                                <input type="datetime-local" value={formData.startTime} onChange={e => setFormData({...formData, startTime: e.target.value})} className="w-full px-4 py-2.5 rounded-xl border border-gray-200" required />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Thời gian kết thúc</label>
                                <input type="datetime-local" value={formData.endTime} onChange={e => setFormData({...formData, endTime: e.target.value})} className="w-full px-4 py-2.5 rounded-xl border border-gray-200" required />
                            </div>

                            <div className="col-span-2">
                                <label className="block text-sm font-medium text-gray-700 mb-1">Mô tả chi tiết</label>
                                <textarea rows="3" value={formData.description} onChange={e => setFormData({...formData, description: e.target.value})} className="w-full px-4 py-2.5 rounded-xl border border-gray-200"></textarea>
                            </div>
                        </div>
                    </form>
                </div>

                <div className="px-6 py-4 border-t border-gray-100 flex items-center justify-end gap-3 bg-gray-50/50">
                    <button onClick={onClose} type="button" className="px-6 py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-200 bg-gray-100 rounded-xl transition-colors">Hủy</button>
                    <button type="submit" form="auction-edit-form" disabled={loading} className="px-6 py-2.5 text-sm font-medium text-white bg-primary hover:bg-primary-dark rounded-xl transition-colors flex items-center gap-2 disabled:opacity-50">
                        <Save className="w-4 h-4" /> {loading ? 'Đang lưu...' : 'Lưu Thay Đổi'}
                    </button>
                </div>
            </div>
        </div>
    );
}
