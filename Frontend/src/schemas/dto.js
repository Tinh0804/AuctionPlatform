// ============================================================
// src/schemas/dto.js
// Zod validation schemas cho toàn bộ form input
// ============================================================
import { z } from 'zod';

// --- Auth ---
export const loginSchema = z.object({
  username: z.string().min(3, 'Tên đăng nhập tối thiểu 3 ký tự'),
  password: z.string().min(6, 'Mật khẩu tối thiểu 6 ký tự'),
});

export const registerSchema = z
  .object({
    full_name: z.string().min(2, 'Họ tên tối thiểu 2 ký tự'),
    email: z.string().email('Email không hợp lệ'),
    phone: z.string().regex(/^(0[3|5|7|8|9])+([0-9]{8})$/, 'Số điện thoại không hợp lệ'),
    username: z.string().min(3, 'Tên đăng nhập tối thiểu 3 ký tự'),
    password: z.string().min(6, 'Mật khẩu tối thiểu 6 ký tự'),
    confirm_password: z.string(),
  })
  .refine((data) => data.password === data.confirm_password, {
    message: 'Mật khẩu xác nhận không khớp',
    path: ['confirm_password'],
  });

// --- Auction ---
export const createAuctionSchema = z.object({
  name: z.string().min(5, 'Tên sản phẩm tối thiểu 5 ký tự'),
  description: z.string().min(20, 'Mô tả tối thiểu 20 ký tự'),
  category_id: z.string().uuid('Danh mục không hợp lệ'),
  condition: z.enum(['NEW', 'LIKE_NEW', 'GOOD', 'FAIR', 'POOR']),
  origin: z.string().optional(),
  start_price: z.number().positive('Giá khởi điểm phải lớn hơn 0'),
  step_price: z.number().positive('Bước giá phải lớn hơn 0'),
  deposit_amount: z.number().min(0, 'Tiền đặt cọc không hợp lệ'),
  start_time: z.string().min(1, 'Vui lòng chọn thời gian bắt đầu'),
  end_time: z.string().min(1, 'Vui lòng chọn thời gian kết thúc'),
});

// --- Bid ---
export const bidSchema = z.object({
  amount: z.number().positive('Số tiền đặt giá phải lớn hơn 0'),
});

// --- Deposit Wallet ---
export const depositSchema = z.object({
  amount: z
    .number()
    .min(10000, 'Tối thiểu nạp 10.000 ₫')
    .max(500000000, 'Tối đa nạp 500.000.000 ₫'),
});
