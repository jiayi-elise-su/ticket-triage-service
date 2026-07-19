package com.triage.ingest;

import com.triage.agents.Handlers;
import com.triage.agents.TriageAgent;
import com.triage.domain.Ticket;
import com.triage.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TicketConsumer {
    private static final Logger log = LoggerFactory.getLogger(TicketConsumer.class);
    private final TicketRepository tickets;
    private final TriageAgent triage;
    private final Handlers handlers;

    public TicketConsumer(TicketRepository tickets, TriageAgent triage, Handlers handlers) {
        this.tickets = tickets;
        this.triage = triage;
        this.handlers = handlers;
    }

    @KafkaListener(topics = "${app.kafka.inbound-topic}", groupId = "triage-workers")
    @Transactional
    public void onTicket(IngestEvent event) {
        long started = System.nanoTime();
        Ticket t = tickets.findById(event.ticketId()).orElse(null);
        if (t == null) { log.warn("ticket {} not found", event.ticketId()); return; }
        // idempotency: Kafka may redeliver; skip if already finished
        if (t.getStatus() == Ticket.Status.HANDLED || t.getStatus() == Ticket.Status.NEEDS_HUMAN) {
            log.info("ticket {} already {}, skipping (dedup)", t.getId(), t.getStatus());
            return;
        }
        t.setStatus(Ticket.Status.TRIAGING);

        TriageAgent.Triage tr = triage.classify(t.getSubject(), t.getBody());
        String[] out = handlers.handle(tr.category(), t.getTenantId(), t.getSubject(), t.getBody());

        t.setCategory(tr.category());
        t.setPriority(tr.priority());
        t.setAssignedAgent(out[0]);
        t.setResult(out[1]);
        t.setStatus(Ticket.Status.valueOf(out[2]));
        t.setLatencyMs((int) ((System.nanoTime() - started) / 1_000_000));
        tickets.save(t);
        log.info("ticket {} -> {}/{} -> {} ({}ms)", t.getId(), tr.category(), tr.priority(), out[2], t.getLatencyMs());
    }
}
