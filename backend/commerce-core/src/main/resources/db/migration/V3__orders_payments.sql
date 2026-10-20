CREATE TABLE customers (
    id              UUID PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    full_name       VARCHAR(200) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL
);

CREATE TABLE orders (
    id                  UUID PRIMARY KEY,
    customer_id         UUID NOT NULL REFERENCES customers (id),
    status              VARCHAR(40) NOT NULL,
    currency            VARCHAR(3) NOT NULL,
    subtotal_amount     NUMERIC(19, 4) NOT NULL,
    discount_amount     NUMERIC(19, 4) NOT NULL DEFAULT 0,
    total_amount        NUMERIC(19, 4) NOT NULL,
    coupon_code         VARCHAR(64),
    idempotency_key     VARCHAR(128) UNIQUE,
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL
);

CREATE TABLE order_items (
    id                  UUID PRIMARY KEY,
    order_id            UUID NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id          UUID NOT NULL REFERENCES products (id),
    warehouse_id        UUID NOT NULL REFERENCES warehouses (id),
    sku                 VARCHAR(64) NOT NULL,
    product_name        VARCHAR(200) NOT NULL,
    quantity            INTEGER NOT NULL CHECK (quantity > 0),
    unit_price          NUMERIC(19, 4) NOT NULL,
    line_total          NUMERIC(19, 4) NOT NULL
);

CREATE TABLE order_status_history (
    id              UUID PRIMARY KEY,
    order_id        UUID NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    from_status     VARCHAR(40),
    to_status       VARCHAR(40) NOT NULL,
    reason          VARCHAR(255),
    created_at      TIMESTAMPTZ NOT NULL
);

CREATE TABLE payments (
    id                  UUID PRIMARY KEY,
    order_id            UUID NOT NULL REFERENCES orders (id),
    idempotency_key     VARCHAR(128) NOT NULL UNIQUE,
    amount              NUMERIC(19, 4) NOT NULL,
    currency            VARCHAR(3) NOT NULL,
    status              VARCHAR(32) NOT NULL,
    provider_reference  VARCHAR(128),
    failure_reason      VARCHAR(255),
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL
);

CREATE TABLE discount_campaigns (
    id                  UUID PRIMARY KEY,
    code                VARCHAR(64) NOT NULL UNIQUE,
    name                VARCHAR(120) NOT NULL,
    discount_type       VARCHAR(32) NOT NULL,
    discount_value      NUMERIC(19, 4) NOT NULL,
    valid_from          TIMESTAMPTZ NOT NULL,
    valid_to            TIMESTAMPTZ NOT NULL,
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_orders_customer ON orders (customer_id);
CREATE INDEX idx_orders_status ON orders (status);
CREATE INDEX idx_order_items_order ON order_items (order_id);
CREATE INDEX idx_payments_order ON payments (order_id);

ALTER TABLE stock_reservations
    ADD CONSTRAINT fk_stock_reservations_order
    FOREIGN KEY (order_id) REFERENCES orders (id);
