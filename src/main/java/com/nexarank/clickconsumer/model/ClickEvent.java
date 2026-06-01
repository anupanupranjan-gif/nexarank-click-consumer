// Copyright (c) 2026 Anup Ranjan. Licensed under Apache 2.0 (https://www.apache.org/licenses/LICENSE-2.0)
package com.nexarank.clickconsumer.model;

public class ClickEvent {
    private String sessionId;
    private String query;
    private String productId;
    private String productTitle;
    private int position;
    private long timestamp;

    public ClickEvent() {}

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductTitle() { return productTitle; }
    public void setProductTitle(String productTitle) { this.productTitle = productTitle; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "ClickEvent{query='" + query + "', productId='" + productId +
               "', position=" + position + "}";
    }
}
