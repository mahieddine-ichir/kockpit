package com.accor.wcp.console.services.audit.console.backend.search.dto;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.List;
import lombok.Getter;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum SearchType {
  @JsonEnumDefaultValue
  STRING(Constants.TEXT_BASED_OPS),
  INTEGER(Constants.NUMBER_BASED_OPS),
  FLOAT(Constants.NUMBER_BASED_OPS),
  DATE(Constants.NUMBER_BASED_OPS),
  // TEXT and EXTENSION must be replace by STRING in all manifest
  TEXT(Constants.TEXT_BASED_OPS),
  EXTENSION(Constants.TEXT_BASED_OPS);

  @Getter private final List<Operation> operations;

  SearchType(List<Operation> operations) {
    this.operations = operations;
  }

  public String getName() {
    return this.name();
  }
}
