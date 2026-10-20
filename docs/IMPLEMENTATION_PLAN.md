# Implementation Plan

## Milestone 0 — Foundation
- [x] JDK 21 + Gradle Wrapper + Spring Boot 4.1.1 commerce-core
- [x] Docker Compose PostgreSQL (port 5433, pgvector image)
- [x] Flyway migrations + Actuator health
- [x] Product vertical slice (domain → port → JPA → REST → React)
- [x] Unit + Testcontainers integration tests
- [x] README / ARCHITECTURE / ROADMAP / ADRs skeleton

## Milestone 1 — Commerce Domain
- [x] Categories
- [x] Warehouses + StockItem + StockMovement
- [x] StockReservation with pessimistic write lock
- [x] Orders + explicit status machine
- [x] Payment simulation + idempotency keys
- [x] Pricing campaigns (percentage / fixed)
- [x] Order flow integration tests

## Next (not started)
- Milestone 2 — Concurrency suite + load tests
- Milestone 3 — RabbitMQ / outbox / saga extraction
- Milestone 4+ — Auth UI expansion, observability, AI
