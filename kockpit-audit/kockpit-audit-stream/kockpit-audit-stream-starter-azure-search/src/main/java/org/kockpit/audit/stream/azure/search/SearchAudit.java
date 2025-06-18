package org.kockpit.audit.stream.azure.search;

import com.azure.search.documents.indexes.FieldBuilderIgnore;
import com.azure.search.documents.indexes.SimpleField;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class SearchAudit {

  @SimpleField(isFilterable = true, isSortable = true)
  private String type;

  @FieldBuilderIgnore // fixme
  private List<Map<String, Object>> events;
}
