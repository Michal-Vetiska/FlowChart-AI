# FlowCart AI

Intelligent Commerce Infrastructure — interview-ready portfolio for **Java** and **AI Engineer** roles.

**Status:** Interview Core complete (M0–M7 + M9–M10 in a modular monolith). Wave-2 items (full microservice extract, HITL warehouse agent, 100-case eval lab, K8s) are Planned — not Done.

## Stack

| Layer | Choice |
|-------|--------|
| Java | 21 LTS |
| Backend | Spring Boot **4.1.1**, JPA, Flyway, Security JWT, AMQP, Micrometer/OTel |
| DB | PostgreSQL 16 + **pgvector** (`:5433`) |
| Messaging | Transactional outbox + in-memory bus (RabbitMQ in Compose for messaging profile) |
| AI | Deterministic hash embeddings + hybrid RAG (vector + FTS + RRF); Copilot/Investigator tools |
| Observability | Prometheus, Grafana, Loki, Tempo, OTel Collector |
| Frontend | React + Vite + Tailwind + TanStack Query |
| Tests | JUnit 5, AssertJ, Testcontainers, Awaitility |

## Architecture

```
admin-dashboard ──► commerce-core REST
                         │
                         ├── PostgreSQL (pgvector)
                         ├── Outbox / Saga (in-process)
                         ├── OTel ──► Tempo / Prometheus / Loki / Grafana
                         └── Knowledge RAG + Copilot + Chaos + Investigator
```

See [ARCHITECTURE.md](ARCHITECTURE.md) and [docs/decisions/](docs/decisions/).

## Quick start

```bash
cp .env.example .env
docker compose --profile core up -d

export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
cd backend/commerce-core
./gradlew bootRun --args='--spring.profiles.active=local'

# another terminal
cd apps/admin-dashboard && npm install && npm run dev
```

| URL | Purpose |
|-----|---------|
| http://localhost:5173 | Admin UI (login `admin` / `admin`) |
| http://localhost:8080 | API + Swagger |
| http://localhost:8080/actuator/prometheus | Metrics |
| http://localhost:3000 | Grafana (`admin` / `flowcart`) — profile `observability` |

### Compose profiles

| Profile | Services |
|---------|----------|
| `core` | Postgres |
| `messaging` | Postgres + RabbitMQ |
| `observability` | Postgres + Prometheus/Grafana/Loki/Tempo/OTel |
| `ai` | Postgres (pgvector ready) |
| `full` | Everything |

## Interview Demo Script (~5 minutes)

1. **Concurrency (Java flex)** — Products + Inventory show stock=10. Run Chaos → High concurrency / or `POST /api/v1/demo/concurrency-burst`. Point to test: 100 parallel reserves → ≤10 success. Mention pessimistic lock + virtual threads in burst service.
2. **Saga / outbox** — Create order with profile `saga`. Show status history / Event Monitor. Explain outbox + idempotent consumers (ADR-004/005).
3. **RAG + Copilot** — Knowledge: seeded runbook / upload `datasets/runbooks/damaged-goods.md`. Ask Copilot about damaged goods → citations. Ask about cancelled orders → tool traces from live DB.
4. **Chaos + Investigator** — Start `PAYMENT_FAILURE`, place/pay order, open Investigator → report cites chaos flags + metrics (not hardcoded prose). Reset chaos.

## Testing

```bash
cd backend/commerce-core && ./gradlew test
cd apps/admin-dashboard && npm ci && npm run build
```

AI paths use **hash embeddings** by default (`flowcart.ai.embedding-mode=hash`) — no LLM credentials in CI.

Load test template: [load-tests/](load-tests/).

## Docs

- [docs/PROGRESS.md](docs/PROGRESS.md)
- [docs/learning/INTERVIEW_PREPARATION.md](docs/learning/INTERVIEW_PREPARATION.md)
- [docs/learning/JAVA_NOTES.md](docs/learning/JAVA_NOTES.md)
- [ROADMAP.md](ROADMAP.md) — Wave 2 Planned items
