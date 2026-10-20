# Architecture

## System boundary (Interview Core)

```
React admin-dashboard
        |
   HTTP /api/v1/*  (+ JWT)
        |
   commerce-core (Spring Boot modular monolith)
        |
   ├── PostgreSQL + pgvector (Flyway)
   ├── Outbox → EventBus (in-memory; RabbitMQ ready)
   ├── Order Saga orchestrator (optional profile)
   ├── Knowledge RAG / Copilot / Incident Investigator
   ├── Chaos Lab (non-prod flag)
   └── Micrometer/OTel → Prometheus / Tempo / Loki / Grafana
```

No separate empty `*-service` projects. Extraction seams remain package + port boundaries.

## Bounded contexts

| Module | Responsibility | Consistency |
|--------|----------------|-------------|
| catalog | Product/category lifecycle | Local entity transactions |
| inventory | Stock & reservations | Pessimistic row lock on `stock_items` |
| ordering | Order aggregate + status machine | TX with reservation / payment |
| payment | Simulated authorize/decline/timeout | Idempotency key unique constraint |
| pricing | Coupon campaigns | Read during order creation |
| messaging | Outbox, saga, idempotent consumers | Same DB TX as domain write for outbox |
| ai | Knowledge RAG, Copilot, Investigator | Tools call application services only |
| chaos | Controlled failure injection | Feature-flagged |

## Hexagonal layout (per module)

```
adapters.in.web        → REST controllers + DTOs
application            → use cases / @Transactional services
application.port       → repository interfaces
domain                 → pure Java aggregates (no Spring/JPA)
adapters.out.persistence → JPA entities + Spring Data adapters
```

## Order paths

**Sync (`flowcart.ordering.mode=sync`, default):** create → reserve → pay in the request thread.

**Saga (`flowcart.ordering.mode=saga`):** create writes outbox `OrderCreated` → orchestrator reserve → pay → confirm; compensation releases stock on payment failure.

## AI runtime

- Default embeddings: deterministic `hash` (CI-safe, no credentials)
- Hybrid retrieval: pgvector distance + Postgres FTS + reciprocal rank fusion
- Copilot / Investigator: allowlisted tools over live DB/telemetry — no invented citations

## Wave 2 (Planned)

1. Extract inventory/order/payment into deployable services
2. Warehouse HITL agent (M8)
3. Evaluation lab with 100 cases (M11)
4. Kubernetes
