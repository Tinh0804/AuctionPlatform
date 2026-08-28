// ============================================================
// src/utils/index.js
// Các hàm tiện ích dùng chung toàn project
// ============================================================

/**
 * Format số tiền theo chuẩn tiền VNĐ
 * @param {number} amount
 * @returns {string} e.g. "1.500.000 ₫"
 */
export const formatCurrencyVND = (amount) => {
  if (amount === null || amount === undefined) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    minimumFractionDigits: 0,
  }).format(amount);
};

/**
 * Format timestamp ISO thành dạng ngày/giờ tiếng Việt
 * @param {string} isoString
 * @returns {string} e.g. "21/06/2026 17:30"
 */
export const formatDateTime = (isoString) => {
  if (!isoString) return '—';
  const date = new Date(isoString);
  return new Intl.DateTimeFormat('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(date);
};

/**
 * Tính thời gian còn lại từ bây giờ đến endTime
 * @param {string} endTime ISO string
 * @returns {{ days, hours, minutes, seconds, isEnded }}
 */
export const getTimeRemaining = (endTime) => {
  const total = new Date(endTime) - new Date();
  if (total <= 0) return { days: 0, hours: 0, minutes: 0, seconds: 0, isEnded: true };
  const seconds = Math.floor((total / 1000) % 60);
  const minutes = Math.floor((total / 1000 / 60) % 60);
  const hours = Math.floor((total / (1000 * 60 * 60)) % 24);
  const days = Math.floor(total / (1000 * 60 * 60 * 24));
  return { days, hours, minutes, seconds, isEnded: false };
};

/**
 * Gộp các class CSS (thay thế cho cn của Tailwind Merge)
 * @param {...string} classes
 * @returns {string}
 */
export const cn = (...classes) => classes.filter(Boolean).join(' ');

/**
 * Lấy chữ cái đầu của tên để hiển thị avatar
 * @param {string} name
 * @returns {string}
 */
export const getInitials = (name) => {
  if (!name) return '?';
  return name.charAt(0).toUpperCase();
};
