# Build Plan — vertical slices (Java/Spring)

Positioning: this project fills the **production-backend gap** in the résumé (SQL, Redis,
Kafka, concurrency, idempotency, a compiled-language web service, deploy). The agent layer
stays light on purpose — agent/LLM/ML depth is already shown elsewhere. Each slice ends
with something runnable and a résumé bullet.

## Slice 1 — end-to-end skeleton  ✅ (this scaffold)
Submit → Redis idempotency → Kafka → consumer → router agent → handler + tools → Postgres →
React dashboard. Tenant isolation + cursor pagination + a test.
**Bullet:** Built a multi-tenant ticket-triage service in **Java/Spring Boot** (Postgres + Redis +
Kafka) that classifies and routes tickets to tool-calling handlers, with a React/TS dashboard
visualizing the pipeline live.

## Slice 2 — concurrency + honest numbers
Run multiple app instances in the `triage-workers` consumer group across the 3 partitions;
load-test with k6/JMeter firing N concurrent submits. Record throughput + p50/p95. Confirm
idempotency holds under retries.
**Adds:** sustained ~X tickets/s at p95 ~Y ms across N consumers.

## Slice 3 — real agent + eval
`LLM_PROVIDER=openai` for genuine triage; build a 10–15 case eval set, measure accuracy.
(Keep it minimal — no RAG/fine-tuning; that's already on the résumé.)
**Adds:** LLM router X% accurate on a 15-case eval; productionized LLM tool-call with fallback.

## Slice 4 — ship it
Dockerize; deploy to Fly.io; frontend to Vercel; GitHub Actions CI (gradle build + test).
Architecture diagram + metrics in README.
**Adds:** containerized, deployed, CI-gated; public demo.

## Stretch (only if all above done)
- Split the consumer into a separate service and add **gRPC** between API and worker
  ("evaluated microservices; in-process + Kafka sufficed at this scale — avoided premature split").
- Rewrite the worker in **Go** for a genuine polyglot line (do this LAST, only with time to spare).

## Priority reminder
Résumé fixes + LeetCode + applying to fall roles come FIRST. Finish Slice 1 → add the bullet →
start applying → keep slicing in parallel. Don't let later slices block applications.
