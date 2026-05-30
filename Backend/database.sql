-- ==========================================
-- AUCTION SYSTEM - FULL SCHEMA
-- PostgreSQL
-- ==========================================

-- ==========================================
-- ENUMS
-- ==========================================

CREATE TYPE auction_status AS ENUM (
    'PENDING',      -- chờ duyệt
    'APPROVED',     -- đã duyệt, chưa mở
    'ACTIVE',       -- đang diễn ra
    'EXTENDED',     -- gia hạn (có bid vào phút cuối)
    'CLOSED',       -- kết thúc, chờ xử lý
    'CANCELLED',    -- bị huỷ
    'FAILED'        -- không có người thắng
);

CREATE TYPE auction_record_status AS ENUM (
    'WIN',
    'LOSE',
    'CANCELLED',
    'PENDING_PAYMENT'
);

CREATE TYPE product_condition AS ENUM (
    'NEW',
    'LIKE_NEW',
    'GOOD',
    'FAIR',
    'POOR'
);

CREATE TYPE product_status AS ENUM (
    'PENDING',      -- chờ duyệt
    'APPROVED',     -- đã duyệt
    'REJECTED',     -- bị từ chối
    'IN_AUCTION',   -- đang trong phiên đấu giá
    'SOLD'          -- đã bán
);

CREATE TYPE verification_status AS ENUM (
    'UNVERIFIED',
    'PENDING',
    'VERIFIED',
    'REJECTED'
);

CREATE TYPE wallet_type AS ENUM (
    'PERSONAL',
    'SYSTEM'
);

CREATE TYPE wallet_status AS ENUM (
    'ACTIVE',
    'FROZEN',
    'CLOSED'
);

CREATE TYPE transaction_type AS ENUM (
    'DEPOSIT',              -- nạp tiền
    'WITHDRAWAL',           -- rút tiền
    'AUCTION_DEPOSIT',      -- nộp cọc đấu giá
    'AUCTION_DEPOSIT_REFUND', -- hoàn cọc
    'AUCTION_DEPOSIT_FORFEIT', -- mất cọc
    'AUCTION_PAYMENT',      -- thanh toán tiền thắng đấu giá
    'PLATFORM_FEE',         -- phí nền tảng
    'CANCELLATION_FEE'      -- phí huỷ
);

CREATE TYPE transaction_status AS ENUM (
    'PENDING',
    'SUCCESS',
    'FAILED',
    'REVERSED'
);

CREATE TYPE registration_status AS ENUM (
    'PENDING',      -- chờ admin duyệt
    'APPROVED',     -- được tham gia
    'REJECTED',     -- bị từ chối
    'CANCELLED'     -- tự huỷ
);

CREATE TYPE deposit_status AS ENUM (
    'PENDING',
    'PAID',
    'REFUNDED',
    'FORFEITED'     -- bị tịch thu (thắng mà không thanh toán)
);

CREATE TYPE order_status AS ENUM (
    'PENDING_PAYMENT',  -- chờ thanh toán
    'PAID',             -- đã thanh toán
    'MEETING_SCHEDULED',-- đã hẹn gặp
    'COMPLETED',        -- hoàn thành
    'CANCELLED',
    'DISPUTED'
);

CREATE TYPE dispute_status AS ENUM (
    'OPEN',
    'UNDER_REVIEW',
    'RESOLVED',
    'CLOSED'
);

CREATE TYPE payment_provider AS ENUM (
    'VNPAY',
    'MOMO',
    'INTERNAL'
);

CREATE TYPE payment_request_status AS ENUM (
    'PENDING',
    'SUCCESS',
    'FAILED',
    'EXPIRED',
    'REFUNDED'
);

CREATE TYPE payment_request_type AS ENUM (
    'DEPOSIT',      -- nạp ví
    'WITHDRAWAL'    -- rút ví
);

-- ==========================================
-- BẢNG GỐC (đã có, chuẩn hóa lại)
-- ==========================================

