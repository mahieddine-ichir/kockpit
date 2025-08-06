package com.accor.wcp.console.services.dynaconfig.instance.domain;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = "currentValue")
public class DynaConfigInstance {
  private String applicationInstance;
  private String propertyKey;
  private String currentValue;
}
