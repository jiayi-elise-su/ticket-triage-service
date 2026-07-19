package com.triage.ingest;

/** Kafka payload. Only the id travels; the worker reloads the ticket from Postgres. */
public record IngestEvent(Long ticketId, String tenantId) {}
