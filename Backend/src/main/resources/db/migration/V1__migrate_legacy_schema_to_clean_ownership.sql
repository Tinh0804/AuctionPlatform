-- Migrates the original polymorphic image/evidence schema to the bounded-context
-- ownership model used by the application. This script is intentionally
-- idempotent so it can be applied to a database that was initialized from the
-- old schema or to a database already initialized from the new bootstrap SQL.

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'users' AND column_name = 'full_name'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'users' AND column_name = 'name'
    ) THEN
        ALTER TABLE users RENAME COLUMN full_name TO name;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'users')
       AND NOT EXISTS (
           SELECT 1 FROM information_schema.columns
           WHERE table_name = 'users' AND column_name = 'avatar_image'
       ) THEN
        ALTER TABLE users ADD COLUMN avatar_image VARCHAR(500);
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'orders')
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_name = 'orders' AND column_name = 'tracking_code') THEN
        ALTER TABLE orders ADD COLUMN tracking_code VARCHAR(100);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'orders')
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_name = 'orders' AND column_name = 'shipping_provider') THEN
        ALTER TABLE orders ADD COLUMN shipping_provider VARCHAR(100);
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS product_images (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id  UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    file_url    VARCHAR(500) NOT NULL,
    is_cover    BOOLEAN DEFAULT FALSE,
    sort_order  INT DEFAULT 0,
    description VARCHAR(255),
    created_at  TIMESTAMP DEFAULT NOW()
);

DO $$
BEGIN
    IF to_regclass('images') IS NOT NULL THEN
        IF EXISTS (SELECT 1 FROM pg_attribute WHERE attrelid = to_regclass('images') AND attname = 'reference_type')
           AND EXISTS (SELECT 1 FROM pg_attribute WHERE attrelid = to_regclass('images') AND attname = 'reference_id')
           AND EXISTS (SELECT 1 FROM pg_attribute WHERE attrelid = to_regclass('images') AND attname = 'file_url') THEN
            INSERT INTO product_images (id, product_id, file_url, is_cover, sort_order, description, created_at)
            SELECT i.id, i.reference_id, i.file_url, COALESCE(i.is_cover, FALSE),
                   COALESCE(i.sort_order, 0), i.description, COALESCE(i.created_at, NOW())
            FROM images i
            WHERE i.reference_type::text = 'PRODUCT'
              AND EXISTS (SELECT 1 FROM products p WHERE p.id = i.reference_id)
            ON CONFLICT (id) DO NOTHING;
        END IF;
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS dispute_evidences (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dispute_id  UUID NOT NULL REFERENCES disputes(id) ON DELETE CASCADE,
    file_url    VARCHAR(1000) NOT NULL,
    sort_order  INT NOT NULL DEFAULT 0,
    description VARCHAR(500),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

DO $$
BEGIN
    IF to_regclass('images') IS NOT NULL THEN
        IF EXISTS (SELECT 1 FROM pg_attribute WHERE attrelid = to_regclass('images') AND attname = 'reference_type') THEN
            INSERT INTO dispute_evidences (id, dispute_id, file_url, sort_order, description, created_at)
            SELECT i.id, i.reference_id, i.file_url, COALESCE(i.sort_order, 0),
                   i.description, COALESCE(i.created_at, NOW())
            FROM images i
            WHERE i.reference_type::text = 'DISPUTE'
              AND EXISTS (SELECT 1 FROM disputes d WHERE d.id = i.reference_id)
            ON CONFLICT (id) DO NOTHING;
        END IF;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'disputes' AND column_name = 'evidence_url'
    ) THEN
        INSERT INTO dispute_evidences (dispute_id, file_url, sort_order)
        SELECT d.id, d.evidence_url, 0
        FROM disputes d
        WHERE d.evidence_url IS NOT NULL
          AND NOT EXISTS (
              SELECT 1 FROM dispute_evidences e
              WHERE e.dispute_id = d.id AND e.file_url = d.evidence_url
          );
        ALTER TABLE disputes DROP COLUMN evidence_url;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_product_images_product ON product_images(product_id);
CREATE INDEX IF NOT EXISTS idx_dispute_evidences_dispute ON dispute_evidences(dispute_id, sort_order);

DO $$
BEGIN
    IF to_regclass('images') IS NOT NULL THEN
        DROP TABLE images;
    END IF;
    IF EXISTS (SELECT 1 FROM pg_type WHERE typname = 'image_reference_type') THEN
        DROP TYPE image_reference_type;
    END IF;
END $$;
