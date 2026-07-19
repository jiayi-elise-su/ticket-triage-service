package com.triage.ingest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class IngestService {
    private static final Logger log = LoggerFactory.getLogger(IngestService.class);
    private final KafkaTemplate<String, Object> kafka;
    private final String topic;

    public IngestService(KafkaTemplate<String, Object> kafka,
                         @Value("${app.kafka.inbound-topic}") String topic) {
        this.kafka = kafka;
        this.topic = topic;
    }

    public void publish(Long ticketId, String tenantId) {
        kafka.send(topic, tenantId, new IngestEvent(ticketId, tenantId));
        log.info("published ticket {} tenant {}", ticketId, tenantId);
    }
}
