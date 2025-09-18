package org.kockpit.audit.stream.opensearch.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public final class SearchIndexedKeyValue {

  private String key;

  private String value;

  private Integer valueInteger;

  private Double valueFloat;

  private Date valueDate;
}
