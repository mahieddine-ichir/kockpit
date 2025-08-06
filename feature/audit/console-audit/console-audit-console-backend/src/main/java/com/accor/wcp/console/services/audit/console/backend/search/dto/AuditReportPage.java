package com.accor.wcp.console.services.audit.console.backend.search.dto;

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

  public static AuditReportPage empty() {
    return AuditReportPage.builder()
            .size(0).from(0).totalSize(0L)
            .build();
  }
}
