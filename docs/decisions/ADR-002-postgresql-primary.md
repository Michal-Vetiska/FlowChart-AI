# ADR-002: PostgreSQL as Primary Database

## Context
Commerce needs ACID transactions, relational modeling, and later vector search for RAG.

## Decision
Use PostgreSQL 16 (Compose image `pgvector/pgvector:pg16`) with Flyway migrations and Hibernate `ddl-auto=validate`.

## Alternatives
- MySQL / MariaDB
- NoSQL-first inventory store

## Consequences
- Strong transactional stock reservation support
- Same engine can host pgvector later (M6)
- Local port set to 5433 to coexist with other developer databases
