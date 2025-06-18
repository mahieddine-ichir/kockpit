package org.kockpit.audit.stream.azure.search;

import com.azure.search.documents.indexes.SimpleField;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SearchAudit {

  @SimpleField(isFilterable = true, isSortable = true)
  private String type;

  @SimpleField
  private List<String> events;
}
