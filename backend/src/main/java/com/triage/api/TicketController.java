package com.triage.api;

import com.triage.api.dto.PageResponse;
import com.triage.api.dto.TicketCreate;
import com.triage.api.dto.TicketResponse;
import com.triage.config.TenantContext;
import com.triage.domain.Ticket;
import com.triage.ingest.IdempotencyGuard;
import com.triage.ingest.IngestService;
import com.triage.repository.TicketRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Limit;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketRepository tickets;
    private final IdempotencyGuard idem;
    private final IngestService ingest;

    public TicketController(TicketRepository tickets, IdempotencyGuard idem, IngestService ingest) {
        this.tickets = tickets;
        this.idem = idem;
        this.ingest = ingest;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, Object> submit(@Valid @RequestBody TicketCreate req) {
        String tenantId = TenantContext.get();
        String subject = req.subject() == null ? "" : req.subject();
        String key = req.idempotencyKey() != null ? req.idempotencyKey()
                : idem.deriveKey(tenantId, subject, req.body());

        if (!idem.tryClaim(key)) {
            Long dup = idem.existingTicketId(key);
            Map<String, Object> resp = new java.util.HashMap<>();
            resp.put("id", dup);
            resp.put("status", "duplicate");
            return resp;
        }

        Ticket t = tickets.save(new Ticket(tenantId, subject, req.body()));
        idem.commit(key, t.getId());
        ingest.publish(t.getId(), tenantId);
        return Map.of("id", t.getId(), "status", "accepted");
    }

    @GetMapping
    public PageResponse<TicketResponse> list(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int limit) {
        String tenantId = TenantContext.get();
        long start = cursor == null ? 0L : cursor;
        List<Ticket> rows = tickets.findByTenantIdAndIdGreaterThanOrderByIdAsc(tenantId, start, Limit.of(limit));
        List<TicketResponse> items = rows.stream().map(TicketResponse::from).toList();
        Long next = items.size() == limit ? rows.get(rows.size() - 1).getId() : null;
        return new PageResponse<>(items, next);
    }

    @GetMapping("/{id}")
    public TicketResponse get(@PathVariable Long id) {
        String tenantId = TenantContext.get();
        Ticket t = tickets.findById(id).orElse(null);
        if (t == null || !t.getTenantId().equals(tenantId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
        return TicketResponse.from(t);
    }
}
