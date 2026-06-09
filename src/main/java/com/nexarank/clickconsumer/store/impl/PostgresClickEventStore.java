// Copyright (c) 2026 Anup Ranjan. Licensed under Apache 2.0 (https://www.apache.org/licenses/LICENSE-2.0)
package com.nexarank.clickconsumer.store.impl;

import com.nexarank.clickconsumer.model.ClickAggregate;
import com.nexarank.clickconsumer.model.ClickEvent;
import com.nexarank.clickconsumer.store.ClickEventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

@Component
@ConditionalOnProperty(name = "nexarank.store", havingValue = "postgresql")
public class PostgresClickEventStore implements ClickEventStore {

    private static final Logger log = LoggerFactory.getLogger(PostgresClickEventStore.class);

    @Value("${nexarank.api.base-url:http://nexarank-api.default.svc.cluster.local/nexarank/api/v1}")
    private String apiBaseUrl;

    @Value("${nexarank.api.tenant-id:default}")
    private String tenantId;

    @Value("${nexarank.api.project-id:main}")
    private String projectId;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public void upsertClickAggregate(ClickAggregate aggregate) {
        try {
            String json = String.format(
                "{\"sessionId\":\"%s\",\"query\":\"%s\",\"productId\":\"%s\",\"productTitle\":\"%s\",\"position\":%d}",
                aggregate.getId(),
                aggregate.getQuery(),
                aggregate.getProductId() != null ? aggregate.getProductId() : "",
                aggregate.getProductTitle() != null ? aggregate.getProductTitle().replace("\"", "'") : "",
                (int) aggregate.getAvgPosition()
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiBaseUrl + "/clicks"))
                    .header("Content-Type", "application/json")
                    .header("X-Tenant-Id", tenantId)
                    .header("X-Project-Id", projectId)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 201) {
                log.warn("Failed to store click event: HTTP {}", response.statusCode());
            }
        } catch (Exception e) {
            log.error("Failed to store click event via API: {}", e.getMessage());
        }
    }

    @Override
    public Optional<ClickAggregate> findByQueryAndProductId(String query, String productId) {
        return Optional.empty();
    }

    @Override
    public List<ClickAggregate> findLowCtrQueries(int minImpressions, double maxCtr, int limit) {
        return List.of();
    }

    @Override
    public List<ClickAggregate> findHighCtrAtLowPosition(double minCtr, double maxAvgPosition, int limit) {
        return List.of();
    }

    @Override
    public List<ClickAggregate> findTopClickedForQuery(String query, int limit) {
        return List.of();
    }
}
