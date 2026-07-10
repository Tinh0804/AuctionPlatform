CREATE DATABASE auctiondb;
CREATE TYPE verification_status AS ENUM (
    'UNVERIFIED',
    'PENDING',
    'VERIFIED',
    'REJECTED'
);

CREATE TYPE product_condition AS ENUM (
    'NEW',
    'LIKE_NEW',
    'GOOD',
    'FAIR',
    'POOR'
);

CREATE TYPE product_status AS ENUM (
    'PENDING',       -- chờ duyệt
    'APPROVED',      -- đã duyệt
    'REJECTED',      -- bị từ chối
    'IN_AUCTION',    -- đang trong phiên đấu giá
    'SOLD'           -- đã bán
);

CREATE TYPE auction_status AS ENUM (
    'PENDING',       -- chờ duyệt
    'APPROVED',      -- đã duyệt, chưa mở
    'ACTIVE',        -- đang diễn ra
    'EXTENDED',      -- gia hạn do bid phút cuối
    'CLOSED',        -- kết thúc, chờ xử lý
    'CANCELLED',     -- bị huỷ
    'FAILED'         -- không có người thắng
);

CREATE TYPE auction_record_status AS ENUM (
    'PENDING_PAYMENT', -- chờ thanh toán
    'WIN',             -- đã thanh toán, thắng
    'LOSE',            -- không thắng
    'CANCELLED'        -- bị huỷ
);

CREATE TYPE wallet_status AS ENUM (
    'ACTIVE',
    'FROZEN',
    'CLOSED'
);

CREATE TYPE transaction_type AS ENUM (
    'DEPOSIT',                 -- nạp tiền vào ví
    'WITHDRAWAL',              -- rút tiền khỏi ví
    'AUCTION_DEPOSIT',         -- nộp cọc tham gia đấu giá
    'AUCTION_DEPOSIT_REFUND',  -- hoàn cọc
    'AUCTION_DEPOSIT_FORFEIT', -- tịch thu cọc
    'AUCTION_PAYMENT',         -- thanh toán tiền thắng
    'PLATFORM_FEE',            -- phí nền tảng
    'CANCELLATION_FEE' ,        -- phí huỷ phiên
    'ORDER_PAYMENT',
    'ESCROW_HOLD',
    'ESCROW_RELEASE',
    'DISPUTE_REFUND'
);

CREATE TYPE transaction_status AS ENUM (
    'PENDING',
    'SUCCESS',
    'FAILED',
    'REVERSED'
);

CREATE TYPE registration_status AS ENUM (
    'PENDING',     -- chờ duyệt
    'APPROVED',    -- được tham gia
    'REJECTED',    -- bị từ chối
    'CANCELLED'    -- tự huỷ
);

CREATE TYPE deposit_status AS ENUM (
    'PENDING',
    'PAID',
    'REFUNDED',
    'FORFEITED'    -- bị tịch thu
);

CREATE TYPE order_status AS ENUM (
    'PENDING_PAYMENT',   -- chờ thanh toán
    'PAID',              -- đã thanh toán
    'MEETING_SCHEDULED', -- đã hẹn gặp
    'COMPLETED',         -- hoàn thành
    'CANCELLED',
    'DISPUTED',
    'SHIPPING'
);
CREATE TYPE dispute_status AS ENUM (
    'OPEN',
    'UNDER_REVIEW',
    'RESOLVED',
    'CLOSED'
);

CREATE TYPE provider_type as  ENUM(
    'LOCAL',          -- Tài khoản do hệ thống quản lý
    'GOOGLE',         -- Đăng nhập bằng Google
    'FACEBOOK'      -- Đăng nhập bằng Facebook
);

CREATE TYPE image_reference_type AS ENUM (
    'PRODUCT',
    'DISPUTE',
    'USER'
);

-- ==========================================
-- 1. PHÂN QUYỀN & TÀI KHOẢN
-- ==========================================

-- Bỏ: permissions, role_permissions, account_role
-- Đơn giản hóa: accounts.role_id → roles trực tiếp

