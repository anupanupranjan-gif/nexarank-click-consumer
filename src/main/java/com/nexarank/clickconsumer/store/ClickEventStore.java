// Copyright (c) 2026 Anup Ranjan. Licensed under Apache 2.0 (https://www.apache.org/licenses/LICENSE-2.0)
package com.nexarank.clickconsumer.store;

import com.nexarank.clickconsumer.model.ClickAggregate;
import java.util.List;
import java.util.Optional;

/**
 * Storage abstraction for click event aggregates.
 * Engine-agnostic — swap implementations via @ConditionalOnProperty.
 * Today: Elasticsearch. Future: OpenSearch, PostgreSQL.
 */
public interface ClickEventStore {

    void upsertClickAggregate(ClickAggregate aggregate);

    Optional<ClickAggregate> findByQueryAndProductId(String query, String productId);

    List<ClickAggregate> findLowCtrQueries(int minImpressions, double maxCtr, int limit);

    List<ClickAggregate> findHighCtrAtLowPosition(double minCtr, double maxAvgPosition, int limit);

    List<ClickAggregate> findTopClickedForQuery(String query, int limit);
}
