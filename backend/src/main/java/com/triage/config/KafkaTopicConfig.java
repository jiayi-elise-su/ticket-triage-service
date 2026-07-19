package com.triage.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    /*
     * TODO (design trade-off, not a bug — found while load-testing Slice 2):
     * IngestService.publish() keys each Kafka message by tenantId, so every message
     * for a given tenant always lands on the same partition. That guarantees
     * per-tenant ordering, but it also means a single tenant's traffic is inherently
     * capped at ONE consumer instance's throughput, no matter how many instances run
     * in the `triage-workers` group. "3 partitions -> scale workers" only delivers
     * real parallelism when traffic is spread across >= 3 concurrently-active
     * tenants; a load test with one dominant tenant sent 100% of the load to a
     * single instance even with 3 instances running.
     * Revisit if a single large ("whale") tenant shows up in practice — options
     * include keying by tenantId+shard-bucket (accepting some reordering within a
     * tenant) or giving hot tenants their own topic/partition.
     */
    @Bean
    public NewTopic inboundTopic(@Value("${app.kafka.inbound-topic}") String name) {
        return TopicBuilder.name(name).partitions(3).replicas(1).build();  // 3 partitions -> scale workers
    }
}
