<div align="center">

# 🏷️ Auction Platform — Nền tảng Đấu giá Trực tuyến

**Ứng dụng web full-stack cho phép người dùng đăng bán sản phẩm, tạo phiên đấu giá, đặt giá (bid) theo thời gian thực, và quản lý toàn bộ vòng đời giao dịch từ đặt cọc, thanh toán đến nhận hàng.**

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-brightgreen)
![React](https://img.shields.io/badge/React-18.2-blue)
![License](https://img.shields.io/badge/license-Portfolio-lightgrey)

</div>

---

## 📸 Demo / Screenshots

| Trang chủ | Chi tiết đấu giá | Tạo phiên đấu giá |
|:---------:|:----------------:|:-----------------:|
| ![Home](docs/screenshots/home.png) | ![Detail](docs/screenshots/auction-detail.png) | ![Create](docs/screenshots/create-auction.png) |

| Đăng nhập | Hồ sơ & eKYC | Thanh toán |
|:---------:|:-------------:|:----------:|
| ![Login](docs/screenshots/login.png) | ![Profile](docs/screenshots/profile.png) | ![Checkout](docs/screenshots/checkout.png) |

> 💡 *Thay thế ảnh trên bằng screenshot thực tế hoặc link video demo.*

---

## 🛠️ Công nghệ (Tech Stack)

**Backend**
* **Framework:** Java 21, Spring Boot 4.0.6
* **Bảo mật & Xác thực:** Spring Security, OAuth2 (Google/Facebook), JWT (Nimbus JOSE+JWT)
* **Real-time:** Spring WebSocket
* **Cơ sở dữ liệu:** PostgreSQL (dữ liệu quan hệ), Redis (cache, blacklist token)
* **Tích hợp bên thứ 3:** Cloudinary (lưu ảnh), FPT.AI Vision (OCR eKYC), MoMo & VNPay API
* **Khác:** Spring Data JPA, MapStruct, Lombok, SpringDoc OpenAPI (Swagger)

**Frontend**
* **Core:** React 18.2, Vite 4.4.5
* **State Management:** Zustand 5
* **Styling & UI:** TailwindCSS 3, GSAP (animation), Lucide React (icon)
* **Routing:** React Router 6
* **Form & Validation:** Zod
* **HTTP Client:** Axios
* **Auth phía client:** Firebase (Google OAuth)

**DevOps**
* **Deployment:** Docker (container hóa Frontend)
* **Build tool:** Maven, npm

---

## ✨ Điểm nổi bật (Features)

* **🔐 Xác thực & Bảo mật:** Đăng nhập cục bộ + OAuth2 (Google/Facebook), JWT Access/Refresh Token, Token Blacklist qua Redis, phân quyền theo Role (ADMIN, STAFF, USER).
* **🪪 eKYC — Xác minh danh tính:** Tích hợp FPT.AI Vision OCR tự động nhận diện CCCD (số, họ tên, giới tính), upload ảnh bảo mật lên Cloudinary.
* **🏷️ Quản lý phiên đấu giá:** Tạo phiên với đầy đủ thông tin sản phẩm/giá/thời gian, vòng đời trạng thái (`PENDING → APPROVED → ACTIVE → EXTENDED → CLOSED`), tự động gia hạn khi có bid phút cuối (anti-snipe).
* **💰 Đấu giá Real-time:** Đặt giá qua WebSocket, lưu lịch sử bid, đăng ký tham gia kèm đặt cọc, xếp hạng người thắng chính + dự phòng.
* **💳 Ví điện tử & Thanh toán:** Ví với số dư khả dụng/đóng băng, tích hợp MoMo & VNPay, đa dạng loại giao dịch (nạp/rút, cọc, hoàn cọc, phí nền tảng).
* **📦 Đơn hàng hậu đấu giá:** Tự động tạo đơn sau khi kết thúc phiên, vòng đời đầy đủ (chờ thanh toán → hẹn gặp → hoàn thành), đánh giá sau giao dịch.
* **⚖️ Khiếu nại & Uy tín:** Hệ thống Dispute có bằng chứng, Admin xử lý và phán quyết, điểm uy tín theo dõi lịch sử hành vi.
* **🔔 Thông báo real-time:** Qua WebSocket cho bid mới, thắng đấu giá, hoàn cọc, đơn hàng, khiếu nại...

---

## 🚀 Hướng dẫn Cài đặt & Chạy thử

Yêu cầu hệ thống: **Java 21, Node.js 18+, PostgreSQL 14+, Redis 7+, Maven.**

### Bước 1: Clone dự án & Khởi tạo CSDL
```bash
git clone https://github.com/<your-username>/auction-platform.git
cd auction-platform

# Tạo database và import schema
psql -U postgres -c "CREATE DATABASE auctiondb;"
psql -U postgres -d auctiondb -f Backend/database.sql
```

### Bước 2: Cấu hình và Khởi động Backend
1. Mở `Backend/src/main/resources/application.yaml`, cập nhật thông tin PostgreSQL, Redis.
2. Điền các key: Cloudinary (`cloud_name`, `api_key`, `api_secret`), FPT.AI (`app.fptai.key`), OAuth2 Google/Facebook.
3. Chạy Backend bằng Maven:
```bash
./mvnw clean install -DskipTests
./mvnw spring-boot:run
```
*(Server chạy tại: `http://localhost:8080/AuctionPlatform` — Swagger UI: `/swagger-ui.html`)*

### Bước 3: Cấu hình và Khởi động Frontend
```bash
cd Frontend
npm install
echo "VITE_API_URL=http://localhost:8080/AuctionPlatform" > .env
npm run dev
```
*(Mở trình duyệt truy cập: `http://localhost:5173`)*

### Bước 4: Khởi động Redis
```bash
# Docker (khuyến nghị)
docker run -d -p 6379:6379 redis:7-alpine
```

---

## 🔑 Tài khoản Demo

Hệ thống tự động tạo tài khoản Admin khi khởi chạy lần đầu:

| Vai trò | Username | Password |
|---------|----------|----------|
| **Admin** | `admin` | `12345` |
| **Seller** | `0366900822` | `123456` |
| **Buyer** | `0366900823` | `123456` |

*Bạn cũng có thể tự đăng ký tài khoản Người mua/Người bán mới trực tiếp trên hệ thống.*

---

## 📄 Giấy phép

Dự án này được phát triển cho mục đích **học tập và portfolio**. Mọi quyền thuộc về tác giả.

---

<div align="center">

*Xây dựng với mong muốn tạo ra một nền tảng đấu giá minh bạch, an toàn và trải nghiệm real-time mượt mà!* ⭐

</div>