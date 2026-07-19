# Smart Ticket Triage System (Java / Spring Boot)

A multi-tenant support-ticket triage service. Tickets are submitted → deduped in Redis →
queued on Kafka → consumed by a worker that runs a **router agent** (classify category +
priority) → routes to a **specialized handler agent** that calls its own tools → result is
persisted and shown live on a React/TypeScript dashboard.

**Stack:** Java 21 · Spring Boot (Web / Data JPA / Kafka / Data Redis) · PostgreSQL · Redis · Apache Kafka · React + TypeScript · Docker

> This project deliberately targets the **production-backend** half of the SWE skill set —
> web framework, SQL + transactions, caching, messaging, concurrency, idempotency, deploy.
> The agent layer is intentionally light (structured LLM tool-call inside a real service),
> since heavier agent/LLM/ML work lives elsewhere on the résumé.

## What works (Slice 1)
- `POST /api/tickets` (needs `X-Tenant-Id`) → Redis **idempotency** guard → persist `QUEUED` → **Kafka** publish → `202`.
- Kafka consumer → **router agent** triages → **handler agent** + tools → writes result + latency to Postgres.
- `GET /api/tickets` lists a tenant's tickets with **cursor pagination**; tenants are isolated.
- React dashboard: submit a ticket, watch `QUEUED → TRIAGING → HANDLED` (2s polling).
- Runs **offline** by default (rule-based triage stub, no API key). `LLM_PROVIDER=openai` uses GPT.

## Run it (infra in Docker, app on your machine)
```bash
docker compose up -d                       # postgres + redis + kafka

cd backend
# generate the gradle wrapper once if you don't have it (needs gradle installed):
#   gradle wrapper --gradle-version 8.10
./gradlew bootRun                          # API + consumer, http://localhost:8080

cd ../frontend && npm install && npm run dev   # http://localhost:5173
```
Or all-in-Docker: `docker compose --profile app up -d --build` (then start the frontend).

## Verify (Slice 1 done)
```bash
curl -X POST http://localhost:8080/api/tickets -H "X-Tenant-Id: acme" \
  -H "Content-Type: application/json" \
  -d '{"subject":"Refund please","body":"charged twice, want my money back ASAP"}'

curl -X POST http://localhost:8080/api/tickets -H "X-Tenant-Id: globex" \
  -H "Content-Type: application/json" \
  -d '{"subject":"Cannot login","body":"login returns an error since this morning"}'

curl "http://localhost:8080/api/tickets" -H "X-Tenant-Id: acme"
```
- [x] Consumer logs `ticket N -> refund/high -> HANDLED (…ms)`.
- [x] Submitting the same ticket twice returns `"status":"duplicate"` (no reprocessing).
- [x] `GET /api/tickets` for acme never shows globex's tickets.
- [x] No `X-Tenant-Id` → `400`.
- [x] `./gradlew test` passes `tenantsAreIsolated`.

## Agent pattern (interview wording)
Describe it as **"router agent + specialized handler agents, each with their own tools"** —
not "multi-agent negotiation". Router: `agents/TriageAgent.java`; handlers/tools: `agents/`.

## Key design decisions (be ready to defend)
- **Kafka, not synchronous:** ingest returns immediately; workers absorb bursts (3 partitions → scale consumers in one group).
- **Redis SETNX idempotency:** Kafka is at-least-once; a retry must not double-process/double-refund.
- **Postgres + JPA:** transactional status transitions, tenant-scoped relational queries, exact idempotency.
- **id as cursor:** bigserial → monotonic, correct pagination (vs fragile offset).
- **Consumer in-app:** simplest; scale horizontally via more instances in the `triage-workers` group.

## Known trade-off / TODO (found during Slice 2 load testing)
Kafka messages are keyed by `tenantId` (see `IngestService.publish`), which guarantees
per-tenant ordering but also means **a single tenant's traffic always lands on one
partition → one consumer instance**, regardless of how many instances are in the
`triage-workers` group. "3 partitions → scale workers" only gives real parallelism when
load is spread across >= 3 concurrently-active tenants; this was confirmed by a load
test where one dominant tenant sent 100% of the load to a single instance even with 3
instances running. Not a bug — an intentional ordering-vs-parallelism trade-off. Revisit
if a single "whale" tenant shows up (e.g. key by `tenantId` + shard bucket, or give hot
tenants a dedicated partition/topic).

See `PLAN.md` for later slices and `HANDOFF.md` for the executor AI.
