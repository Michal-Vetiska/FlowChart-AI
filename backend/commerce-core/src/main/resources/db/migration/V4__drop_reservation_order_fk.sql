-- Soft-reference order_id so reservations can be created during saga / demo bursts
-- before the order aggregate is fully persisted or when using synthetic order ids in tests.
ALTER TABLE stock_reservations
    DROP CONSTRAINT IF EXISTS fk_stock_reservations_order;