CREATE TABLE "role" (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE permission (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE role_permission (
    role_id        UUID NOT NULL REFERENCES "role"(id),
    permission_id  UUID NOT NULL REFERENCES permission(id),
    effective_date DATE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE account (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username   VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    is_active  BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE account_role (
    account_id UUID NOT NULL REFERENCES account(id),
    role_id    UUID NOT NULL REFERENCES "role"(id),
    PRIMARY KEY (account_id, role_id)
);

CREATE TABLE "user" (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id          UUID UNIQUE REFERENCES account(id),
    full_name           VARCHAR(100) NOT NULL,
    phone_number        VARCHAR(20) NOT NULL,
    email               VARCHAR(100) NOT NULL UNIQUE,
    identity_card       VARCHAR(20) UNIQUE,
    gender              BOOLEAN,
    reputation_score    INTEGER DEFAULT 100,
    verification_status verification_status DEFAULT 'UNVERIFIED',
    identity_front_image VARCHAR(500),
    identity_back_image  VARCHAR(500),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP
);

CREATE TABLE address (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID REFERENCES "user"(id),
    ward         VARCHAR(100),
    district     VARCHAR(100),
    city         VARCHAR(100),
    address_line VARCHAR(255),
    is_default   BOOLEAN DEFAULT FALSE,        -- thêm: địa chỉ mặc định
    created_at   TIMESTAMP DEFAULT NOW()
);

CREATE TABLE category (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    parent_id   UUID REFERENCES category(id)   -- thêm: category cha/con
);

CREATE TABLE product (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID REFERENCES "user"(id),  -- thêm: chủ sản phẩm
    category_id         UUID REFERENCES category(id),
    name                VARCHAR(255) NOT NULL,
    condition           product_condition NOT NULL,
    description         VARCHAR(2000),
    origin              VARCHAR(255),
    provenance_file_url VARCHAR(500),
    manufacture_year    VARCHAR(10),
    has_certificate     BOOLEAN DEFAULT FALSE,
    status              product_status DEFAULT 'PENDING',
    created_at          TIMESTAMP DEFAULT NOW(),
    updated_at          TIMESTAMP
);

CREATE TABLE image (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID REFERENCES product(id) ON DELETE CASCADE,
    name       VARCHAR(255),
    file_url   VARCHAR(500) NOT NULL,
    is_cover   BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE fee_config (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    percentage  NUMERIC(5,2),
    minimum_fee NUMERIC(18,2),
    valid_from  DATE NOT NULL,
    valid_to    DATE,
    description VARCHAR(255)
);

CREATE TABLE wallet (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID UNIQUE REFERENCES "user"(id),
    wallet_type       wallet_type NOT NULL,
    available_balance NUMERIC(18,2) DEFAULT 0,
    frozen_balance    NUMERIC(18,2) DEFAULT 0,
    pin_code          VARCHAR(255),
    status            wallet_status DEFAULT 'ACTIVE',
    notes             VARCHAR(255),
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP
);

-- ==========================================
-- BẢNG MỚI CẦN THÊM #1: payment_request
-- Nạp/rút tiền qua VNPay, MoMo
-- ==========================================

CREATE TABLE payment_request (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id               UUID NOT NULL REFERENCES wallet(id),
    user_id                 UUID NOT NULL REFERENCES "user"(id),
    type                    payment_request_type NOT NULL,
    provider                payment_provider NOT NULL,
    amount                  NUMERIC(18,2) NOT NULL,
    provider_transaction_id VARCHAR(255),       -- mã giao dịch từ VNPay/MoMo
    provider_response       TEXT,               -- raw response lưu debug
    status                  payment_request_status DEFAULT 'PENDING',
    expired_at              TIMESTAMP,          -- link thanh toán hết hạn
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP
);

-- ==========================================
-- BẢNG GỐC (tiếp theo)
-- ==========================================

CREATE TABLE wallet_transaction (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_wallet_id     UUID REFERENCES wallet(id),
    destination_wallet_id UUID REFERENCES wallet(id),
    payment_request_id   UUID REFERENCES payment_request(id), -- thêm: liên kết nạp/rút
    reference_id         VARCHAR(255),
    transaction_type     transaction_type NOT NULL,
    amount               NUMERIC(18,2) NOT NULL,
    note                 VARCHAR(500),                         -- thêm: mô tả giao dịch
    transaction_time     TIMESTAMP NOT NULL DEFAULT NOW(),
    status               transaction_status DEFAULT 'PENDING'
);

CREATE TABLE auction (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID REFERENCES "user"(id),
    product_id       UUID REFERENCES product(id),
    start_price      NUMERIC(18,2) NOT NULL,
    current_price    NUMERIC(18,2) NOT NULL,
    step_price       NUMERIC(18,2) NOT NULL,
    deposit_amount   NUMERIC(18,2) NOT NULL,
    platform_fee     NUMERIC(18,2),
    cancellation_fee NUMERIC(18,2),
    start_time       TIMESTAMP NOT NULL,
    end_time         TIMESTAMP NOT NULL,
    status           auction_status DEFAULT 'PENDING',
    description      VARCHAR(2000),
    created_at       TIMESTAMP DEFAULT NOW(),
    updated_at       TIMESTAMP
);

-- ==========================================
-- BẢNG MỚI CẦN THÊM #2: auction_registration
-- Đăng ký + nộp cọc trước khi bid
-- ==========================================

CREATE TABLE auction_registration (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auction_id           UUID NOT NULL REFERENCES auction(id),
    user_id              UUID NOT NULL REFERENCES "user"(id),
    deposit_amount       NUMERIC(18,2) NOT NULL,
    deposit_status       deposit_status DEFAULT 'PENDING',
    registration_status  registration_status DEFAULT 'PENDING',
    transaction_id       UUID REFERENCES wallet_transaction(id), -- giao dịch nộp cọc
    refund_transaction_id UUID REFERENCES wallet_transaction(id),-- giao dịch hoàn cọc
    registered_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    approved_at          TIMESTAMP,
    note                 VARCHAR(500),
    UNIQUE (auction_id, user_id)
);

CREATE TABLE bid (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID REFERENCES "user"(id),
    auction_id   UUID REFERENCES auction(id),
    bid_amount   NUMERIC(18,2) NOT NULL,
    bid_time     TIMESTAMP NOT NULL DEFAULT NOW(),
    is_winning   BOOLEAN DEFAULT FALSE           -- thêm: đánh dấu bid thắng hiện tại
);

CREATE TABLE auction_record (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auction_id   UUID REFERENCES auction(id),
    user_id      UUID REFERENCES "user"(id),
    bid_id       UUID REFERENCES bid(id),        -- thêm: bid tương ứng
    winning_rank INTEGER,
    final_price  NUMERIC(18,2),                  -- thêm: giá thắng cuối cùng
    status       auction_record_status DEFAULT 'PENDING_PAYMENT',
    expiry_time  TIMESTAMP NOT NULL,             -- deadline thanh toán
    created_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ==========================================
-- BẢNG MỚI CẦN THÊM #3: auction_extension_log
-- Log khi phiên đấu giá bị gia hạn (bid vào phút cuối)
-- ==========================================

CREATE TABLE auction_extension_log (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auction_id      UUID NOT NULL REFERENCES auction(id),
    old_end_time    TIMESTAMP NOT NULL,
    new_end_time    TIMESTAMP NOT NULL,
    triggered_by_bid_id UUID REFERENCES bid(id),
    extended_at     TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ==========================================
-- BẢNG GỐC (tiếp theo)
-- ==========================================

CREATE TABLE "order" (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auction_record_id UUID REFERENCES auction_record(id),
    user_id           UUID NOT NULL REFERENCES "user"(id),   -- thêm: buyer rõ ràng
    total_amount      NUMERIC(18,2),
    status            order_status DEFAULT 'PENDING_PAYMENT',
    -- gặp trực tiếp, không ship
    meeting_address   VARCHAR(500),                           -- địa điểm gặp
    meeting_time      TIMESTAMP,                              -- thời gian hẹn gặp
    met_at            TIMESTAMP,                              -- thực tế gặp lúc nào
    note              VARCHAR(500),
    rating_score      INTEGER CHECK (rating_score BETWEEN 1 AND 5),
    review_content    VARCHAR(1000),
    review_date       TIMESTAMP,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP
);

CREATE TABLE dispute (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id     UUID REFERENCES "order"(id),
    user_id      UUID REFERENCES "user"(id),
    reason       VARCHAR(255),
    description  VARCHAR(2000),
    evidence_url VARCHAR(500),
    status       dispute_status DEFAULT 'OPEN',
    resolved_by  UUID REFERENCES "user"(id),    -- thêm: admin xử lý
    resolution   VARCHAR(1000),                  -- thêm: kết quả xử lý
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    resolved_at  TIMESTAMP
);

CREATE TABLE reputation_history (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID REFERENCES "user"(id),
    score_change INTEGER NOT NULL,
    reason       VARCHAR(255),
    reference_id UUID,                           -- thêm: order_id hoặc dispute_id
    created_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE notification (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_type VARCHAR(50),
    title             VARCHAR(255) NOT NULL,
    content           VARCHAR(1000),
    link_type         VARCHAR(50),
    reference_id      UUID,
    created_at        TIMESTAMP DEFAULT NOW()
);

CREATE TABLE user_notification (
    user_id         UUID NOT NULL REFERENCES "user"(id),
    notification_id UUID NOT NULL REFERENCES notification(id),
    is_read         BOOLEAN DEFAULT FALSE,
    read_at         TIMESTAMP,                   -- thêm: đọc lúc nào
    PRIMARY KEY (user_id, notification_id)
);

-- ==========================================
-- INDEXES (performance)
-- ==========================================

CREATE INDEX idx_auction_status       ON auction(status);
CREATE INDEX idx_auction_end_time     ON auction(end_time);
CREATE INDEX idx_bid_auction_id       ON bid(auction_id);
CREATE INDEX idx_bid_user_id          ON bid(user_id);
CREATE INDEX idx_registration_auction ON auction_registration(auction_id);
CREATE INDEX idx_registration_user    ON auction_registration(user_id);
CREATE INDEX idx_wallet_tx_source     ON wallet_transaction(source_wallet_id);
CREATE INDEX idx_wallet_tx_dest       ON wallet_transaction(destination_wallet_id);
CREATE INDEX idx_payment_request_user ON payment_request(user_id);
CREATE INDEX idx_order_user           ON "order"(user_id);
CREATE INDEX idx_product_user         ON product(user_id);
CREATE INDEX idx_product_status       ON product(status);