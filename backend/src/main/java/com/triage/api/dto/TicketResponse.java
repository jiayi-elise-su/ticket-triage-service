package com.triage.api.dto;

import com.triage.domain.Ticket;
import java.time.Instant;

public record TicketResponse(
        Long id, String subject, String body, String category, String priority,
        String assignedAgent, String result, Integer latencyMs, String status,
        Instant createdAt) {
    public static TicketResponse from(Ticket t) {
        return new TicketResponse(t.getId(), t.getSubject(), t.getBody(), t.getCategory(),
                t.getPriority(), t.getAssignedAgent(), t.getResult(), t.getLatencyMs(),
                t.getStatus().name(), t.getCreatedAt());
    }
}
