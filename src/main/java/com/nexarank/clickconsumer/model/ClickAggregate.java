// Copyright (c) 2026 Anup Ranjan. Licensed under Apache 2.0 (https://www.apache.org/licenses/LICENSE-2.0)
package com.nexarank.clickconsumer.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "search-click-events")
public class ClickAggregate {

    @Id
    private String id; // composite: query__productId

    @Field(type = FieldType.Keyword)
    private String query;

    @Field(type = FieldType.Keyword)
    private String productId;

    @Field(type = FieldType.Text)
    private String productTitle;

    @Field(type = FieldType.Long)
    private long clickCount;

    @Field(type = FieldType.Long)
    private long impressionCount;

    @Field(type = FieldType.Double)
    private double avgPosition;

    @Field(type = FieldType.Double)
    private double ctr; // clickCount / impressionCount

    @Field(type = FieldType.Long)
    private long lastClickedAt;

    @Field(type = FieldType.Long)
    private long updatedAt;

    public ClickAggregate() {}

    public static String buildId(String query, String productId) {
        return query.toLowerCase().replaceAll("[^a-z0-9]", "_")
               + "__" + productId;
    }

    public void recalculate() {
        this.ctr = impressionCount > 0
            ? (double) clickCount / impressionCount
            : 0.0;
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductTitle() { return productTitle; }
    public void setProductTitle(String productTitle) { this.productTitle = productTitle; }

    public long getClickCount() { return clickCount; }
    public void setClickCount(long clickCount) { this.clickCount = clickCount; }

    public long getImpressionCount() { return impressionCount; }
    public void setImpressionCount(long impressionCount) { this.impressionCount = impressionCount; }

    public double getAvgPosition() { return avgPosition; }
    public void setAvgPosition(double avgPosition) { this.avgPosition = avgPosition; }

    public double getCtr() { return ctr; }
    public void setCtr(double ctr) { this.ctr = ctr; }

    public long getLastClickedAt() { return lastClickedAt; }
    public void setLastClickedAt(long lastClickedAt) { this.lastClickedAt = lastClickedAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
