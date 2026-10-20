# Inventory Latency Incident Pattern

## Symptoms
- Elevated order create latency
- Inventory reserve spans take seconds instead of milliseconds
- Timeouts under concurrency burst

## Likely causes
1. Chaos Lab `INVENTORY_TIMEOUT` delay injection
2. Lock contention on `stock_items` under high concurrency
3. Missing index / cold DB (less common in demos)

## Evidence checklist
- Chaos status: `inventoryDelayMs > 0`
- Tempo: long inventory spans
- Concurrency burst demo: successful reservations still `<= stock`

## Remediation
1. Reset Chaos Lab
2. Re-run concurrency burst to prove invariant still holds
3. Inspect pessimistic lock wait in Postgres if delay remains
