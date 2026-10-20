# ADR-004: Transactional Outbox Pattern

## Context
Domain writes and message publishing must not diverge (dual-write problem).

## Decision
Persist `outbox_messages` in the same DB transaction as domain changes, then publish asynchronously (after commit for in-memory bus; scheduled poller as safety net).

## Consequences
- At-least-once delivery; consumers must be idempotent (`processed_events`).
- RabbitMQ can replace the bus adapter later without changing domain services.
