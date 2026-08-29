# GCP VM Production Deployment Guide

Hệ thống triển khai trên máy ảo GCP VM (1 VM ~ 4GB RAM) sử dụng Docker Compose với mô hình phân lớp rõ ràng:
- **Tầng Dữ liệu (Stateful):** PostgreSQL 15 & Redis 7 (sử dụng External Volume bảo toàn dữ liệu).
- **Tầng Ứng dụng (Stateless):** Spring Boot 3 Backend (port nội bộ `8080`) và Nginx Reverse Proxy (ports `80` & `443`).

---

## 1. Cấu trúc Thư mục Triển khai

```text
deploy/
├── .env.example              # Mẫu biến môi trường production
├── compose.prod.yml          # Compose tổng hợp toàn bộ stack (cho CI/CD)
├── compose.infra.yml         # Tầng lưu trữ: PostgreSQL + Redis
├── compose.app.yml           # Tầng ứng dụng: Backend + Nginx
├── nginx/
│   ├── nginx.conf            # Cấu hình chính Nginx (Worker, Event, Log, Rate limit)
│   └── conf.d/
│       └── api.conf          # Reverse Proxy, SSL TLS 1.2/1.3, Healthcheck /healthz
└── scripts/
    ├── lib/
    │   └── common.sh         # Thư viện hàm tiện ích dùng chung (logging, read_env, lock)
    ├── preflight.sh          # Kiểm tra môi trường, volume, disk, SSL trước deploy
    ├── backup-postgres.sh    # Dump dữ liệu PostgreSQL + verify + SHA256
    ├── restore-postgres.sh   # Khôi phục dữ liệu từ bản dump an toàn
    ├── rollback.sh           # Rollback backend image khẩn cấp khi gặp lỗi
    ├── renew-ssl.sh          # Tự động gia hạn chứng chỉ Let's Encrypt & reload Nginx
    └── deploy.sh             # Điều phối toàn bộ luồng zero-downtime deploy
```

---

## 2. Quy tắc An toàn Bắt buộc

- **TUYỆT ĐỐI KHÔNG** chạy `docker compose down -v` hoặc `docker volume prune`.
- Biến `POSTGRES_VOLUME_NAME` trong `/srv/auction/.env` phải khớp chính xác tên volume thật.
- File `/srv/auction/.env` trên VM phải được phân quyền `chmod 600`.
- Luôn giữ `FLYWAY_ENABLED=false` cho đến khi quá trình chạy thử nghiệm migration trên bản backup thành công.

---

## 3. Hướng dẫn Sử dụng các Script Vận hành

### Triển khai Phiên bản Mới (Routine Deployment)
```bash
sudo DEPLOY_HOME=/srv/auction APP_ENV_FILE=/srv/auction/.env bash deploy/scripts/deploy.sh <tested-git-sha>
```
* Tự động chạy preflight check.
* Tự động sao lưu PostgreSQL có kiểm tra tính toàn vẹn và mã băm SHA256.
* Pull image mới, khởi động Backend và chờ Healthcheck UP.
* Nếu lỗi, tự động kích hoạt rollback về phiên bản trước.

### Rollback Phiên bản Backend Khẩn cấp
```bash
# Rollback về phiên bản ghi nhận gần nhất trong /srv/auction/current-image-tag
sudo DEPLOY_HOME=/srv/auction bash deploy/scripts/rollback.sh

# Hoặc chỉ định rõ một Git SHA muốn rollback tới:
sudo DEPLOY_HOME=/srv/auction bash deploy/scripts/rollback.sh <specific-git-sha>
```

### Sao lưu Cơ sở Dữ liệu Thủ công
```bash
sudo DEPLOY_HOME=/srv/auction bash deploy/scripts/backup-postgres.sh
```
* File backup được lưu tại `/srv/auction/backups/postgres/<db_name>_<timestamp>.dump` kèm file `.sha256`.

### Khôi phục Cơ sở Dữ liệu từ Bản Dump
```bash
# Yêu cầu xác nhận trước khi phục hồi
sudo DEPLOY_HOME=/srv/auction bash deploy/scripts/restore-postgres.sh /srv/auction/backups/postgres/auctiondb_20260828T120000Z.dump

# Hoặc tự động xác nhận với cờ -y
sudo DEPLOY_HOME=/srv/auction bash deploy/scripts/restore-postgres.sh /path/to/backup.dump -y
```

### Gia hạn Chứng chỉ SSL & Reload Nginx
```bash
sudo DEPLOY_HOME=/srv/auction bash deploy/scripts/renew-ssl.sh
```
* Có thể đưa vào Cronjob trên server để tự động chạy hàng tuần:
  ```crontab
  0 3 * * 1 DEPLOY_HOME=/srv/auction /srv/auction/current/deploy/scripts/renew-ssl.sh >> /var/log/ssl-renew.log 2>&1
  ```

---

## 4. Quản lý theo Từng Phân lớp (Layered Compose)

Nếu cần bảo trì riêng từng tầng mà không ảnh hưởng tầng còn lại:

* **Chỉ khởi động Tầng Cơ sở Dữ liệu & Cache (Stateful):**
  ```bash
  docker compose --env-file /srv/auction/.env -f deploy/compose.infra.yml up -d
  ```

* **Chỉ cập nhật/khởi động Tầng Ứng dụng & Proxy (Stateless):**
  ```bash
  docker compose --env-file /srv/auction/.env -f deploy/compose.app.yml up -d
  ```
