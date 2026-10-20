CREATE TABLE warehouses (
    id              UUID PRIMARY KEY,
    code            VARCHAR(32) NOT NULL UNIQUE,
    name            VARCHAR(120) NOT NULL,
    location        VARCHAR(255),
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL
);

CREATE TABLE stock_items (
    id              UUID PRIMARY KEY,
    warehouse_id    UUID NOT NULL REFERENCES warehouses (id),
    product_id      UUID NOT NULL REFERENCES products (id),
    available_qty   INTEGER NOT NULL CHECK (available_qty >= 0),
    reserved_qty    INTEGER NOT NULL CHECK (reserved_qty >= 0),
    version         BIGINT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_stock_warehouse_product UNIQUE (warehouse_id, product_id)
);

CREATE TABLE stock_movements (
    id              UUID PRIMARY KEY,
    stock_item_id   UUID NOT NULL REFERENCES stock_items (id),
    movement_type   VARCHAR(32) NOT NULL,
    quantity        INTEGER NOT NULL,
    reason          VARCHAR(255),
    reference_id    UUID,
    created_at      TIMESTAMPTZ NOT NULL
);

CREATE TABLE stock_reservations (
    id              UUID PRIMARY KEY,
    stock_item_id   UUID NOT NULL REFERENCES stock_items (id),
    order_id        UUID,
    quantity        INTEGER NOT NULL CHECK (quantity > 0),
    status          VARCHAR(32) NOT NULL,
    expires_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_stock_items_product ON stock_items (product_id);
CREATE INDEX idx_stock_reservations_order ON stock_reservations (order_id);
CREATE INDEX idx_stock_reservations_status ON stock_reservations (status);
