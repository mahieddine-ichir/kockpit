package org.kockpit.audit.backend.model;

import lombok.Data;

import java.util.Date;

@Data
public final class SearchIndexedKeyValue {

  private String key;

  private String value;

  private Integer valueInteger;

  private Double valueFloat;

  private Date valueDate;
}