---
description: Tự động load learnings về thiết kế UI/UX cho Frontend
globs: "**/*.{html,css,js,jsx,tsx,ts}"
trigger: always_on
---

# Frontend UI/UX Style Guide - Auction Project

Tài liệu này ghi chú lại phong cách thiết kế đặc trưng đã được chốt và thống nhất cho dự án đấu giá (Auction), nhằm tránh việc AI sau này sử dụng lại các thiết kế đại trà (slop/generic) hoặc phá vỡ cấu trúc tổng thể. AI BẮT BUỘC phải đọc và tuân thủ file này trước khi code UI.

## 1. Triết lý thiết kế (Design Philosophy)
- **Soft Editorial & Artistic:** Mang hơi hướng tạp chí nghệ thuật, cao cấp, thanh lịch nhưng phải "mềm mại, êm ái" chứ không được quá gai góc, cứng nhắc.
- **Bento & Structured:** Sử dụng cấu trúc "Bento Grid" (các ô chia tỷ lệ bất đối xứng nhưng gọn gàng) để lấp đầy không gian trống một cách nghệ thuật, không làm giao diện bị "trống trải" (empty) nhưng cũng không bị rối mắt.
- **Anti-Slop / Anti-Generic:** Tuyệt đối KHÔNG sử dụng các hiệu ứng bóng đổ (shadow) quá dày, màu sắc gradient lòe loẹt, hoặc các component bo góc nhàm chán của Bootstrap/Tailwind mặc định.

## 2. Hệ thống màu sắc (Color System)
- **Màu nền chủ đạo (Primary Background):** `#faf7f1` (Cream / Off-white). Bắt buộc phải giữ màu nền này để tạo cảm giác nhẹ nhàng, dịu mắt (khác biệt hoàn toàn với trắng tinh `#ffffff` hay nền tối).
- **Màu văn bản chính (Text/Foreground):** `#1c1815` (Đen tuyền / Than chì). Tạo độ tương phản (contrast) cực cao với nền kem, giúp chữ rõ nét, dứt khoát.
- **Màu phụ đạo / đường viền (Accent/Border):** Sử dụng các sắc thái nhạt như `#e8e2d5`, `#d8d1c9`, `#746b62` (nâu xám, be) để làm các đường viền mỏng hoặc văn bản phụ, giữ đúng vibe "bản thảo / nghệ thuật".

## 3. Typography (Kiểu chữ)
- **Font chữ DUY NHẤT:** **`Nunito`**.
- **Lý do:** Người dùng đặc biệt không thích các nét quá sắc, gai góc. Font chữ cần phải "tròn trịa, đều, mềm, mượt". Bất kì font nào có nét nhọn hoặc quá kỹ thuật đều sẽ phá vỡ vibe này.
- **Quy tắc kích thước:** Các văn bản mô tả, liên kết phụ, nhãn (label) không được để quá nhỏ (tối thiểu `text-xs` hoặc `text-sm`, mô tả dùng `text-[17px]` hoặc `text-base`). Các tiêu đề chính (h1, h2) cần to, rõ ràng (`text-5xl` trở lên) để tạo điểm nhấn thị giác.

## 4. Chuyển động & Hoạt ảnh (Motion & Animation)
- **Công cụ:** BẮT BUỘC dùng **GSAP** (thay vì Framer Motion hay CSS transition cơ bản) cho các hiệu ứng phức tạp.
- **Hiệu ứng vào trang (Entrance):** Sử dụng Stagger (xuất hiện so le) với ease `power4.out` hoặc `power3.out`. Các khối hình ảnh trượt từ dưới lên, chữ trượt ngang nhẹ.
- **Hiệu ứng duy trì (Continuous/Ambient):** Sử dụng `yoyo: true`, `repeat: -1` với thời gian dài (15-20s) để pan nhẹ hình ảnh bên trong (Parallax pan) hoặc làm nổi lơ lửng các khối badge nhỏ. Giao diện phải luôn có sức sống (alive) nhưng không được giật cục.

## 5. UI Components
- **Hình ảnh:** Mix các ảnh nghệ thuật, dùng ảnh trắng đen hoặc giảm độ bão hòa (grayscale 15-20%) để hòa hợp với màu nền kem.
- **Glassmorphism:** Hạn chế làm mờ (blur) quá nặng nề. Chỉ dùng `backdrop-blur` tinh tế kết hợp nền có độ trong suốt thấp (ví dụ `bg-white/80` hoặc `bg-[#1c1815]/80`).
- **Nút bấm (Buttons):** Nút CTA màu `#1c1815`, chữ `#faf7f1`, hover chuyển màu nhẹ và đẩy bóng (`shadow-[0_20px_40px_rgba(28,24,21,0.2)]`). Hiệu ứng click nhấn xuống mượt (`active:scale-[0.98]`).
