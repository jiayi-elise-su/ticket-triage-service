package com.triage.ingest;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;

/** Redis SETNX-based idempotency: atomic 'claim once', so retries don't double-process. */
@Component
public class IdempotencyGuard {
    private final StringRedisTemplate redis;

    public IdempotencyGuard(StringRedisTemplate redis) { this.redis = redis; }

    public String deriveKey(String tenantId, String subject, String body) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] h = md.digest((tenantId + ":" + subject + ":" + body).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(h);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final String PENDING = "pending";

    /**
     * Atomic claim gate: SETNX a placeholder so concurrent callers with the same key
     * race on a single Redis write, not on a separate check-then-write pair.
     * Returns true only for the caller that wins the claim (must proceed to
     * save + publish + commit); false means a duplicate (caller must not save/publish).
     */
    public boolean tryClaim(String idemKey) {
        Boolean ok = redis.opsForValue()
                .setIfAbsent("idem:" + idemKey, PENDING, Duration.ofHours(24));
        return Boolean.TRUE.equals(ok);
    }

    /** Called only by the winning claimer, after the ticket is persisted. */
    public void commit(String idemKey, Long ticketId) {
        redis.opsForValue().set("idem:" + idemKey, String.valueOf(ticketId), Duration.ofHours(24));
    }

    /** Null if unseen, or if a claim is still pending (winner hasn't committed the real id yet). */
    public Long existingTicketId(String idemKey) {
        String v = redis.opsForValue().get("idem:" + idemKey);
        return (v == null || PENDING.equals(v)) ? null : Long.valueOf(v);
    }
}
