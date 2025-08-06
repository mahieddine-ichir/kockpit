package com.accor.wcp.console.services.sqsdlq.dynamo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum OrderBy {
  asc(true),
  desc(false);

  @Getter private final Boolean value;
}
