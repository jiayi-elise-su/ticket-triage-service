package com.triage.repository;

import com.triage.domain.Ticket;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    // cursor pagination, tenant-scoped (the tenantId filter enforces isolation)
    List<Ticket> findByTenantIdAndIdGreaterThanOrderByIdAsc(String tenantId, Long cursor, Limit limit);
}
