package org.kockpit.ai.mcp.server.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class AuditDataPage {

  private List<Map<String, Object>> items;

  private Long totalSize;

  private Integer size;

  private Integer from;
}
