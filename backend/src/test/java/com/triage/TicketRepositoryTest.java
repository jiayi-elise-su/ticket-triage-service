package com.triage;

import com.triage.domain.Ticket;
import com.triage.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Limit;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Tenant isolation at the data layer (uses the default in-memory test DB). */
@DataJpaTest
class TicketRepositoryTest {

    @Autowired TicketRepository tickets;

    @Test
    void tenantsAreIsolated() {
        tickets.save(new Ticket("acme", "a", "hi from acme"));
        tickets.save(new Ticket("globex", "b", "hi from globex"));

        List<Ticket> acme = tickets.findByTenantIdAndIdGreaterThanOrderByIdAsc("acme", 0L, Limit.of(10));

        assertThat(acme).hasSize(1);
        assertThat(acme.get(0).getBody()).contains("acme");
        assertThat(acme.get(0).getTenantId()).isEqualTo("acme");
    }
}
