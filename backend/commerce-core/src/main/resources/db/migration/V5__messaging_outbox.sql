CREATE TABLE outbox_messages (
    id              UUID PRIMARY KEY,
    event_type      VARCHAR(120) NOT NULL,
    event_version   INT NOT NULL,
    aggregate_id    UUID NOT NULL,
    correlation_id  UUID NOT NULL,
    causation_id    UUID,
    payload         TEXT NOT NULL,
    status          VARCHAR(32) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    published_at    TIMESTAMPTZ,
    attempts        INT NOT NULL DEFAULT 0,
    last_error      TEXT
);

CREATE INDEX idx_outbox_status_created ON outbox_messages (status, created_at);

CREATE TABLE processed_events (
    event_id        UUID PRIMARY KEY,
    event_type      VARCHAR(120) NOT NULL,
    processed_at    TIMESTAMPTZ NOT NULL
);

CREATE TABLE order_sagas (
    order_id            UUID PRIMARY KEY,
    saga_state          VARCHAR(64) NOT NULL,
    correlation_id      UUID NOT NULL,
    payment_outcome     VARCHAR(32),
    last_error          TEXT,
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL
);
