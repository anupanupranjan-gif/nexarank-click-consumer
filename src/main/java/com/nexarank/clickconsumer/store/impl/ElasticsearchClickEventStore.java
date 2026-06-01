// Copyright (c) 2026 Anup Ranjan. Licensed under Apache 2.0 (https://www.apache.org/licenses/LICENSE-2.0)
package com.nexarank.clickconsumer.store.impl;

import com.nexarank.clickconsumer.model.ClickAggregate;
import com.nexarank.clickconsumer.store.ClickEventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@ConditionalOnProperty(name = "nexarank.store", havingValue = "elasticsearch", matchIfMissing = true)
public class ElasticsearchClickEventStore implements ClickEventStore {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchClickEventStore.class);

    private final ElasticsearchOperations esOps;

    public ElasticsearchClickEventStore(ElasticsearchOperations esOps) {
        this.esOps = esOps;
    }

    @Override
    public void upsertClickAggregate(ClickAggregate aggregate) {
        try {
            esOps.save(aggregate);
            log.debug("Upserted click aggregate: query={} product={}",
                aggregate.getQuery(), aggregate.getProductId());
        } catch (Exception e) {
            log.error("Failed to upsert click aggregate: query={} product={}",
                aggregate.getQuery(), aggregate.getProductId(), e);
        }
    }

    @Override
    public Optional<ClickAggregate> findByQueryAndProductId(String query, String productId) {
        String id = ClickAggregate.buildId(query, productId);
        try {
            ClickAggregate found = esOps.get(id, ClickAggregate.class);
            return Optional.ofNullable(found);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<ClickAggregate> findLowCtrQueries(int minImpressions, double maxCtr, int limit) {
        try {
            CriteriaQuery query = new CriteriaQuery(
                new Criteria("impressionCount").greaterThanEqual(minImpressions)
                    .and(new Criteria("ctr").lessThanEqual(maxCtr))
            );
            query.setMaxResults(limit);
            SearchHits<ClickAggregate> hits = esOps.search(query, ClickAggregate.class);
            return hits.getSearchHits().stream()
                .map(h -> h.getContent())
                .toList();
        } catch (Exception e) {
            log.error("Failed to find low CTR queries", e);
            return List.of();
        }
    }

    @Override
    public List<ClickAggregate> findHighCtrAtLowPosition(double minCtr, double maxAvgPosition, int limit) {
        try {
            CriteriaQuery query = new CriteriaQuery(
                new Criteria("ctr").greaterThanEqual(minCtr)
                    .and(new Criteria("avgPosition").greaterThanEqual(maxAvgPosition))
            );
            query.setMaxResults(limit);
            SearchHits<ClickAggregate> hits = esOps.search(query, ClickAggregate.class);
            return hits.getSearchHits().stream()
                .map(h -> h.getContent())
                .toList();
        } catch (Exception e) {
            log.error("Failed to find high CTR at low position", e);
            return List.of();
        }
    }

    @Override
    public List<ClickAggregate> findTopClickedForQuery(String query, int limit) {
        try {
            CriteriaQuery cq = new CriteriaQuery(
                new Criteria("query").is(query.toLowerCase())
            );
            cq.setMaxResults(limit);
            SearchHits<ClickAggregate> hits = esOps.search(cq, ClickAggregate.class);
            return hits.getSearchHits().stream()
                .map(h -> h.getContent())
                .sorted((a, b) -> Long.compare(b.getClickCount(), a.getClickCount()))
                .toList();
        } catch (Exception e) {
            log.error("Failed to find top clicked for query={}", query, e);
            return List.of();
        }
    }
}
