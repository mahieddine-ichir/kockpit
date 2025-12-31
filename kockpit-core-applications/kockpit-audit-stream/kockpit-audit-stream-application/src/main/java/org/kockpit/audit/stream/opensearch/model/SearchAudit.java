package org.kockpit.audit.stream.opensearch.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SearchAudit {

  private String type;

  private List<Map<String, Object>> events;
}
