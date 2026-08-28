import React, { useState, useEffect } from 'react';
import { Save, Image as ImageIcon, X } from 'lucide-react';
import toast from 'react-hot-toast';
import { adminApi } from '@/features/admin/api';

export default function AdminCategoryModal({ category, onClose, onSuccess }) {
    const [loading, setLoading] = useState(false);
    const [imageFile, setImageFile] = useState(null);
    const [imagePreview, setImagePreview] = useState(null);
    const [formData, setFormData] = useState({
        name: '',
        description: '',
        parentId: ''
    });

    useEffect(() => {
        if (category) {
            setFormData({
                name: category.name || '',
                description: category.description || '',
                parentId: category.parentId || ''
            });
            if (category.imageUrl) {
                setImagePreview(category.imageUrl);
            }
        }
    }, [category]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        if (!formData.name.trim()) {
            toast.error('Vui lòng nhập tên danh mục');
            return;
        }

        try {
            setLoading(true);

        const submitData = new FormData();
        submitData.append('name', formData.name);
        if (formData.description) submitData.append('description', formData.description);
        if (formData.parentId) submitData.append('parentId', formData.parentId);
        if (imageFile) submitData.append('image', imageFile);

            if (category) {
                await adminApi.updateCategory(category.id, submitData);
                toast.success('Cập nhật danh mục thành công');
            } else {
                await adminApi.createCategory(submitData);
                toast.success('Thêm danh mục thành công');
            }
            onSuccess();
        } catch (error) {
            toast.error(error.response?.data?.message || 'Có lỗi xảy ra');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" onClick={onClose}></div>
            
            <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md overflow-hidden flex flex-col relative animate-fade-in-up">
                <div className="px-6 py-4 border-b border-gray-100 flex items-center justify-between bg-gray-50/50">
                    <h3 className="text-lg font-bold text-gray-900">
                        {category ? 'Chỉnh sửa Danh mục' : 'Thêm Danh mục Mới'}
                    </h3>
                    <button onClick={onClose} className="text-gray-400 hover:text-gray-600 transition-colors">
                        ✕
                    </button>
                </div>

                <div className="p-6 overflow-y-auto">
                    <form id="category-form" onSubmit={handleSubmit} className="space-y-4">

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Ảnh đại diện / Icon
                            </label>
                            <div className="flex items-center gap-4">
                                {imagePreview ? (
                                    <div className="relative w-20 h-20 rounded-xl border border-gray-200 overflow-hidden bg-gray-50 flex-shrink-0 group">
                                        <img src={imagePreview} alt="Preview" className="w-full h-full object-cover" />
                                        <button 
                                            type="button"
                                            onClick={() => { setImageFile(null); setImagePreview(null); }}
                                            className="absolute inset-0 bg-black/40 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
                                        >
                                            <X className="w-5 h-5 text-white" />
                                        </button>
                                    </div>
                                ) : (
                                    <div className="w-20 h-20 rounded-xl border-2 border-dashed border-gray-300 flex items-center justify-center bg-gray-50 flex-shrink-0">
                                        <ImageIcon className="w-6 h-6 text-gray-400" />
                                    </div>
                                )}
                                <div className="flex-1">
                                    <input 
                                        type="file" 
                                        accept="image/*"
                                        onChange={(e) => {
                                            const file = e.target.files[0];
                                            if (file) {
                                                setImageFile(file);
                                                setImagePreview(URL.createObjectURL(file));
                                            }
                                        }}
                                        className="block w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-semibold file:bg-primary/10 file:text-primary hover:file:bg-primary/20 transition-colors"
                                    />
                                    <p className="mt-1 text-xs text-gray-500">PNG, JPG, WEBP (Max 2MB)</p>
                                </div>
                            </div>
                        </div>
    
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Tên danh mục <span className="text-red-500">*</span>
                            </label>
                            <input
                                type="text"
                                value={formData.name}
                                onChange={(e) => setFormData(prev => ({ ...prev, name: e.target.value }))}
                                className="w-full px-4 py-2.5 rounded-xl border border-gray-200 focus:ring-2 focus:ring-primary/20 focus:border-primary transition-colors bg-gray-50 focus:bg-white"
                                placeholder="Nhập tên danh mục..."
                            />
                        </div>
                        
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Mô tả
                            </label>
                            <textarea
                                value={formData.description}
                                onChange={(e) => setFormData(prev => ({ ...prev, description: e.target.value }))}
                                rows={3}
                                className="w-full px-4 py-2.5 rounded-xl border border-gray-200 focus:ring-2 focus:ring-primary/20 focus:border-primary transition-colors bg-gray-50 focus:bg-white resize-none"
                                placeholder="Nhập mô tả (không bắt buộc)..."
                            />
                        </div>
                    </form>
                </div>

                <div className="px-6 py-4 border-t border-gray-100 flex items-center justify-end gap-3 bg-gray-50/50">
                    <button 
                        onClick={onClose}
                        type="button"
                        className="px-6 py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-200 bg-gray-100 rounded-xl transition-colors"
                    >
                        Hủy
                    </button>
                    <button 
                        type="submit"
                        form="category-form"
                        disabled={loading}
                        className="px-6 py-2.5 text-sm font-medium text-white bg-primary hover:bg-primary-dark rounded-xl transition-colors flex items-center gap-2 disabled:opacity-50"
                    >
                        <Save className="w-4 h-4" />
                        {loading ? 'Đang lưu...' : 'Lưu Danh Mục'}
                    </button>
                </div>
            </div>
        </div>
    );
}
