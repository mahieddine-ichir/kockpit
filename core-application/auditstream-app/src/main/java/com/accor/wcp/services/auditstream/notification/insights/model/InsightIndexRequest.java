package com.accor.wcp.services.auditstream.notification.insights.model;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class InsightIndexRequest {

  private String docId;

  private String domain;

  private String env;

  private int ttl;

  private final Map<String, Object> data = new HashMap<>();

  public InsightIndexRequest add(String key, Object value) {
    this.data.put(key, value);
    return this;
  }
}
