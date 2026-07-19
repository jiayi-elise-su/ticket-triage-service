# HANDOFF — for the AI that will execute this

Slice 1 of a multi-slice Java/Spring project (see PLAN.md). Get it running + verified, fix
build/runtime issues, STOP at Slice 1 scope.

## Context (intentional — don't "fix")
- Java 21, Spring Boot 3.3.5, Spring Data JPA (Postgres), Spring Data Redis, Spring Kafka.
- Only {ticketId, tenantId} is published to Kafka; the consumer reloads the ticket from Postgres.
- Triage uses a rule-based STUB by default (no API key). `LLM_PROVIDER=openai` switches to GPT via a plain HttpClient call.
- The Kafka consumer runs IN the same app (@KafkaListener). Scale = more app instances in the `triage-workers` group. This is intentional, not a bug.
- `ddl-auto: update` creates tables on boot (Slice 1). Flyway later.
- Test uses `@DataJpaTest` + H2 (testRuntimeOnly). Real runs use Postgres.

## Steps
1. If no Gradle wrapper in backend/, run `cd backend && gradle wrapper --gradle-version 8.10` (or use the Docker path).
2. `docker compose up -d`; wait for postgres healthy + kafka up.
3. `cd backend && ./gradlew build` — fix compile errors (safe: version bumps, imports). Don't add features to make it build.
4. `./gradlew bootRun`; run the README curl checks.
5. `./gradlew test` → `tenantsAreIsolated` green.
6. `cd frontend && npm install && npm run dev`; submit a ticket, watch status change.
7. Tick every Slice 1 checkbox in README; report results + any version changes.

## Guardrails
- Adding a load test, real OpenAI calls, RAG, gRPC, a separate worker service, deploy, or CI → STOP (Slice 2–4). Leave a TODO.
- Smallest change that passes the checklist. Adjust dependency versions if they fail to resolve; don't remove deps.
- Kafka won't start? Get `docker compose up -d` (infra only) healthy before touching the app.

## Done = every Slice 1 checkbox passes. Hand back for Slice 2.
