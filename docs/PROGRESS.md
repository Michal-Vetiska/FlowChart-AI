# Progress

## COMPLETED — Interview Core

### M0–M1 Foundation
- Spring Boot 4.1.1 modular monolith, Flyway, Compose Postgres `:5433`
- Catalog / inventory / ordering / payment / pricing hexagonal modules
- Admin Products UI

### M2 Concurrency
- 100-parallel reservation Testcontainers suite
- Reservation expiration job
- Demo burst endpoint + k6 template
- Learning notes on locks / isolation / virtual threads

### M3 Events (in monolith)
- Transactional outbox + publisher
- In-memory event bus (RabbitMQ available in Compose)
- Order saga orchestrator (`flowcart.ordering.mode=saga`)
- Idempotent consumers (`processed_events`)
- ADR-004 / ADR-005

### M4-lite Security + Admin UI
- JWT + roles `ADMIN`, `WAREHOUSE_OPERATOR`, `CUSTOMER`, `SYSTEM_OBSERVER`
- Admin layout: Overview, Products, Orders, Inventory, Events, Copilot, Knowledge, Chaos, Investigator

### M5 Observability
- Micrometer Prometheus + OTel tracing bridge
- Compose: Prometheus, Grafana, Loki, Tempo, OTel Collector
- Grafana dashboard JSON + custom commerce meters

### M6–M7 RAG + Copilot
- Tika ingestion, chunking, hash embeddings → pgvector
- Hybrid retrieval: vector + FTS + RRF + citations
- Copilot tool calling over application services (no raw SQL from LLM)
- Knowledge / Copilot UI

### M9–M10 Chaos + Investigator
- Chaos scenarios: payment failure, inventory timeout, duplicate event, concurrency burst
- Incident Investigator builds report from live chaos flags, order metrics, and RAG

### Demo polish
- Demo seeder (`local` profile / `flowcart.demo.seed-on-startup`)
- `datasets/incidents` + runbooks
- README interview script, CI with stub AI (hash embeddings)

## IN PROGRESS
- None (Interview Core closed)

## PLANNED (Wave 2 — not Done)
- Full microservice extraction
- M8 warehouse agent HITL
- M11 evaluation lab (100 cases)
- Kubernetes deployment

## BLOCKED
- None
