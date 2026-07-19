package com.triage.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "tickets")
public class Ticket {

    public enum Status { QUEUED, TRIAGING, HANDLED, NEEDS_HUMAN }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // bigserial; doubles as pagination cursor
    private Long id;

    @Column(nullable = false)
    private String tenantId;

    @Column(length = 300)
    private String subject = "";

    @Column(columnDefinition = "text")
    private String body = "";

    private String category;        // filled by worker
    private String priority;
    private String assignedAgent;

    @Column(columnDefinition = "text")
    private String result;

    private Integer latencyMs;

    @Enumerated(EnumType.STRING)
    private Status status = Status.QUEUED;

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    protected Ticket() {}
    public Ticket(String tenantId, String subject, String body) {
        this.tenantId = tenantId;
        this.subject = subject;
        this.body = body;
    }

    @PreUpdate void touch() { this.updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public String getTenantId() { return tenantId; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public String getCategory() { return category; }
    public String getPriority() { return priority; }
    public String getAssignedAgent() { return assignedAgent; }
    public String getResult() { return result; }
    public Integer getLatencyMs() { return latencyMs; }
    public Status getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setCategory(String c) { this.category = c; }
    public void setPriority(String p) { this.priority = p; }
    public void setAssignedAgent(String a) { this.assignedAgent = a; }
    public void setResult(String r) { this.result = r; }
    public void setLatencyMs(Integer m) { this.latencyMs = m; }
    public void setStatus(Status s) { this.status = s; }
}
