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

    /** Returns true if newly claimed (proceed), false if already seen (duplicate). */
    public boolean claim(String idemKey, Long ticketId) {
        Boolean ok = redis.opsForValue()
                .setIfAbsent("idem:" + idemKey, String.valueOf(ticketId), Duration.ofHours(24));
        return Boolean.TRUE.equals(ok);
    }

    public Long existingTicketId(String idemKey) {
        String v = redis.opsForValue().get("idem:" + idemKey);
        return v == null ? null : Long.valueOf(v);
    }
}