CREATE TABLE roles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(50)  NOT NULL UNIQUE, -- 'ADMIN', 'STAFF', 'USER'
    description VARCHAR(255)
);

CREATE TABLE accounts (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username   VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role_id    UUID NOT NULL REFERENCES roles(id), -- FK thẳng vào roles
    is_active  BOOLEAN   DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    provider provider_type  NOT null default "LOCAL",
    provider_id  VARCHAR(255)
);

-- ==========================================
-- 2. NGƯỜI DÙNG & ĐỊA CHỈ
-- ==========================================

CREATE TABLE users (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id           UUID UNIQUE NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    full_name            VARCHAR(100) NOT NULL,
    phone_number         VARCHAR(20)  NOT NULL,
    email                VARCHAR(100) NOT NULL UNIQUE,
    identity_card        VARCHAR(20)  UNIQUE,
    gender               BOOLEAN,
    dob                  DATE,
    reputation_score     INTEGER     DEFAULT 100,
    verification_status  verification_status DEFAULT 'UNVERIFIED',
    identity_front_image VARCHAR(500),
    identity_back_image  VARCHAR(500),
    created_at           TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP
);

CREATE TABLE addresses (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    ward         VARCHAR(100),
    district     VARCHAR(100),
    city         VARCHAR(100),
    address_line VARCHAR(255),
    is_default   BOOLEAN   DEFAULT FALSE,
    created_at   TIMESTAMP DEFAULT NOW()
);

-- ==========================================
-- 3. DANH MỤC & SẢN PHẨM
-- ==========================================

CREATE TABLE categories (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    parent_id   UUID REFERENCES categories(id) -- tự tham chiếu: cha/con
);

CREATE TABLE products (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID REFERENCES users(id),
    category_id         UUID REFERENCES categories(id),
    name                VARCHAR(255)      NOT NULL,
    condition           product_condition NOT NULL,
    description         VARCHAR(2000),
    origin              VARCHAR(255),
    provenance_file_url VARCHAR(500),
    manufacture_year    VARCHAR(10),
    has_certificate     BOOLEAN        DEFAULT FALSE,
    status              product_status DEFAULT 'PENDING',
    created_at          TIMESTAMP DEFAULT NOW(),
    updated_at          TIMESTAMP
);

CREATE TABLE images (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reference_type image_reference_type  NOT NULL,          -- 'PRODUCT', 'DISPUTE', 'USER', ...
    reference_id   UUID         NOT NULL,           -- polymorphic FK (no hard constraint)
    file_url       VARCHAR(500) NOT NULL,
    is_cover       BOOLEAN      DEFAULT FALSE,      -- dùng cho PRODUCT: ảnh đại diện
    sort_order     INT          DEFAULT 0,          -- thứ tự hiển thị
    description    VARCHAR(255),                    -- dùng cho DISPUTE: mô tả bằng chứng
    created_at     TIMESTAMP    DEFAULT NOW()
);
CREATE INDEX idx_images_reference ON images(reference_type, reference_id);

-- ==========================================
-- 4. VÍ & GIAO DỊCH
-- Gộp payment_requests + wallet_transactions → transactions
-- ==========================================

CREATE TABLE wallets (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    available_balance NUMERIC(18,2) DEFAULT 0,
    frozen_balance    NUMERIC(18,2) DEFAULT 0,
    pin_code          VARCHAR(255),
    status            wallet_status DEFAULT 'ACTIVE',
    notes             VARCHAR(255),
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP
);

