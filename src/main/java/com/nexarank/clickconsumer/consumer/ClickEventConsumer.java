// Copyright (c) 2026 Anup Ranjan. Licensed under Apache 2.0 (https://www.apache.org/licenses/LICENSE-2.0)
package com.nexarank.clickconsumer.consumer;

import com.nexarank.clickconsumer.model.ClickAggregate;
import com.nexarank.clickconsumer.model.ClickEvent;
import com.nexarank.clickconsumer.store.ClickEventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ClickEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ClickEventConsumer.class);

    private final ClickEventStore store;

    public ClickEventConsumer(ClickEventStore store) {
        this.store = store;
    }

    @KafkaListener(
        topics = "${nexarank.kafka.topic:search-clicks}",
        groupId = "${nexarank.kafka.group-id:click-consumer-group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ClickEvent event) {
        if (event == null || event.getQuery() == null || event.getProductId() == null) {
            log.warn("Received invalid click event, skipping");
            return;
        }

        log.debug("Received click: query={} product={} position={}",
            event.getQuery(), event.getProductId(), event.getPosition());

        try {
            processClickEvent(event);
        } catch (Exception e) {
            log.error("Failed to process click event: {}", event, e);
        }
    }

    private void processClickEvent(ClickEvent event) {
        String query = event.getQuery().toLowerCase().trim();
        String productId = event.getProductId();

        // Load existing aggregate or create new one
        ClickAggregate aggregate = store
            .findByQueryAndProductId(query, productId)
            .orElseGet(() -> createNew(query, event));

        // Update aggregate
        aggregate.setClickCount(aggregate.getClickCount() + 1);
        aggregate.setLastClickedAt(event.getTimestamp());

        // Update rolling average position
        long prevClicks = aggregate.getClickCount() - 1;
        double newAvg = prevClicks == 0
            ? event.getPosition()
            : (aggregate.getAvgPosition() * prevClicks + event.getPosition()) / aggregate.getClickCount();
        aggregate.setAvgPosition(newAvg);

        // Update product title if provided
        if (event.getProductTitle() != null && !event.getProductTitle().isBlank()) {
            aggregate.setProductTitle(event.getProductTitle());
        }

        aggregate.recalculate();
        store.upsertClickAggregate(aggregate);

        log.debug("Updated aggregate: query={} product={} clicks={} avgPos={:.1f} ctr={:.3f}",
            query, productId, aggregate.getClickCount(),
            aggregate.getAvgPosition(), aggregate.getCtr());
    }

    private ClickAggregate createNew(String query, ClickEvent event) {
        ClickAggregate a = new ClickAggregate();
        a.setId(ClickAggregate.buildId(query, event.getProductId()));
        a.setQuery(query);
        a.setProductId(event.getProductId());
        a.setProductTitle(event.getProductTitle());
        a.setClickCount(0);
        a.setImpressionCount(0);
        a.setAvgPosition(0);
        a.setCtr(0);
        a.setUpdatedAt(System.currentTimeMillis());
        return a;
    }
}
