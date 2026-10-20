# ADR-005: Saga-Based Order Processing

## Context
Order flow spans inventory reservation and payment. Failures need compensation.

## Decision
Orchestration saga inside the modular monolith (`OrderSagaOrchestrator`) driven by versioned domain events. Sync mode remains default for simple demos; set `flowcart.ordering.mode=saga` for async path.

## Consequences
- Clear compensation on payment failure (release stock, cancel order).
- Extraction to microservices later reuses the same event contracts.
