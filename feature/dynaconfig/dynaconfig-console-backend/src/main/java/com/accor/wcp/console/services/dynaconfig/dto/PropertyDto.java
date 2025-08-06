package com.accor.wcp.console.services.dynaconfig.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PropertyDto {
  Source source;
  Long lastUpdatedTimestamp;
  String name;
  String type;
  String value;
  String comment;
  String description;
  Status status;
  List<PropertyInstanceDto> instances = new ArrayList<>();
}
