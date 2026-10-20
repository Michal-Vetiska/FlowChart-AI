# ADR-001: Modular Monolith First

## Context
FlowCart AI will eventually include multiple extractable services, but starting with many empty microservices increases complexity without proving domain correctness.

## Decision
Implement commerce as a single Spring Boot application (`commerce-core`) with package-bounded contexts (`catalog`, `inventory`, `ordering`, `payment`, `pricing`, `shared`) and hexagonal layers inside each context.

## Alternatives
- Start with independent microservices immediately
- Classic layered monolith without clear module boundaries

## Consequences
- Faster feedback on inventory/order invariants
- Clear extraction seams later (ports already isolate persistence/messaging)
- Must discipline package boundaries manually until Gradle multi-module extraction
