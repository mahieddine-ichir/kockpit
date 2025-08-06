package com.accor.wcp.console.services.audit.console.backend.search.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditMetadataDto {

  private String name;

  private String label;

  private String description;

  private SearchType type;

  private List<String> options;

  private String subtype;
}