CREATE TABLE transactions (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id         UUID NOT NULL REFERENCES wallets(id), -- ví chịu tác động
    related_wallet_id UUID REFERENCES wallets(id),          -- ví đối ứng (chuyển khoản nội bộ)
    type              transaction_type   NOT NULL,
    amount            NUMERIC(18,2)      NOT NULL,
    status            transaction_status DEFAULT 'PENDING',
    -- Cổng thanh toán (NULL nếu là giao dịch nội bộ)
    gateway_provider  VARCHAR(20),   -- 'VNPAY', 'MOMO'
    gateway_tx_id     VARCHAR(255),  -- mã GD từ đối tác
    gateway_response  TEXT,          -- raw response để debug
    expired_at        TIMESTAMP,     -- link thanh toán hết hạn
    -- Tham chiếu nghiệp vụ
    reference_type    VARCHAR(20),   -- 'REGISTRATION', 'ORDER', 'AUCTION'
    reference_id      UUID,          -- trỏ tới bảng tương ứng
    note              VARCHAR(500),
    created_at        TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ==========================================
-- 5. ĐẤU GIÁ
-- ==========================================

CREATE TABLE auctions (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID REFERENCES users(id),
    product_id       UUID REFERENCES products(id),
    start_price      NUMERIC(18,2) NOT NULL,
    current_price    NUMERIC(18,2) NOT NULL,
    step_price       NUMERIC(18,2) NOT NULL,
    deposit_amount   NUMERIC(18,2) NOT NULL,
    platform_fee     NUMERIC(18,2),   -- snapshot từ application.yml lúc tạo auction
    cancellation_fee NUMERIC(18,2),   -- snapshot từ application.yml lúc tạo auction
    start_time       TIMESTAMP NOT NULL,
    end_time         TIMESTAMP NOT NULL,
    status           auction_status DEFAULT 'PENDING',
    description      VARCHAR(2000),
    auto_extend      BOOLEAN DEFAULT FALSE,
    extend_minutes   INTEGER DEFAULT 0,
    created_at       TIMESTAMP DEFAULT NOW(),
    updated_at       TIMESTAMP
);

CREATE TABLE auction_registrations (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auction_id          UUID NOT NULL REFERENCES auctions(id),
    user_id             UUID NOT NULL REFERENCES users(id),
    deposit_amount      NUMERIC(18,2)       NOT NULL,
    deposit_status      deposit_status      DEFAULT 'PENDING',
    registration_status registration_status DEFAULT 'PENDING',
    -- Tra transactions bằng reference_type='REGISTRATION', reference_id=id
    registered_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    approved_at         TIMESTAMP,
    note                VARCHAR(500),
    UNIQUE (auction_id, user_id)
);

CREATE TABLE bids (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auction_id      UUID NOT NULL REFERENCES auctions(id),
    user_id         UUID NOT NULL REFERENCES users(id),
    bid_amount      NUMERIC(18,2) NOT NULL,
    bid_time        TIMESTAMP NOT NULL DEFAULT NOW(),
    is_winning      BOOLEAN DEFAULT FALSE, -- true = đang là bid cao nhất
    -- Gộp auction_extension_logs vào đây
    triggered_extend BOOLEAN   DEFAULT FALSE, -- bid này có kích hoạt gia hạn không
    new_end_time     TIMESTAMP               -- end_time mới nếu có gia hạn
);

CREATE TABLE auction_records (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auction_id   UUID REFERENCES auctions(id),
    user_id      UUID REFERENCES users(id),
    bid_id       UUID REFERENCES bids(id),
    winning_rank INTEGER,         -- 1 = winner, 2 = backup
    final_price  NUMERIC(18,2),
    status       auction_record_status DEFAULT 'PENDING_PAYMENT',
    expiry_time  TIMESTAMP NOT NULL,  -- deadline thanh toán
    created_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ==========================================
-- 6. ĐƠN HÀNG & KHIẾU NẠI
-- ==========================================

CREATE TABLE orders (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auction_record_id UUID REFERENCES auction_records(id),
    buyer_id          UUID NOT NULL REFERENCES users(id),
    seller_id         UUID NOT NULL REFERENCES users(id),  -- thêm để query nhanh
    total_amount      NUMERIC(18,2) NOT NULL,
    status            order_status DEFAULT 'PENDING_PAYMENT',
    meeting_address   VARCHAR(500),
    meeting_time      TIMESTAMP,
    met_at            TIMESTAMP,
    note              VARCHAR(500),
    rating_score      INTEGER CHECK (rating_score BETWEEN 1 AND 5),
    review_content    VARCHAR(1000),
    review_date       TIMESTAMP,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP
);

CREATE TABLE disputes (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id     UUID NOT NULL REFERENCES orders(id),
    claimant_id  UUID NOT NULL REFERENCES users(id),  -- người tạo khiếu nại
    reason       VARCHAR(255) NOT NULL,
    description  VARCHAR(2000),
    evidence_url VARCHAR(500),
    status       dispute_status DEFAULT 'OPEN',
    resolved_by  UUID REFERENCES users(id),           -- admin xử lý
    resolution   VARCHAR(1000),
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    resolved_at  TIMESTAMP
);

-- ==========================================
-- 7. UY TÍN
-- ==========================================

CREATE TABLE reputation_histories (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID NOT NULL REFERENCES users(id),
    score_change INTEGER NOT NULL,   -- +5, -10
    reason       VARCHAR(255),
    order_id     UUID REFERENCES orders(id),
    dispute_id   UUID REFERENCES disputes(id),
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_reputation_ref CHECK (
        (order_id IS NOT NULL AND dispute_id IS NULL)
        OR
        (order_id IS NULL   AND dispute_id IS NOT NULL)
    )
);

-- ==========================================
-- 8. THÔNG BÁO
-- Gộp notifications + user_notifications → notifications
-- ==========================================

CREATE TABLE notifications (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type           VARCHAR(50),    -- 'BID_PLACED', 'AUCTION_WON', 'DEPOSIT_REFUNDED'
    title          VARCHAR(255) NOT NULL,
    content        VARCHAR(1000),
    reference_type VARCHAR(20),   -- 'AUCTION', 'ORDER', 'DISPUTE'
    reference_id   UUID,
    is_read        BOOLEAN   DEFAULT FALSE,
    read_at        TIMESTAMP,
    created_at     TIMESTAMP DEFAULT NOW()
);

-- ==========================================
-- INDEXES
-- ==========================================

-- Accounts
CREATE INDEX idx_accounts_role_id         ON accounts(role_id);

-- Users
CREATE INDEX idx_users_account_id         ON users(account_id);

-- Products
CREATE INDEX idx_products_user_id         ON products(user_id);
CREATE INDEX idx_products_status          ON products(status);
CREATE INDEX idx_products_category_id     ON products(category_id);

-- Auctions
CREATE INDEX idx_auctions_status          ON auctions(status);
CREATE INDEX idx_auctions_end_time        ON auctions(end_time);
CREATE INDEX idx_auctions_user_id         ON auctions(user_id);

-- Bids
CREATE INDEX idx_bids_auction_id          ON bids(auction_id);
CREATE INDEX idx_bids_user_id             ON bids(user_id);
CREATE INDEX idx_bids_auction_winning     ON bids(auction_id, is_winning);

-- Registrations
CREATE INDEX idx_registrations_auction_id ON auction_registrations(auction_id);
CREATE INDEX idx_registrations_user_id    ON auction_registrations(user_id);

-- Transactions
CREATE INDEX idx_transactions_wallet_id   ON transactions(wallet_id);
CREATE INDEX idx_transactions_type        ON transactions(type);
CREATE INDEX idx_transactions_status      ON transactions(status);
CREATE INDEX idx_transactions_ref         ON transactions(reference_type, reference_id);

-- Orders
CREATE INDEX idx_orders_buyer_id          ON orders(buyer_id);
CREATE INDEX idx_orders_seller_id         ON orders(seller_id);
CREATE INDEX idx_orders_status            ON orders(status);

-- Disputes
CREATE INDEX idx_disputes_order_id        ON disputes(order_id);
CREATE INDEX idx_disputes_status          ON disputes(status);

-- Reputation
CREATE INDEX idx_reputation_user_id       ON reputation_histories(user_id);
CREATE INDEX idx_reputation_order_id      ON reputation_histories(order_id);
CREATE INDEX idx_reputation_dispute_id    ON reputation_histories(dispute_id);

-- Notifications
CREATE INDEX idx_notifications_user_id    ON notifications(user_id);
CREATE INDEX idx_notifications_unread     ON notifications(user_id, is_read);

