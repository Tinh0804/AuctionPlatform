import { useState, useEffect } from 'react';
import { Camera, Video, Plus, Info, X, ImagePlus, ArrowLeft, Sparkles, Clock, DollarSign, FileText, Tag } from 'lucide-react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import client from '../api/client';
import { useToast } from '../components/Toast';

export default function CreateAuction() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const relistId = searchParams.get('relist_id');
    const { toast } = useToast();
    const [categories, setCategories] = useState([]);
    const [formData, setFormData] = useState({
        name: '', description: '', origin: '', category_id: '',
        condition: 'NEW', start_price: 5000, step_price: 200, deposit_amount: 500,
        start_time: new Date().toISOString().slice(0, 16),
        end_time: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().slice(0, 16)
    });
    const [mainImage, setMainImage] = useState(null);
    const [additionalImages, setAdditionalImages] = useState([]);
    const [oldImages, setOldImages] = useState([]);
    
    useEffect(() => {
        client.get('/auctions/categories').then(res => setCategories(res.data)).catch(console.error);

        if (relistId) {
            client.get(`/auctions/${relistId}`).then(res => {
                const auc = res.data;
                setFormData(prev => ({
                    ...prev,
                    name: auc.product_name,
                    description: auc.description,
                    origin: auc.origin,
                    category_id: auc.category_id,
                    condition: auc.condition || 'NEW',
                    start_price: auc.start_price,
                    step_price: auc.step_price,
                    deposit_amount: auc.deposit_amount
                }));
                if (auc.images) {
                    setOldImages(auc.images);
                }
            }).catch(console.error);
        }
    }, [relistId]);

    const handleChange = (e) => setFormData({...formData, [e.target.name]: e.target.value});

    const handleMainImage = (e) => {
        if(e.target.files && e.target.files[0]) {
            setMainImage(e.target.files[0]);
        }
    };

    const handleAdditionalImages = (e) => {
        if(e.target.files) {
            setAdditionalImages([...additionalImages, ...Array.from(e.target.files)]);
        }
    };

    const removeAdditionalImage = (index) => {
        setAdditionalImages(additionalImages.filter((_, i) => i !== index));
    };

    const handleSubmit = async () => {
        const hasMainImage = mainImage || oldImages.some(i => i.is_cover);
        if (!hasMainImage) {
            toast.warning("Vui lòng tải lên ảnh chính cho vật phẩm");
            return;
        }

        try {
             const data = new FormData();
             Object.keys(formData).forEach(key => {
                 data.append(key, formData[key]);
             });
             
             if (relistId) {
                 data.append("relist_id", relistId);
             }

             if (mainImage) {
                 data.append("files", mainImage);
             }
             additionalImages.forEach(img => data.append("files", img));

             const res = await client.post('/auctions/create-auction', data, {
                 headers: { 'Content-Type': 'multipart/form-data' }
             });
             toast.success("Lên sàn thành công!");
             setTimeout(() => navigate(`/auctions/${res.data.auction_id}`), 1000);
        } catch (error) {
             console.error("Lỗi tạo phiên:", error);
             const detail = error.response?.data?.detail;
             
             // Map field names to Vietnamese
             const fieldNames = {
                 'name': 'Tên vật phẩm',
                 'description': 'Mô tả vật phẩm',
                 'origin': 'Nguồn gốc xuất xứ',
                 'category_id': 'Danh mục',
                 'start_price': 'Giá khởi điểm',
                 'step_price': 'Bước giá',
                 'deposit_amount': 'Tiền cọc',
                 'start_time': 'Thời gian bắt đầu',
                 'end_time': 'Thời gian kết thúc'
             };
             
             let msg = "Lỗi tạo phiên đấu giá";
             
             if (typeof detail === 'string') {
                 msg = detail;
             } else if (Array.isArray(detail)) {
                 // Parse validation errors
                 const errors = detail.map(err => {
                     const field = err.loc[err.loc.length - 1]; // Get last part of location
                     const fieldName = fieldNames[field] || field;
                     const message = err.msg === 'Field required' ? 'không được để trống' : err.msg;
                     return `${fieldName} ${message}`;
                 });
                 msg = errors.join(', ');
             } else if (detail && typeof detail === 'object') {
                 msg = JSON.stringify(detail);
             }
             
             toast.error(msg, 6000); // Show for 6 seconds for longer messages
        }
    };

    const inputClass = "w-full bg-[#F8F1E6]/70 border border-[#2F2418]/12 px-4 py-3 text-sm text-[#2F2418] placeholder-[#2F2418]/35 focus:outline-none focus:ring-2 focus:ring-[#9A6A2F]/20 focus:border-[#9A6A2F]/60 transition-all";

    const InputLabel = ({ children, icon: Icon }) => (
        <label className="text-sm font-semibold text-[#2F2418]/75 mb-2 flex items-center gap-2 block">
            {Icon && <Icon className="w-4 h-4 text-[#9A6A2F]" />} {children}
        </label>
    );

    return (
        <div className="min-h-screen bg-[radial-gradient(circle_at_12%_0%,rgba(154,106,47,0.16),transparent_34rem),linear-gradient(180deg,#F8F1E6,#FFF8ED)] px-4 md:px-6 py-12 animate-fade-in text-[#2F2418]">
          <div className="max-w-6xl mx-auto">
             {/* Back */}
              <button onClick={() => navigate(-1)} className="flex items-center gap-2 text-sm text-[#2F2418]/55 hover:text-[#9A6A2F] transition-colors mb-8">
                 <ArrowLeft className="w-4 h-4" /> Quay lại
             </button>

             {/* Header */}
             <div className="mb-10">
                   <p className="text-xs uppercase tracking-[0.35em] text-[#9A6A2F] mb-4">Consign With Us</p>
                   <h1 className="font-serif text-4xl md:text-6xl font-medium text-[#2F2418] mb-4">
                      {relistId ? 'Đăng lại đấu giá' : 'Tạo Phiên Đấu Giá'}
                  </h1>
                   <p className="text-[#2F2418]/58 max-w-2xl leading-7">Đưa vật phẩm của bạn đến với cộng đồng đấu giá thông qua quy trình đăng ký tuyển chọn, sang trọng và minh bạch.</p>
             </div>
             
             <div className="flex flex-col lg:flex-row gap-8">
                  {/* Form */}
                  <div className="w-full lg:w-2/3 space-y-8">
                       {/* Section 1: Images */}
                        <div className="bg-[#FFF8ED]/92 border border-[#2F2418]/10 shadow-[0_30px_90px_rgba(47,36,24,0.10)] p-6 md:p-8">
                            <h3 className="text-lg font-bold text-[#2F2418] mb-6 flex items-center gap-2">
                                <Camera className="w-5 h-5 text-[#9A6A2F]" /> Hình ảnh vật phẩm
                           </h3>
                           <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
                                {/* Main Image */}
                                <div>
                                      <p className="text-xs font-semibold text-[#2F2418]/50 mb-3">Ảnh bìa *</p>
                                      <div className="aspect-square bg-[#F8F1E6] border border-dashed border-[#9A6A2F]/30 flex flex-col items-center justify-center relative hover:border-[#9A6A2F] hover:bg-[#9A6A2F]/5 transition-all cursor-pointer overflow-hidden group">
                                         {mainImage ? (
                                             <img src={URL.createObjectURL(mainImage)} className="w-full h-full object-cover rounded-2xl" />
                                         ) : oldImages.find(i => i.is_cover) ? (
                                             <img src={oldImages.find(i => i.is_cover).url} className="w-full h-full object-cover rounded-2xl" />
                                         ) : (
                                             <div className="text-center p-4">
                                                  <div className="w-12 h-12 bg-[#9A6A2F]/10 border border-[#9A6A2F]/25 shadow-soft flex items-center justify-center mx-auto mb-3">
                                                      <ImagePlus className="w-5 h-5 text-[#9A6A2F]" />
                                                 </div>
                                                  <span className="text-xs font-semibold text-[#2F2418]/65">Tải lên ảnh chính</span>
                                             </div>
                                         )}
                                         <input type="file" accept="image/*" onChange={handleMainImage} className="absolute inset-0 opacity-0 cursor-pointer" />
                                     </div>
                                </div>

                                {/* Additional Images */}
                                <div className="md:col-span-2">
                                      <p className="text-xs font-semibold text-[#2F2418]/50 mb-3">Ảnh chi tiết ({additionalImages.length + (additionalImages.length === 0 ? oldImages.filter(i => !i.is_cover).length : 0)})</p>
                                     <div className="grid grid-cols-3 sm:grid-cols-4 gap-3">
                                         {/* Old images */}
                                         {additionalImages.length === 0 && oldImages.filter(i => !i.is_cover).map((img, idx) => (
                                             <div key={`old-${idx}`} className="aspect-square bg-slate-50 border border-slate-200 rounded-xl relative group overflow-hidden">
                                                 <img src={img.url} className="w-full h-full object-cover rounded-xl opacity-80" />
                                                 <div className="absolute inset-0 bg-black/30 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity rounded-xl">
                                                     <span className="text-[10px] text-white font-bold bg-black/40 px-2 py-1 rounded">Ảnh cũ</span>
                                                 </div>
                                             </div>
                                         ))}

                                         {/* New images */}
                                         {additionalImages.map((img, idx) => (
                                             <div key={`new-${idx}`} className="aspect-square bg-slate-50 border border-slate-200 rounded-xl relative group overflow-hidden">
                                                 <img src={URL.createObjectURL(img)} className="w-full h-full object-cover rounded-xl" />
                                                 <button 
                                                     onClick={() => removeAdditionalImage(idx)}
                                                     className="absolute top-1.5 right-1.5 bg-red-500 text-white rounded-lg p-1 shadow-lg opacity-0 group-hover:opacity-100 transition-opacity"
                                                 >
                                                     <X className="w-3 h-3" />
                                                 </button>
                                             </div>
                                         ))}
                                         
                                         {/* Add button */}
                                          <div className="aspect-square bg-[#F8F1E6] border border-dashed border-[#9A6A2F]/30 flex flex-col items-center justify-center relative hover:border-[#9A6A2F] hover:bg-[#9A6A2F]/5 transition-all cursor-pointer">
                                              <Plus className="w-5 h-5 text-[#9A6A2F]/75 mb-1" />
                                              <span className="text-[10px] font-semibold text-[#2F2418]/55">Thêm ảnh</span>
                                             <input type="file" multiple accept="image/*" onChange={handleAdditionalImages} className="absolute inset-0 opacity-0 cursor-pointer" />
                                         </div>
                                         
                                         {/* Video placeholder */}
                                          <div className="aspect-square bg-[#F8F1E6] border border-[#2F2418]/10 flex flex-col items-center justify-center opacity-40 cursor-not-allowed">
                                             <Video className="w-5 h-5 text-slate-400 mb-1" />
                                             <span className="text-[9px] text-slate-500 text-center px-1">Video 360° (Sắp có)</span>
                                         </div>
                                     </div>
                                </div>
                           </div>
                       </div>

                       {/* Section 2: Basic Info */}
                        <div className="bg-[#FFF8ED]/92 border border-[#2F2418]/10 shadow-[0_30px_90px_rgba(47,36,24,0.10)] p-6 md:p-8">
                            <h3 className="text-lg font-bold text-[#2F2418] mb-6 flex items-center gap-2">
                                <FileText className="w-5 h-5 text-[#9A6A2F]" /> Thông tin cơ bản
                           </h3>
                           <div className="space-y-5">
                               <div>
                                   <InputLabel icon={Tag}>Tên vật phẩm</InputLabel>
                                   <input type="text" name="name" value={formData.name} onChange={handleChange} 
                                        className={inputClass}
                                       placeholder="Ví dụ: Bình gốm thời Minh, Thế kỷ 15" />
                               </div>
                               <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                                   <div>
                                       <InputLabel icon={DollarSign}>Giá khởi điểm (VNĐ)</InputLabel>
                                       <input type="number" name="start_price" value={formData.start_price} onChange={handleChange} 
                                            className={inputClass} />
                                   </div>
                                   <div>
                                       <InputLabel icon={DollarSign}>Bước giá tối thiểu (VNĐ)</InputLabel>
                                       <input type="number" name="step_price" value={formData.step_price} onChange={handleChange} 
                                            className={inputClass} />
                                   </div>
                               </div>
                               <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                                   <div>
                                       <InputLabel icon={Clock}>Thời gian bắt đầu</InputLabel>
                                       <input type="datetime-local" name="start_time" value={formData.start_time} onChange={handleChange} 
                                            className={inputClass} />
                                   </div>
                                   <div>
                                       <InputLabel icon={Clock}>Thời gian kết thúc</InputLabel>
                                       <input type="datetime-local" name="end_time" value={formData.end_time} onChange={handleChange} 
                                            className={inputClass} />
                                   </div>
                               </div>
                           </div>
                       </div>

                       {/* Section 3: Details */}
                        <div className="bg-[#FFF8ED]/92 border border-[#2F2418]/10 shadow-[0_30px_90px_rgba(47,36,24,0.10)] p-6 md:p-8">
                            <h3 className="text-lg font-bold text-[#2F2418] mb-6 flex items-center gap-2">
                                <Sparkles className="w-5 h-5 text-[#9A6A2F]" /> Mô tả chi tiết
                           </h3>
                            <div className="space-y-5">
                                <div>
                                    <InputLabel>Câu chuyện & Ý nghĩa vật phẩm</InputLabel>
                                    <textarea name="description" value={formData.description} onChange={handleChange} 
                                         className={`${inputClass} min-h-32 resize-none`}
                                        placeholder="Kể về lịch sử, những người chủ sở hữu trước đây..." />
                                </div>
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                                    <div>
                                        <InputLabel>Nguồn gốc xuất xứ</InputLabel>
                                        <input type="text" name="origin" value={formData.origin} onChange={handleChange} 
                                             className={inputClass}
                                            placeholder="Gia tộc Nguyễn, Huế, Việt Nam" />
                                    </div>
                                    <div>
                                        <InputLabel>Danh mục</InputLabel>
                                        <select name="category_id" value={formData.category_id} onChange={handleChange} 
                                             className={inputClass}>
                                            <option value="">Chọn danh mục...</option>
                                            {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                                        </select>
                                    </div>
                                </div>
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                                    <div>
                                        <InputLabel>Tình trạng vật phẩm</InputLabel>
                                        <select name="condition" value={formData.condition} onChange={handleChange} 
                                             className={inputClass}>
                                            <option value="NEW">Mới / Hoàn hảo</option>
                                            <option value="USED">Đã qua sử dụng</option>
                                        </select>
                                    </div>
                                </div>
                            </div>
                       </div>
                  </div>

                  {/* Sidebar */}
                  <div className="w-full lg:w-1/3">
                       <div className="sticky top-24 space-y-6">
                             <div className="bg-[#FFF8ED] border border-[#9A6A2F]/25 shadow-[0_30px_90px_rgba(47,36,24,0.12)] p-6 md:p-8">
                                 <h4 className="flex items-center gap-2 font-bold text-[#2F2418] mb-5">
                                     <Info className="w-5 h-5 text-[#9A6A2F]"/> Lời khuyên hữu ích
                                </h4>
                                 <ul className="space-y-3 text-sm text-[#2F2418]/60">
                                    <li className="flex items-start gap-2">
                                         <span className="w-1.5 h-1.5 bg-[#9A6A2F] rounded-full mt-2 shrink-0" />
                                        Ảnh bìa nên chụp chính diện vật phẩm trên nền trung tính.
                                    </li>
                                    <li className="flex items-start gap-2">
                                         <span className="w-1.5 h-1.5 bg-[#9A6A2F] rounded-full mt-2 shrink-0" />
                                        Mô tả nguồn gốc rõ ràng tăng tỷ lệ đấu giá thành công 45%.
                                    </li>
                                    <li className="flex items-start gap-2">
                                         <span className="w-1.5 h-1.5 bg-[#9A6A2F] rounded-full mt-2 shrink-0" />
                                        Cung cấp giấy tờ chứng thực hoặc kiểm định nếu có.
                                    </li>
                                </ul>
                                
                                <div className="mt-8 space-y-3">
                                     <button onClick={handleSubmit} className="w-full py-3.5 justify-center text-base inline-flex items-center gap-2 bg-[#9A6A2F] text-[#F8F1E6] font-bold hover:bg-[#2F2418] transition-colors">
                                        <Sparkles className="w-5 h-5" /> Đăng ký đấu giá
                                    </button>
                                     <button className="w-full py-3 border border-[#2F2418]/15 text-sm font-semibold text-[#2F2418]/65 hover:border-[#9A6A2F]/50 hover:text-[#9A6A2F] transition-colors">
                                        Lưu bản nháp
                                    </button>
          </div>
        </div>
                            </div>
                       </div>
                  </div>
             </div>
        </div>
    );
}
