package com.accor.wcp.console.services.audit.console.backend.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchQuery {

  private SearchType type;

  /**
   * Emplacement of the field -> (actually -> builtin.kv, null)
   *
   * <p>builtin.kv -> indexedExtensions.indexedKeyValues null -> root of the document
   */
  private String subtype;

  /** Name of the field in ES document */
  private String name;

  private OperationQuery operation;
}
