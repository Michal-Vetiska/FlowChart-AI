# Interview Preparation — Interview Core

## Java / Backend

### 1. Why modular monolith before microservices?
**Answer:** Prove domain invariants (especially stock) with lower ops cost; keep clear package boundaries and ports so extraction is mechanical later.  
**Code:** `com.flowcart.commerce.*`, [ADR-001](../decisions/ADR-001-modular-monolith-first.md).

### 2. How do you prevent overselling under concurrency?
**Answer:** `SELECT … FOR UPDATE` on stock row, domain check `available >= qty`, DB CHECK, reservation + stock update in one `@Transactional`. Demo: 100 parallel reserves on 10 stock → ≤10 success.  
**Code:** `InventoryService.reserve`, `ConcurrentReservationIntegrationTest`, demo burst endpoint.

### 3. Exactly-once vs at-least-once?
**Answer:** Broker delivery is at-least-once. We get *effectively once* processing via transactional outbox (publish after commit) + `processed_events` idempotency table.  
**Code:** `OutboxService`, `IdempotentEventProcessor`, ADR-004/005.

### 4. Saga compensation
**Answer:** Orchestrator steps reserve → pay → confirm. On payment decline, release reservations and cancel order.  
**Code:** `OrderSagaOrchestrator`, `OrderSagaIntegrationTest`.

### 5. JWT roles
**Answer:** HS256 JWT with `roles` claim; method/path rules for ADMIN / WAREHOUSE / CUSTOMER / OBSERVER. Mutations are not `permitAll`.  
**Code:** `SecurityConfig`, `AuthController`.

## Observability

### 6. Show one order across a trace
**Answer:** Micrometer Tracing + OTel exporter → Tempo; Prometheus scrapes `/actuator/prometheus`; Grafana dashboard links metrics to traces. Structured logs carry `traceId`.

## AI

### 7. How is RAG grounded?
**Answer:** Hybrid retrieval (vector + FTS + RRF). Answers cite chunk titles/refs; empty hits → insufficient evidence (no invented sources). Hash embeddings for CI; Ollama/OpenAI swappable via config.  
**Code:** `KnowledgeService`, `HashEmbeddingClient`.

### 8. Copilot tools safety
**Answer:** Tools call application services (`getOrderStatistics`, inventory, knowledge search) — LLM never runs raw SQL. Allowlisted tool names in service logic.  
**Code:** `CopilotService`.

### 9. Incident Investigator
**Answer:** Correlates Chaos Lab flags + order failure metrics + incident RAG into symptoms / evidence / cause / confidence / remediation.  
**Demo:** enable `PAYMENT_FAILURE` → investigate → high confidence cites chaos flag.

## Wave 2 honesty
If asked about K8s / full microservices / eval lab of 100 cases: **Planned**, documented in ROADMAP — not claimed as shipped.
