# Java Notes (Laravel → Spring) — M0 / M1

Tied to this codebase: `backend/commerce-core`.

## Dependency Injection vs Laravel container

- Spring creates beans and injects them via constructors (see `ProductService`, `OrderService`).
- Prefer constructor injection over field `@Autowired` — dependencies are explicit and testable.
- Laravel analogy: service container bindings; Spring does most wiring via component scan + stereotypes (`@Service`, `@Component`, `@RestController`).

## Records and DTOs

- API request/response types use Java `record` (e.g. `CreateProductRequest`, `ProductResponse`).
- Domain aggregates are mutable classes with factories (`Product.create`, `Order.create`) — closer to rich models than anemic DTOs.
- Never expose `*JpaEntity` from controllers.

## JPA / Hibernate vs Eloquent

- Domain `Product` ≠ `ProductJpaEntity`. Mapping lives in adapters (`ProductPersistenceMapper`).
- Eloquent models often are the persistence model; here hexagonal separation keeps domain free of `@Entity`.
- `ddl-auto=validate` + Flyway: schema is owned by SQL migrations, not by Hibernate auto-DDL.

## `@Transactional`

- Application services own transaction boundaries (`OrderService.createOrder`, `InventoryService.reserve`).
- One transaction can update order rows + stock rows + reservation rows — ACID across modules inside the monolith.
- Laravel analogy: `DB::transaction(fn () => ...)`.

## Ports & adapters

- `ProductRepository` (interface in `application.port`) is a **port**.
- `ProductRepositoryAdapter` + Spring Data is an **adapter**.
- Domain never imports Spring Data interfaces.

## Concurrency primitives introduced in M1 / M2

- `LockModeType.PESSIMISTIC_WRITE` on stock load (`SpringDataStockItemJpaRepository.findByIdForUpdate`).
- `@Version` on `StockItemJpaEntity` for optimistic detection.
- Domain methods `StockItem.reserve/release` throw if invariants break.
- **Virtual threads** (`Executors.newVirtualThreadPerTaskExecutor`) for concurrency burst demo — many blocking DB calls without OS-thread explosion (Java 21).
- Reservation TTL + scheduled expiration (`@EnableScheduling`, `ReservationExpirationJob`) — similar to Laravel scheduled commands, but JVM-in-process.
- Interview invariant: 100 concurrent reserves of 1 unit against stock=10 → exactly 10 successes, never negative available.

## Money

- `com.flowcart.commerce.shared.domain.Money` wraps `BigDecimal` + `Currency`.
- Same idea as careful decimal usage in PHP — but Java forces type discipline; never use `double` for money.

## Checked vs unchecked exceptions

- Domain uses unchecked `DomainException` hierarchy mapped by `GlobalExceptionHandler` to HTTP statuses.
- We do not force callers to declare `throws` for business rule failures (more idiomatic Spring style).

## Security (M4)

- Stateless JWT (HS256) via Spring Security OAuth2 Resource Server; roles claim → `ROLE_*`.
- Demo users in `SecurityConfig` (`admin`/`warehouse`/`customer`/`observer`).
- Prefer method/path authorization over `permitAll` on mutations.

## Messaging (M3)

- Outbox row written in the **same DB transaction** as the domain change → no dual-write.
- At-least-once delivery + `processed_events` → effectively-once side effects.
- Saga orchestrator is conditional on `flowcart.ordering.mode=saga`.

## Observability (M5)

- Actuator `/actuator/prometheus` + Micrometer Tracing → OTLP → Tempo.
- Custom meters: `flowcart.reservation.failures`, `flowcart.payment.failures`, `flowcart.order.latency`.
- Log pattern includes `traceId` / `spanId` for correlation.
