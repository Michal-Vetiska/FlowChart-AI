# Decisions Log

| ID | Decision | Status |
|----|----------|--------|
| ADR-001 | Modular monolith first | Accepted |
| ADR-002 | PostgreSQL as primary DB | Accepted |
| ADR-003 | RabbitMQ later (not in M0/M1) | Accepted (deferred) |
| ADR-004 | Transactional outbox later | Accepted (deferred) |
| ADR-005 | Saga later | Accepted (deferred) |
| ADR-006 | Spring AI 2.x later | Accepted (deferred) |
| ADR-007 | Hybrid retrieval later | Accepted (deferred) |
| INV-LOCK | Pessimistic write lock + `@Version` on stock_items for reservations | Accepted (M1) |
| PORT | Local Postgres mapped to host port **5433** to avoid clash with other local DBs | Accepted |
| TC-COLIMA | Testcontainers uses Colima socket + `pgvector/pgvector:pg16` local image | Accepted |
