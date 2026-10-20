# ADR-003: RabbitMQ for Asynchronous Messaging

## Context
Event-driven order processing is required for Interview Core, but full broker coupling can wait until extraction.

## Decision (updated Interview Core)
- Ship **transactional outbox + saga + idempotent consumers** inside the modular monolith.
- Default bus: **in-memory** (`flowcart.messaging.mode=in-memory`) for tests and notebook-friendly demos.
- Provide **RabbitMQ** via Compose profile `messaging` as the extraction seam; AMQP starter is on the classpath.
- Do **not** create empty `*-service` deployables yet (Wave 2).

## Alternatives
- Kafka from day one
- Extract inventory/order services before proving saga/outbox

## Consequences
- Interview demos prove outbox/saga without ops tax of many JVMs
- Switching EventBus adapter to RabbitMQ is localized
- See ADR-004 (outbox) and ADR-005 (saga)
