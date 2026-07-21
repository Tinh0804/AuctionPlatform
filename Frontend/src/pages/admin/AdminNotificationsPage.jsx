import React, { useState } from 'react';
import { adminApi } from '@/features/admin/api';
import { Send, Users, User, Shield } from 'lucide-react';
import { toast } from 'react-toastify';

export default function AdminNotificationsPage() {
    const [form, setForm] = useState({
        sendType: 'broadcast', // 'broadcast' | 'role' | 'single'
        roleName: 'USER',
        userId: '',
        title: '',
        content: '',
        type: 'ADMIN_ANNOUNCEMENT',
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm(prev => ({ ...prev, [name]: value }));
        setError('');
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!form.title.trim() || !form.content.trim()) {
            setError('Vui lòng điền đầy đủ tiêu đề và nội dung');
            return;
        }
        if (form.sendType === 'single' && !form.userId.trim()) {
            setError('Vui lòng nhập User ID');
            return;
        }

        setLoading(true);
        try {
            await adminApi.sendNotification({
                userId: form.sendType === 'single' ? form.userId.trim() : null,
                roleName: form.sendType === 'role' ? form.roleName : null,
                title: form.title.trim(),
                content: form.content.trim(),
                type: form.type,
            });
            toast.success('Gửi thông báo thành công');
            setForm({ sendType: 'broadcast', roleName: 'USER', userId: '', title: '', content: '', type: 'ADMIN_ANNOUNCEMENT' });
        } catch (err) {
            setError(err?.response?.data?.message || 'Gửi thông báo thất bại');
        } finally {
            setLoading(false);
        }
    };

    const notifTypes = [
        { value: 'ADMIN_ANNOUNCEMENT', label: 'Thông báo chung' },
        { value: 'SYSTEM', label: 'Hệ thống' },
        { value: 'WARNING', label: 'Cảnh báo' },
        { value: 'INFO', label: 'Thông tin' },
    ];

    return (
        <div className="max-w-3xl mx-auto space-y-6">
            <div className="flex items-center justify-between">
                <div>
                    <h2 className="text-2xl font-bold text-gray-900">Tạo thông báo mới</h2>
                    <p className="text-sm text-gray-500 mt-1">Gửi thông báo cho người dùng hệ thống</p>
                </div>
            </div>

            <div className="bg-white rounded-2xl shadow-sm border border-gray-200 overflow-hidden">
                <form onSubmit={handleSubmit} className="p-8 space-y-6">
                    {/* Send type toggle */}
                    <div>
                        <label className="block text-sm font-semibold text-gray-900 mb-3">Đối tượng nhận</label>
                        <div className="grid grid-cols-3 gap-4">
                            <button
                                type="button"
                                onClick={() => setForm(prev => ({ ...prev, sendType: 'broadcast' }))}
                                className={`flex flex-col items-center justify-center gap-2 p-4 rounded-xl border-2 transition-all ${
                                    form.sendType === 'broadcast'
                                        ? 'border-[#111] bg-[#111] text-white shadow-md'
                                        : 'border-gray-200 bg-white text-gray-600 hover:border-gray-300 hover:bg-gray-50'
                                }`}
                            >
                                <Users className="w-6 h-6" />
                                <span className="text-sm font-medium">Tất cả (Toàn thể)</span>
                            </button>
                            <button
                                type="button"
                                onClick={() => setForm(prev => ({ ...prev, sendType: 'role' }))}
                                className={`flex flex-col items-center justify-center gap-2 p-4 rounded-xl border-2 transition-all ${
                                    form.sendType === 'role'
                                        ? 'border-[#111] bg-[#111] text-white shadow-md'
                                        : 'border-gray-200 bg-white text-gray-600 hover:border-gray-300 hover:bg-gray-50'
                                }`}
                            >
                                <Shield className="w-6 h-6" />
                                <span className="text-sm font-medium">Theo vai trò</span>
                            </button>
                            <button
                                type="button"
                                onClick={() => setForm(prev => ({ ...prev, sendType: 'single' }))}
                                className={`flex flex-col items-center justify-center gap-2 p-4 rounded-xl border-2 transition-all ${
                                    form.sendType === 'single'
                                        ? 'border-[#111] bg-[#111] text-white shadow-md'
                                        : 'border-gray-200 bg-white text-gray-600 hover:border-gray-300 hover:bg-gray-50'
                                }`}
                            >
                                <User className="w-6 h-6" />
                                <span className="text-sm font-medium">User chỉ định</span>
                            </button>
                        </div>
                    </div>

                    {/* Role Selection (only for role) */}
                    {form.sendType === 'role' && (
                        <div className="bg-gray-50 p-4 rounded-xl border border-gray-100">
                            <label className="block text-sm font-medium text-gray-700 mb-2">Vai trò</label>
                            <select
                                name="roleName"
                                value={form.roleName}
                                onChange={handleChange}
                                className="w-full px-4 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-[#111]/10 focus:border-[#111] transition-all bg-white"
                            >
                                <option value="USER">User (Người dùng thường)</option>
                                <option value="ADMIN">Admin (Quản trị viên)</option>
                            </select>
                        </div>
                    )}

                    {/* User ID (only for single) */}
                    {form.sendType === 'single' && (
                        <div className="bg-gray-50 p-4 rounded-xl border border-gray-100">
                            <label className="block text-sm font-medium text-gray-700 mb-2">User ID (UUID)</label>
                            <input
                                type="text"
                                name="userId"
                                value={form.userId}
                                onChange={handleChange}
                                placeholder="Nhập UUID của người nhận..."
                                className="w-full px-4 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-[#111]/10 focus:border-[#111] transition-all font-mono bg-white"
                            />
                        </div>
                    )}

                    <div className="grid grid-cols-2 gap-6">
                        {/* Notification type */}
                        <div className="col-span-2">
                            <label className="block text-sm font-medium text-gray-700 mb-2">Loại thông báo</label>
                            <select
                                name="type"
                                value={form.type}
                                onChange={handleChange}
                                className="w-full px-4 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-[#111]/10 focus:border-[#111] transition-all bg-white"
                            >
                                {notifTypes.map(t => (
                                    <option key={t.value} value={t.value}>{t.label}</option>
                                ))}
                            </select>
                        </div>

                        {/* Title */}
                        <div className="col-span-2">
                            <label className="block text-sm font-medium text-gray-700 mb-2">Tiêu đề</label>
                            <input
                                type="text"
                                name="title"
                                value={form.title}
                                onChange={handleChange}
                                placeholder="Nhập tiêu đề thông báo..."
                                maxLength={255}
                                className="w-full px-4 py-3 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-[#111]/10 focus:border-[#111] transition-all"
                            />
                        </div>

                        {/* Content */}
                        <div className="col-span-2">
                            <label className="block text-sm font-medium text-gray-700 mb-2">Nội dung</label>
                            <textarea
                                name="content"
                                value={form.content}
                                onChange={handleChange}
                                placeholder="Nhập nội dung chi tiết..."
                                rows={6}
                                maxLength={1000}
                                className="w-full px-4 py-3 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-[#111]/10 focus:border-[#111] transition-all resize-none"
                            />
                            <p className="mt-2 text-xs text-gray-400 text-right">{form.content.length}/1000 ký tự</p>
                        </div>
                    </div>

                    {/* Error */}
                    {error && (
                        <div className="px-4 py-3 rounded-xl bg-red-50 text-sm text-red-600 font-medium flex items-center gap-2">
                            <div className="w-1.5 h-1.5 rounded-full bg-red-600"></div>
                            {error}
                        </div>
                    )}

                    {/* Actions */}
                    <div className="pt-4 border-t border-gray-100 flex justify-end">
                        <button
                            type="submit"
                            disabled={loading}
                            className="flex items-center justify-center gap-2 px-8 py-3 rounded-xl text-sm font-semibold text-white bg-[#111] hover:bg-[#333] hover:shadow-lg hover:-translate-y-0.5 disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:translate-y-0 disabled:hover:shadow-none transition-all duration-200"
                        >
                            <Send className="w-4 h-4" />
                            {loading ? 'Đang gửi...' : 'Gửi thông báo ngay'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
