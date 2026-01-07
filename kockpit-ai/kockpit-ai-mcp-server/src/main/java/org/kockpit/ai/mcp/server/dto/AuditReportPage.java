package org.kockpit.ai.mcp.server.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AuditReportPage {

  private List<AuditReport> items;

  private Long totalSize;

  private Integer size;

  private Integer from;
}
