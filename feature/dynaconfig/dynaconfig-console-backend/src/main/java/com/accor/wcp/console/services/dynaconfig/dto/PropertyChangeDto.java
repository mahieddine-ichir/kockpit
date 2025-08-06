package com.accor.wcp.console.services.dynaconfig.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PropertyChangeDto {
  private Long timestamp;
  private String propertyName;
  private String valueBeforeChange;
  private String valueAfterChange;
  private String username;
}
