package org.kockpit.audit.stream.azure.search;

import com.azure.search.documents.indexes.SimpleField;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public final class SearchIndexedKeyValue {

  @SimpleField(isFilterable = true, isSortable = true)
  private String key;

  @SimpleField(isFilterable = true, isSortable = true)
  private String value;

  @SimpleField(isFilterable = true, isSortable = true)
  private Integer valueInteger;

  @SimpleField(isFilterable = true, isSortable = true)
  private Float valueFloat;

  @SimpleField(isFilterable = true, isSortable = true)
  private Date valueDate;
}