# ADR: Inventory Locking Strategy (M1)

## Context
Stock reservations must prevent overselling under concurrent load.

## Decision
For M1:
1. Load `StockItem` with `LockModeType.PESSIMISTIC_WRITE` before mutating quantities
2. Keep JPA `@Version` on `stock_items` as a secondary optimistic guard
3. Enforce non-negative quantities in the domain model and DB `CHECK` constraints

## Alternatives
- Optimistic locking only (retry on conflict)
- Conditional SQL `UPDATE ... WHERE available_qty >= :qty`

## Consequences
- Correctness first; throughput to be measured in Milestone 2 concurrency suite
- Documented choice can change if benchmarks justify conditional updates
