package org.kockpit.audit.stream.api;

import lombok.Data;

import java.util.Date;

@Data
public final class IndexedKeyValue {

  private String key;

  private String value;

  private Integer valueInteger;

  private Float valueFloat;

  private Date valueDate;
}