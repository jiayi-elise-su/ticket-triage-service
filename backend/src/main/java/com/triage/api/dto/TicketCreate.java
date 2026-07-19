package com.triage.api.dto;

import jakarta.validation.constraints.NotBlank;

public record TicketCreate(String subject, @NotBlank String body, String idempotencyKey) {}
