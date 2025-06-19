package org.kockpit.audit.backoffice.DataModel;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public final class SearchIndexedKeyValue {

  private String key;

  private String value;

  private Integer valueInteger;

  private Float valueFloat;

  private Date valueDate;
}